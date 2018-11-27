package com.syswin.temail.ps.server.handler;

import static com.syswin.temail.ps.common.Constants.CDTP_VERSION;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.INTERNAL_ERROR;
import static com.syswin.temail.ps.common.entity.CommandType.LOGIN;
import static com.syswin.temail.ps.common.entity.CommandType.LOGOUT;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPServerError;
import com.syswin.temail.ps.common.exception.PacketException;
import com.syswin.temail.ps.server.service.RequestService;
import com.syswin.temail.ps.server.service.SessionService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Sharable
public class PacketHandler extends SimpleChannelInboundHandler<CDTPPacket> {

  private final SessionService sessionService;
  private final RequestService requestService;

  public PacketHandler(
      SessionService sessionService,
      RequestService requestService) {
    this.sessionService = sessionService;
    this.requestService = requestService;
  }

  @Override
  public void channelRead0(ChannelHandlerContext ctx, CDTPPacket packet) {
    try {
      Channel channel = ctx.channel();
      short commandSpace = packet.getCommandSpace();
      short command = packet.getCommand();

      validateHeader(packet);

      if (commandSpace == CHANNEL_CODE) {
        if (command == LOGIN.getCode()) {
          sessionService.login(channel, packet);
        } else if (command == LOGOUT.getCode()) {
          sessionService.logout(channel, packet);
        } else {
          log.warn("Received unknown command {} {}", Integer.toHexString(commandSpace),
              Integer.toHexString(command));
        }
      } else {
        sessionService.bind(channel, packet);
        requestService.handleRequest(packet, msg -> ctx.writeAndFlush(msg, ctx.voidPromise()));
      }
    } catch (Exception e) {
      throw new PacketException(e, packet);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    log.error("Failed to handle packet on channel: {}", ctx.channel(), cause);
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
      ctx.writeAndFlush(packet, ctx.voidPromise());
    }
  }

  private void validateHeader(CDTPPacket packet) {
    if (packet.getHeader() == null
        || StringUtil.isNullOrEmpty(packet.getHeader().getDeviceId())
        || StringUtil.isNullOrEmpty(packet.getHeader().getSender())) {
      throw new IllegalArgumentException("Sender and device ID must not be empty");
    }
  }
}
