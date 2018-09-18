package com.syswin.temail.ps.server.connection;

import com.syswin.temail.ps.common.Constants;
import com.syswin.temail.ps.server.handler.ChannelExceptionHandler;
import com.syswin.temail.ps.server.handler.IdleHandler;
import com.syswin.temail.ps.server.handler.TemailGatewayHandler;
import com.syswin.temail.ps.server.service.ChannelHolder;
import com.syswin.temail.ps.server.service.HeartBeatService;
import com.syswin.temail.ps.server.service.RequestHandler;
import com.syswin.temail.ps.server.service.SessionHandler;
import com.syswin.temail.ps.common.codec.BodyExtractor;
import com.syswin.temail.ps.common.codec.PacketDecoder;
import com.syswin.temail.ps.common.codec.PacketEncoder;
import com.syswin.temail.ps.common.codec.SimpleBodyExtractor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.InetSocketAddress;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TemailGatewayServer {

  private final IdleHandler idleHandler;
  private final TemailGatewayHandler temailGatewayHandler;
  private final ChannelExceptionHandler channelExceptionHandler;
  private final BodyExtractor bodyExtractor;
  private final ChannelHolder channelHolder;

  public TemailGatewayServer(SessionHandler sessionHandler, RequestHandler requestHandler) {
    this(sessionHandler, requestHandler, new ChannelHolder());
  }

  public TemailGatewayServer(SessionHandler sessionHandler, RequestHandler requestHandler,
      BodyExtractor bodyExtractor) {
    this(sessionHandler, requestHandler, bodyExtractor, new ChannelHolder());
  }

  private TemailGatewayServer(SessionHandler sessionHandler, RequestHandler requestHandler,
      ChannelHolder channelHolder) {
    this(new TemailGatewayHandler(sessionHandler, requestHandler, new HeartBeatService(), channelHolder),
        new ChannelExceptionHandler(),
        new SimpleBodyExtractor(),
        sessionHandler,
        channelHolder);
  }

  private TemailGatewayServer(
      SessionHandler sessionHandler,
      RequestHandler requestHandler,
      BodyExtractor bodyExtractor,
      ChannelHolder channelHolder) {

    this(new TemailGatewayHandler(sessionHandler, requestHandler, new HeartBeatService(), channelHolder),
        new ChannelExceptionHandler(),
        bodyExtractor,
        sessionHandler,
        channelHolder);
  }

  private TemailGatewayServer(
      TemailGatewayHandler temailGatewayHandler,
      ChannelExceptionHandler channelExceptionHandler,
      BodyExtractor bodyExtractor,
      SessionHandler sessionHandler,
      ChannelHolder channelHolder) {
    this.channelHolder = channelHolder;
    this.idleHandler = new IdleHandler(sessionHandler, channelHolder);
    this.temailGatewayHandler = temailGatewayHandler;
    this.channelExceptionHandler = channelExceptionHandler;
    this.bodyExtractor = bodyExtractor;
  }

  public void run(int port, final int readIdleTimeSeconds) {
    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();// 默认 cup

    ServerBootstrap bootstrap = new ServerBootstrap();

    bootstrap.group(bossGroup, workerGroup)
        // 使用指定端口设置套接字地址
        .channel(NioServerSocketChannel.class)
        // 指定使用NIO传输Channel
        .localAddress(new InetSocketAddress(port))
        // 通过NoDelay禁用Nagle,使消息立即发送出去
        .childOption(ChannelOption.TCP_NODELAY, true)
        // 保持长连接状态
        .childOption(ChannelOption.SO_KEEPALIVE, true)
        .childHandler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel channel) {
            channel.pipeline()
                .addLast("idleStateHandler", new IdleStateHandler(readIdleTimeSeconds, 0, 0))
                .addLast("idleHandler", idleHandler)
                .addLast("lengthFieldBasedFrameDecoder",
                    new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, Constants.LENGTH_FIELD_LENGTH, 0, 0))
                .addLast("lengthFieldPrepender",
                    new LengthFieldPrepender(Constants.LENGTH_FIELD_LENGTH, 0, false))
                .addLast("packetDecoder", new PacketDecoder(bodyExtractor))
                .addLast("packetEncoder", new PacketEncoder())
                .addLast("temailGatewayHandler", temailGatewayHandler)
                .addLast("channelExceptionHandler", channelExceptionHandler);
          }
        });

    // 异步地绑定服务器;调用sync方法阻塞等待直到绑定完成
    bootstrap.bind().syncUninterruptibly();
    log.info("Temail 服务器已启动,端口号：{}", port);
  }

  public ChannelHolder channelHolder() {
    return channelHolder;
  }
}
