package com.syswin.temail.ps.server.handler;

import com.syswin.temail.ps.server.service.SessionService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleUserEventChannelHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 姚华成
 * @date 2018-8-24
 */
@Slf4j
@Sharable
public class IdleHandler extends SimpleUserEventChannelHandler<IdleStateEvent> {

  private final SessionService sessionService;

  public IdleHandler(SessionService sessionService) {
    this.sessionService = sessionService;
  }

  @Override
  protected void eventReceived(ChannelHandlerContext ctx, IdleStateEvent evt) {
    if (evt.state() == IdleState.READER_IDLE) {
      Channel channel = ctx.channel();
      sessionService.disconnect(channel);
      channel.close();
    }
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    sessionService.disconnect(ctx.channel());
    ctx.channel().close();
  }

}
