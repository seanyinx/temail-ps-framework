package com.syswin.temail.ps.server.handler;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.INTERNAL_ERROR;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPServerError;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPServerError.Builder;
import com.syswin.temail.ps.server.service.ChannelHolder;
import io.netty.channel.Channel;

class RequestInterceptor {

  private final ChannelHolder channelHolder;

  RequestInterceptor(ChannelHolder channelHolder) {
    this.channelHolder = channelHolder;
  }

  boolean isLoggedIn(Channel channel, CDTPPacket packet) {
    String temail = packet.getHeader().getSender();
    String deviceId = packet.getHeader().getDeviceId();
    if (!authSession(channel, temail, deviceId)) {
      CDTPPacket errorPacket = errorPacket(packet, INTERNAL_ERROR.getCode(),
          "用户" + temail + "在设备" + deviceId + "上没有登录，无法进行操作！");
      channel.writeAndFlush(errorPacket);
      return false;
    }
    return true;
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
