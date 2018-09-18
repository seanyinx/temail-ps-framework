package com.syswin.temail.ps.server.handler;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL;
import static com.syswin.temail.ps.common.entity.CommandType.LOGIN;
import static com.syswin.temail.ps.common.entity.CommandType.LOGOUT;
import static com.syswin.temail.ps.common.entity.CommandType.PING;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.exception.PacketException;
import com.syswin.temail.ps.server.service.ChannelHolder;
import com.syswin.temail.ps.server.service.HeartBeatService;
import com.syswin.temail.ps.server.service.RequestHandler;
import com.syswin.temail.ps.server.service.SessionHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Sharable
public class TemailGatewayHandler extends SimpleChannelInboundHandler<CDTPPacket> {

  private final SessionHandler sessionService;
  private final RequestHandler requestService;
  private final HeartBeatService heartBeatService;
  private final ChannelHolder channelHolder;
  private final RequestInterceptor requestInterceptor;

  public TemailGatewayHandler(
      SessionHandler sessionService,
      RequestHandler requestService,
      HeartBeatService heartBeatService,
      ChannelHolder channelHolder) {
    this.sessionService = sessionService;
    this.requestService = requestService;
    this.heartBeatService = heartBeatService;
    this.channelHolder = channelHolder;
    this.requestInterceptor = new RequestInterceptor(channelHolder);
  }

  @Override
  public void channelRead0(ChannelHandlerContext ctx, CDTPPacket packet) {
    try {
      Channel channel = ctx.channel();
      if (packet.getCommandSpace() == CHANNEL.getCode()) {
        if (packet.getCommand() == PING.getCode()) {
          heartBeatService.pong(channel, packet);
        } else if (packet.getCommand() == LOGIN.getCode()) {
          sessionService.login(packet, loginHandler(channel));
        } else if (packet.getCommand() == LOGOUT.getCode()) {
          // TODO: 2018/8/31 only allowed after login
          sessionService.logout(packet, new LogoutHandlerImpl(channelHolder, channel));
        } else {
          log.warn("Received unknown command {} {}", Integer.toHexString(packet.getCommandSpace()), Integer.toHexString(packet.getCommand()));
        }
      } else {
        if (requestInterceptor.isLoggedIn(channel, packet)) {
          requestService.handleRequest(packet, channel::writeAndFlush);
        }
      }
    } catch (Exception e) {
      throw new PacketException(e, packet);
    }
  }

  private LoginHandler loginHandler(Channel channel) {
    return new LoginHandler() {
      @Override
      public void onSucceed(CDTPPacket request, CDTPPacket response) {
        CDTPHeader header = request.getHeader();
        channelHolder.addSession(header.getSender(), header.getDeviceId(), channel);
        channel.writeAndFlush(response);
      }

      @Override
      public void onFailed(CDTPPacket response) {
        channel.writeAndFlush(response);

        if (channelHolder.hasNoSession(channel)) {
          log.debug("连接关闭前的请求堆栈信息", new RuntimeException(channel.toString()));
          channel.close();
        }
      }
    };
  }
}
