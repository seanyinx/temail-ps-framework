package com.syswin.temail.ps.server.service;

import static com.syswin.temail.ps.server.utils.SignatureUtil.resetSignature;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLoginResp;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLogoutResp;
import com.syswin.temail.ps.server.Constants;
import com.syswin.temail.ps.server.entity.Session;
import io.netty.channel.Channel;
import java.util.Collection;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 姚华成
 * @date 2018-9-19
 */
@Slf4j
public abstract class AbstractSessionService implements SessionService {

  @Getter
  private final ChannelHolder channelHolder = new ChannelHolder();

  protected void loginExtAsync(CDTPPacket reqPacket, Consumer<CDTPPacket> successHandler,
      Consumer<CDTPPacket> failedHandler) {
    CDTPPacket respPacket = new CDTPPacket(reqPacket);
    CDTPLoginResp.Builder builder = CDTPLoginResp.newBuilder();
    builder.setCode(Constants.HTTP_STATUS_OK);
    respPacket.setData(builder.build().toByteArray());
    resetSignature(respPacket);
    successHandler.accept(respPacket);
  }

  protected void logoutExt(CDTPPacket packet, CDTPPacket respPacket) {
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
          channelHolder.addSession(header.getSender(), header.getDeviceId(), channel);
          channel.writeAndFlush(respPacket, channel.voidPromise());
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
          channelHolder.addSession(temail, deviceId, channel);
        },
        respPacket -> {
          log.debug("User {} on device {} bound to channel {} failed", temail, deviceId, channel);
          // 自动绑定操作，登录失败不需要处理
        });
  }

  @Override
  public final void logout(Channel channel, CDTPPacket packet) {
    CDTPHeader header = packet.getHeader();
    CDTPPacket respPacket = new CDTPPacket(packet);
    logoutExt(packet, respPacket);
    channel.writeAndFlush(respPacket, channel.voidPromise());
    channelHolder.removeSession(header.getSender(), header.getDeviceId(), channel);
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
