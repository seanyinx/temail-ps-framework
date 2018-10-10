package com.syswin.temail.ps.server.service;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.INTERNAL_ERROR;
import static com.syswin.temail.ps.common.utils.SignatureUtil.resetSignature;

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

  /**
   * 判断发起请求的用户是否登录的扩展接口
   *
   * @param packet 请求数据包
   * @return 是否登录
   * @deprecated 请求不再进行是否登录的判断
   */
//   * 而是使用{@link #bind(Channel, CDTPPacket)}进行处理：
//   * 如果没有建立会话，则自动建立会话；如果已经建立会话则什么都不做
  @Deprecated
  protected boolean isLoggedInExt(CDTPPacket packet) {
    return true;
  }

  protected boolean loginExt(CDTPPacket packet, CDTPPacket respPacket) {
    CDTPLoginResp.Builder builder = CDTPLoginResp.newBuilder();
    builder.setCode(Constants.HTTP_STATUS_OK);
    respPacket.setData(builder.build().toByteArray());
    resetSignature(respPacket);
    return true;
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
  @SuppressWarnings("deprecation")
  public boolean isLoggedIn(Channel channel, CDTPPacket packet) {
    String temail = packet.getHeader().getSender();
    String deviceId = packet.getHeader().getDeviceId();
    boolean loggedIn = hasSession(channel, temail, deviceId) && isLoggedInExt(packet);
    if (!loggedIn) {
      CDTPPacket errorPacket = errorPacket(packet, INTERNAL_ERROR.getCode(),
          "用户" + temail + "在设备" + deviceId + "上没有登录，无法进行操作！");
      channel.writeAndFlush(errorPacket);
    }
    return loggedIn;
  }

  @Override
  public void login(Channel channel, CDTPPacket packet) {
    CDTPHeader header = packet.getHeader();
    CDTPPacket respPacket = new CDTPPacket(packet);
    if (loginExt(packet, respPacket)) {
      channelHolder.addSession(header.getSender(), header.getDeviceId(), channel);
      channel.writeAndFlush(respPacket);
    } else {
      channel.writeAndFlush(respPacket);
    }
  }
//
//  @Override
//  public boolean bind(Channel channel, CDTPPacket packet) {
//    String temail = packet.getHeader().getSender();
//    String deviceId = packet.getHeader().getDeviceId();
//    if (hasSession(channel, temail, deviceId)) {
//      // 已经构建会话
//      return true;
//    }
//    CDTPPacket respPacket = new CDTPPacket(packet);
//    boolean loginExt = loginExt(packet, respPacket);
//    if (loginExt) {
//      // 成功登录
//      channelHolder.addSession(temail, deviceId, channel);
//    } else {
//      channel.writeAndFlush(respPacket);
//    }
//    return loginExt;
//  }

  @Override
  public void logout(Channel channel, CDTPPacket packet) {
    CDTPHeader header = packet.getHeader();
    CDTPPacket respPacket = new CDTPPacket(packet);
    logoutExt(packet, respPacket);
    channel.writeAndFlush(respPacket);
    channelHolder.removeSession(header.getSender(), header.getDeviceId(), channel);
  }

  @Override
  public void disconnect(Channel channel) {
    Collection<Session> sessions = channelHolder.removeChannel(channel);
    disconnectExt(sessions);
    channel.close();
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

  private boolean hasSession(Channel channel, String temail, String deviceId) {
    return isNotEmpty(temail) && isNotEmpty(deviceId)
        && channel == channelHolder.getChannel(temail, deviceId);
  }

  private boolean isNotEmpty(String str) {
    return str != null && str.length() > 0;
  }
}
