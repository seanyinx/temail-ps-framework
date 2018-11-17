package com.syswin.temail.ps.server;

import com.syswin.temail.ps.common.Constants;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.server.handler.IdleHandler;
import com.syswin.temail.ps.server.handler.PacketHandler;
import com.syswin.temail.ps.server.service.HeartBeatService;
import com.syswin.temail.ps.server.service.RequestService;
import com.syswin.temail.ps.server.service.SessionService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayServer {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final IdleHandler idleHandler;
  private final PacketHandler packetHandler;
  private final int port;
  private final int idleTimeSeconds;
  private final Supplier<MessageToByteEncoder<CDTPPacket>> packetEncoderSupplier;
  private final Supplier<ByteToMessageDecoder> packetDecoderSupplier;

  public GatewayServer(SessionService sessionService,
      RequestService requestService,
      Supplier<MessageToByteEncoder<CDTPPacket>> packetEncoderSupplier,
      Supplier<ByteToMessageDecoder> packetDecoderSupplier,
      int port,
      int idleTimeSeconds) {

    this.idleHandler = new IdleHandler(sessionService, idleTimeSeconds);
    this.packetHandler = new PacketHandler(sessionService, requestService, new HeartBeatService());
    this.packetEncoderSupplier = packetEncoderSupplier;
    this.packetDecoderSupplier = packetDecoderSupplier;
    this.port = port;
    this.idleTimeSeconds = idleTimeSeconds;
  }

  public Stoppable run() {
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;// 默认 cpu

    try {
      bossGroup = new EpollEventLoopGroup(1);
      workerGroup = new EpollEventLoopGroup();
      LOGGER.info("Using epoll event loop group");
    } catch (Throwable t) {
      bossGroup = new NioEventLoopGroup(1);
      workerGroup = new NioEventLoopGroup();
      LOGGER.info("Using Nio event loop group, since epoll is only available on linux");
    }

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
                .addLast("idleStateHandler", new IdleStateHandler(idleTimeSeconds, 0, 0))
                .addLast("idleHandler", idleHandler)
                .addLast("lengthFieldBasedFrameDecoder",
                    new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, Constants.LENGTH_FIELD_LENGTH, 0, 0))
                .addLast("lengthFieldPrepender",
                    new LengthFieldPrepender(Constants.LENGTH_FIELD_LENGTH, 0, false))
                .addLast("packetEncoder", packetEncoderSupplier.get())
                .addLast("packetDecoder", packetDecoderSupplier.get())
                .addLast("packetHandler", packetHandler);
          }
        });

    // 异步地绑定服务器;调用sync方法阻塞等待直到绑定完成
    bootstrap.bind().syncUninterruptibly();
    LOGGER.info("Temail 服务器已启动,端口号：{}", port);
    return stoppable(bossGroup, workerGroup);
  }

  private Stoppable stoppable(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
    return () -> {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    };
  }
}
