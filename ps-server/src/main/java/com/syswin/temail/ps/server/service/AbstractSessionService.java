package com.syswin.temail.ps.server.service;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.INTERNAL_ERROR;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLoginResp;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLogoutResp;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPServerError;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPServerError.Builder;
import com.syswin.temail.ps.server.Constants;
import com.syswin.temail.ps.server.entity.Session;
import io.netty.channel.Channel;
import java.util.Collection;
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

  protected boolean isLoggedInExt(CDTPPacket packet) {
    return true;
  }

  protected boolean loginExt(CDTPPacket reqPacket, CDTPPacket respPacket) {
    CDTPLoginResp.Builder builder = CDTPLoginResp.newBuilder();
    builder.setCode(Constants.HTTP_STATUS_OK);
    respPacket.setData(builder.build().toByteArray());
    return true;
  }

  protected void logoutExt(CDTPPacket reqPacket, CDTPPacket respPacket) {
    CDTPLogoutResp.Builder builder = CDTPLogoutResp.newBuilder();
    builder.setCode(Constants.HTTP_STATUS_OK);
    respPacket.setData(builder.build().toByteArray());
  }

  protected void disconnectExt(Collection<Session> sessions) {
  }

  @Override
  public boolean isLoggedIn(Channel channel, CDTPPacket packet) {
    String temail = packet.getHeader().getSender();
    String deviceId = packet.getHeader().getDeviceId();
    if (!authSession(channel, temail, deviceId) ||
        !isLoggedInExt(packet)) {
      CDTPPacket errorPacket = errorPacket(packet, INTERNAL_ERROR.getCode(),
          "用户" + temail + "在设备" + deviceId + "上没有登录，无法进行操作！");
      channel.writeAndFlush(errorPacket);
      return false;
    }
    return true;
  }

  @Override
  public void login(Channel channel, CDTPPacket reqPacket) {
    CDTPHeader header = reqPacket.getHeader();
    CDTPPacket respPacket = new CDTPPacket(reqPacket);
    if (loginExt(reqPacket, respPacket)) {
      channelHolder.addSession(header.getSender(), header.getDeviceId(), channel);
      channel.writeAndFlush(respPacket);
    } else {
      channel.writeAndFlush(respPacket);
      if (channelHolder.hasNoSession(channel)) {
        log.debug("连接关闭前的请求堆栈信息", new RuntimeException(channel.toString()));
        channel.close();
      }
    }
  }

  @Override
  public void logout(Channel channel, CDTPPacket reqPacket) {
    CDTPHeader header = reqPacket.getHeader();
    CDTPPacket respPacket = new CDTPPacket(reqPacket);
    logoutExt(reqPacket, respPacket);
    channel.writeAndFlush(respPacket);
    channelHolder.removeSession(header.getSender(), header.getDeviceId(), channel);
  }

  @Override
  public void disconnect(Channel channel) {
    Collection<Session> sessions = channelHolder.removeChannel(channel);
    disconnectExt(sessions);
  }

  private CDTPPacket errorPacket(CDTPPacket packet, int code, String message) {
    packet.setCommandSpace(CHANNEL_CODE);
    packet.setCommand(INTERNAL_ERROR.getCode());

    Builder builder = CDTPServerError.newBuilder();
    builder.setCode(code);
    builder.setDesc(message);
    packet.setData(builder.build().toByteArray());
    return packet;
  }

  private boolean authSession(Channel channel, String temail, String deviceId) {
    return isNotEmpty(temail)
        && isNotEmpty(deviceId)
        && channel == channelHolder.getChannel(temail, deviceId);
  }

  private boolean isNotEmpty(String str) {
    return str != null && str.length() > 0;
  }
}
