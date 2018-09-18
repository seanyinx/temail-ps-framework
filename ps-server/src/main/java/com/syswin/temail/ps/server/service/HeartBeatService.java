package com.syswin.temail.ps.server.service;

import static com.syswin.temail.ps.common.entity.CommandType.PONG;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.netty.channel.Channel;

public class HeartBeatService {

  public void pong(Channel channel, CDTPPacket packet) {
    packet.setCommand(PONG.getCode());
    channel.writeAndFlush(packet);
  }
}
