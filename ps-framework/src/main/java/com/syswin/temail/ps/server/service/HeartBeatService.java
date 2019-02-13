package com.syswin.temail.ps.server.service;

import static com.syswin.temail.ps.common.entity.CommandType.PONG;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.netty.channel.Channel;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartBeatService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void pong(Channel channel, CDTPPacket packet) {
    LOG.info("Received heartbeat on channel {}", channel);
    packet.setCommand(PONG.getCode());
    channel.writeAndFlush(packet, channel.voidPromise());
  }
}
