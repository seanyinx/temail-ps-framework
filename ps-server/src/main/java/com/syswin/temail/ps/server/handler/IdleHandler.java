package com.syswin.temail.ps.server.handler;

import com.syswin.temail.ps.server.entity.Session;
import com.syswin.temail.ps.server.service.ChannelCollector;
import com.syswin.temail.ps.server.service.SessionHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleUserEventChannelHandler;
import io.netty.handler.timeout.IdleStateEvent;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 姚华成
 * @date 2018-8-24
 */
@Slf4j
@Sharable
public class IdleHandler extends SimpleUserEventChannelHandler<IdleStateEvent> {

  private final SessionHandler sessionService;
  private final ChannelCollector channelCollector;

  public IdleHandler(SessionHandler sessionService, ChannelCollector channelCollector) {
    this.sessionService = sessionService;
    this.channelCollector = channelCollector;
  }

  @Override
  protected void eventReceived(ChannelHandlerContext ctx, IdleStateEvent evt) {
    Collection<Session> sessions = channelCollector.removeChannel(ctx.channel());
    sessionService.disconnect(sessions);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    Collection<Session> sessions = channelCollector.removeChannel(ctx.channel());
    sessionService.disconnect(sessions);
  }

}
