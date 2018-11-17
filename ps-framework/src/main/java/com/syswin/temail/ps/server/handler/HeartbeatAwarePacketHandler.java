package com.syswin.temail.ps.server.handler;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.PING;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.server.service.HeartBeatService;
import com.syswin.temail.ps.server.service.RequestService;
import com.syswin.temail.ps.server.service.SessionService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

@Sharable
public class HeartbeatAwarePacketHandler extends PacketHandler {

  private final HeartBeatService heartBeatService;

  public HeartbeatAwarePacketHandler(
      SessionService sessionService,
      RequestService requestService,
      HeartBeatService heartBeatService) {

    super(sessionService, requestService);
    this.heartBeatService = heartBeatService;
  }

  @Override
  public void channelRead0(ChannelHandlerContext ctx, CDTPPacket packet) {
    short commandSpace = packet.getCommandSpace();
    short command = packet.getCommand();
    if (commandSpace == CHANNEL_CODE && command == PING.getCode()) {
      heartBeatService.pong(ctx.channel(), packet);
      return;
    }

    super.channelRead0(ctx, packet);
  }
}
