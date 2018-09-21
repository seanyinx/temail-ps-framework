package com.syswin.temail.ps.server;

import com.syswin.temail.ps.common.Constants;
import com.syswin.temail.ps.common.codec.BodyExtractor;
import com.syswin.temail.ps.common.codec.PacketDecoder;
import com.syswin.temail.ps.common.codec.PacketEncoder;
import com.syswin.temail.ps.common.codec.SimpleBodyExtractor;
import com.syswin.temail.ps.server.handler.IdleHandler;
import com.syswin.temail.ps.server.handler.PsServerHandler;
import com.syswin.temail.ps.server.service.HeartBeatService;
import com.syswin.temail.ps.server.service.RequestService;
import com.syswin.temail.ps.server.service.SessionService;
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
public class PsServer {

  private final IdleHandler idleHandler;
  private final PsServerHandler psServerHandler;
  private final BodyExtractor bodyExtractor;
  private final int port;
  private final int readIdleTimeSeconds;

  public PsServer(SessionService sessionService, RequestService requestService, int port, int readIdleTimeSeconds) {
    this(sessionService, requestService, port, readIdleTimeSeconds, new SimpleBodyExtractor());
  }

  public PsServer(SessionService sessionService, RequestService requestService, int port,
      int readIdleTimeSeconds, BodyExtractor bodyExtractor) {
    this.idleHandler = new IdleHandler(sessionService);
    this.port = port;
    this.readIdleTimeSeconds = readIdleTimeSeconds;
    this.psServerHandler = new PsServerHandler(sessionService, requestService, new HeartBeatService());
    this.bodyExtractor = bodyExtractor;
  }

  public void run() {
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
                .addLast("psServerHandler", psServerHandler);
          }
        });

    // 异步地绑定服务器;调用sync方法阻塞等待直到绑定完成
    bootstrap.bind().syncUninterruptibly();
    log.info("Temail 服务器已启动,端口号：{}", port);
  }

}
