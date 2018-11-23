package com.syswin.temail.ps.server.service;

import static com.syswin.temail.ps.server.utils.SignatureUtil.resetSignature;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLoginResp;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLogoutResp;
import com.syswin.temail.ps.server.Constants;
import com.syswin.temail.ps.server.entity.Session;
import com.syswin.temail.ps.server.service.channels.strategy.ChannelManager;
import com.syswin.temail.ps.server.service.channels.strategy.one2one.ChannelManagerOne2One;
import io.netty.channel.Channel;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 姚华成
 * @date 2018-9-19
 */
@Slf4j
public abstract class AbstractSessionService implements SessionService {

  @Getter
  private final ChannelManager channelHolder = new ChannelManagerOne2One();

  protected void loginExtAsync(CDTPPacket reqPacket, Function<CDTPPacket,Collection<Session>> successHandler,
      Consumer<CDTPPacket> failedHandler) {
    CDTPPacket respPacket = new CDTPPacket(reqPacket);
    CDTPLoginResp.Builder builder = CDTPLoginResp.newBuilder();
    builder.setCode(Constants.HTTP_STATUS_OK);
    respPacket.setData(builder.build().toByteArray());
    resetSignature(respPacket);
    successHandler.apply(respPacket);
  }

  protected void logoutExt(CDTPPacket packet, CDTPPacket respPacket, Supplier<Collection<Session>> rmedSessionsSupplier) {
    CDTPLogoutResp.Builder builder = CDTPLogoutResp.newBuilder();
    builder.setCode(Constants.HTTP_STATUS_OK);
    respPacket.setData(builder.build().toByteArray());
    resetSignature(respPacket);
  }

  protected void disconnectExt(Collection<Session> sessions) {
  }

  @Override
  public final void login(Channel channel, CDTPPacket reqPacket) {
    CDTPHeader header = reqPacket.getHeader();
    loginExtAsync(reqPacket,
        respPacket -> {
          log.debug("User {} on device {} logged in on channel {} successfully", header.getSender(), header.getDeviceId(), channel);
          Collection<Session> sessions = channelHolder.addSession(header.getSender(), header.getDeviceId(), channel);
          channel.writeAndFlush(respPacket, channel.voidPromise());
          return sessions;
        },
        msg -> {
          log.debug("User {} on device {} logged in on channel {} failed", header.getSender(), header.getDeviceId(), channel);
          channel.writeAndFlush(msg, channel.voidPromise());
        });
  }

  @Override
  public final void bind(Channel channel, CDTPPacket reqPacket) {
    String temail = reqPacket.getHeader().getSender();
    String deviceId = reqPacket.getHeader().getDeviceId();
    if (hasSession(channel, temail, deviceId)) {
      // 已经构建会话
      log.debug("Skip binding because user {} on device {} is already connected on channel {}", temail, deviceId, channel);
      return;
    }
    loginExtAsync(reqPacket,
        respPacket -> {
          log.debug("User {} on device {} bound to channel {} successfully", temail, deviceId, channel);
          return channelHolder.addSession(temail, deviceId, channel);
        },
        respPacket -> {
          log.debug("User {} on device {} bound to channel {} failed", temail, deviceId, channel);
          // 自动绑定操作，登录失败不需要处理
        });
  }

  @Override
  public final void logout(Channel channel, CDTPPacket packet) {
    CDTPHeader header = packet.getHeader();
    Collection<Session> sessions = channelHolder.removeSession(header.getSender(), header.getDeviceId(), channel);
    CDTPPacket respPacket = new CDTPPacket(packet);
    logoutExt(packet, respPacket, () -> sessions);
    channel.writeAndFlush(respPacket, channel.voidPromise());
    log.debug("User {} on device {} logged out on channel {} successfully", header.getSender(), header.getDeviceId(), channel);
  }

  @Override
  public final void disconnect(Channel channel) {
    Collection<Session> sessions = channelHolder.removeChannel(channel);
    log.debug("Removed sessions {} on closing channel {}", channel);
    disconnectExt(sessions);
  }

  private boolean hasSession(Channel channel, String temail, String deviceId) {
    return isNotEmpty(temail) && isNotEmpty(deviceId)
        && channel == channelHolder.getChannel(temail, deviceId);
  }

  private boolean isNotEmpty(String str) {
    return str != null && str.length() > 0;
  }
}
