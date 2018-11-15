package com.syswin.temail.ps.server.handler;

import static com.syswin.temail.ps.common.Constants.CDTP_VERSION;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.INTERNAL_ERROR;
import static com.syswin.temail.ps.common.entity.CommandType.LOGIN;
import static com.syswin.temail.ps.common.entity.CommandType.LOGOUT;
import static com.syswin.temail.ps.common.entity.CommandType.PING;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPServerError;
import com.syswin.temail.ps.common.exception.PacketException;
import com.syswin.temail.ps.server.service.HeartBeatService;
import com.syswin.temail.ps.server.service.RequestService;
import com.syswin.temail.ps.server.service.SessionService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Sharable
public class PacketHandler extends SimpleChannelInboundHandler<CDTPPacket> {

  private final SessionService sessionService;
  private final RequestService requestService;
  private final HeartBeatService heartBeatService;

  public PacketHandler(
      SessionService sessionService,
      RequestService requestService,
      HeartBeatService heartBeatService) {
    this.sessionService = sessionService;
    this.requestService = requestService;
    this.heartBeatService = heartBeatService;
  }

  @Override
  public void channelRead0(ChannelHandlerContext ctx, CDTPPacket packet) {
    try {
      Channel channel = ctx.channel();
      short commandSpace = packet.getCommandSpace();
      if (commandSpace == CHANNEL_CODE) {
        short command = packet.getCommand();
        if (command == PING.getCode()) {
          heartBeatService.pong(channel, packet);
        } else if (command == LOGIN.getCode()) {
          sessionService.login(channel, packet);
        } else if (command == LOGOUT.getCode()) {
          sessionService.logout(channel, packet);
        } else {
          log.warn("Received unknown command {} {}", Integer.toHexString(commandSpace),
              Integer.toHexString(command));
        }
      } else {
        // 异步执行绑定动作
        channel.eventLoop().execute(() -> sessionService.bind(channel, packet));
        requestService.handleRequest(packet, channel::writeAndFlush);
      }
    } catch (Exception e) {
      throw new PacketException(e, packet);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    log.error("数据处理异常，通道信息" + ctx.channel(), cause);
    if (ctx.channel().isActive()) {
      CDTPPacket packet;
      if (cause instanceof PacketException && ((PacketException) cause).getPacket() != null) {
        PacketException packetException = (PacketException) cause;
        packet = packetException.getPacket();
      } else {
        CDTPHeader header = new CDTPHeader();
        packet = new CDTPPacket();
        packet.setHeader(header);
        packet.setVersion(CDTP_VERSION);
      }
      packet.setCommandSpace(CHANNEL_CODE);
      packet.setCommand(INTERNAL_ERROR.getCode());
      CDTPServerError.Builder builder = CDTPServerError.newBuilder();
      builder.setCode(INTERNAL_ERROR.getCode());
      if (cause != null) {
        builder.setDesc(cause.getMessage());
      }
      packet.setData(builder.build().toByteArray());
      ctx.channel().writeAndFlush(packet);
    }
  }
}
