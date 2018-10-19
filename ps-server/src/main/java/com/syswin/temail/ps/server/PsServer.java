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

/**
 * <H1>PsServer服务器对象</H1>
 * 为了构建PsServer，需要实现SessionService和RequestService接口。 其中：
 * <ul>
 * <li>SessionService已经提供了实现基本会话管理功能的AbstractSessionService类，
 * 只要在这个类的基础上重载相应的ext方法即可。</li>
 * <li>RequestService是实际的业务处理是接口，接口处理完成后，必须调用responseHandler将响应返回客户端。</li>
 * <ul/>
 * <p>
 * 构建完PsServer实例，调用run方法即可将PsServer启动起来。<br>
 * <H1>使用示例</H1>
 * <pre>
 * class TestSessionService extends AbstractSessionService {}
 * class TestRequestService implements RequestService {
 *
 *     private TestRequestHandler handler;
 *
 *     TestRequestService(TestRequestHandler handler) {
 *         this.handler = handler;
 *     }
 *
 *     public void handleRequest(CDTPPacket reqPacket, Consumer<CDTPPacket> responseHandler) {
 *         responseHandler.accept(handler.dispatch(reqPacket));
 *     }
 * }
 * public void startServer() {
 *     PsServer psServer =
 *         new PsServer(
 *             new TestSessionService(),
 *             new TestRequestService(testRequestHandler),
 *             serverPort, serverReadIdleTimeSeconds);
 *     psServer.run();
 * }
 * </pre>
 */
@Slf4j
public class PsServer {

  private final IdleHandler idleHandler;
  private final PsServerHandler psServerHandler;
  private final int port;
  private final int idleTimeSeconds;
  private final BodyExtractor bodyExtractor;

  public PsServer(SessionService sessionService, RequestService requestService, int port, int idleTimeSeconds) {
    this(sessionService, requestService, port, idleTimeSeconds, new SimpleBodyExtractor());
  }

  public PsServer(SessionService sessionService, RequestService requestService, int port,
      int idleTimeSeconds, BodyExtractor bodyExtractor) {
    this.idleHandler = new IdleHandler(sessionService);
    this.port = port;
    this.idleTimeSeconds = idleTimeSeconds;
    this.bodyExtractor = bodyExtractor;
    this.psServerHandler = new PsServerHandler(sessionService, requestService, new HeartBeatService());
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
                .addLast("idleStateHandler", new IdleStateHandler(idleTimeSeconds, 0, 0))
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
