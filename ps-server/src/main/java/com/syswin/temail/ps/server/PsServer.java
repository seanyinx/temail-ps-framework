package com.syswin.temail.ps.server;

import com.syswin.temail.ps.common.Constants;
import com.syswin.temail.ps.common.codec.BodyExtractor;
import com.syswin.temail.ps.common.codec.PacketDecoder;
import com.syswin.temail.ps.common.codec.PacketEncoder;
import com.syswin.temail.ps.common.codec.SimpleBodyExtractor;
import com.syswin.temail.ps.common.packet.KeyAwarePacketDecryptor;
import com.syswin.temail.ps.common.packet.KeyAwarePacketEncryptor;
import com.syswin.temail.ps.common.packet.KeyAwarePacketSigner;
import com.syswin.temail.ps.common.packet.KeyAwarePacketVerifier;
import com.syswin.temail.ps.common.packet.PacketDecryptor;
import com.syswin.temail.ps.common.packet.PacketEncryptor;
import com.syswin.temail.ps.common.packet.PacketSigner;
import com.syswin.temail.ps.common.packet.PacketVerifier;
import com.syswin.temail.ps.common.packet.PublicKeyPacketEncryptor;
import com.syswin.temail.ps.common.packet.PublicKeyPacketVerifier;
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
 * <H1>普通用法</H1>
 * <H2>使用示例</H2>
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
 *     @Override
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
 *     psServer.start();
 * }
 * </pre>
 * <h1>自动加密解密签名验签用法</h1>
 * PsServer可以设置为自动签名、验签，自动加密解密。四项功能可以分别设置，分别对应以下对象：
 * <li><strong>PacketSigner</strong>：Packet签名生成器</li>
 * 用于自动给Packet生成签名。要启用签名，传送的PacketSigner，还在在Header里指定具体的签名算法：signatureAlgorithm。
 * <ol>
 * <li>PacketSigner.NoOp</li>如果不需要签名，则可以使用{@code PacketSigner#NoOp}（也是不设置时的默认值 ）。
 * <li>KeyAwarePacketSigner</li>{@link KeyAwarePacketSigner }是基于kms系统的签名生成器。当前只支持ECC算法。
 * <li>PacketSigner</li>也可以根据自己的需要实现接口{@link PacketSigner}
 * </ol>
 *
 * <li><strong>PacketVerifier</strong>：Packet签名验证器</li>
 * 用于自动对Packet的签名进行验证。
 * <ol>
 * <li>PacketVerifier.NoOp</li>如果不需要验证签名，则可以使用{@code PacketVerifier.NoOp}（也是不设置时的默认值 ）。
 * <li>KeyAwarePacketVerifier</li>{@link KeyAwarePacketVerifier }是基于kms系统的签名验证器。如果发送者在kms系统里管理，则使用kms系统中的公钥进行验签；否则使用packet包里的公钥进行验签。
 * <li>PublicKeyPacketVerifier</li>{@link PublicKeyPacketVerifier }只使用Packet包里的公钥对数据进行验签。
 * <li>PacketVerifier</li>也可以根据自己的需要实现接口{@link PacketVerifier}
 * </ol>
 * </li>
 * <li><strong>PacketEncryptor</strong>：Packet数据加密器</li>
 * 用于自动给Packet的Data进行自动加密。
 * <ol>
 * <li>PacketEncryptor.NoOp</li>如果不需要自动加密，则可以使用{@code PacketEncryptor.NoOp}（也是不设置时的默认值 ）。
 * <li>KeyAwarePacketEncryptor</li>{@link KeyAwarePacketEncryptor }是基于kms系统的数据加密器。如果接收者在kms系统中进行管理，则使用kms系统中的公钥进行加密，否则使用packet包里的公钥进行加密。
 * <li>PublicKeyPacketEncryptor</li>{@link PublicKeyPacketEncryptor }只使用Packet包里的公钥对数据进行加密。
 * <li>PacketEncryptor</li>也可以根据自己的需要实现接口{@link PacketEncryptor}
 * </ol>
 *
 * <li><strong>PacketDecryptor</strong>：Packet数据解密器</li>
 * 根据具体的解密算法将数据进行解密。解密包含在bodyExtractor中。
 * <ol>
 * <li>PacketDecryptor.NoOp</li>如果不解密，使用{@link PacketDecryptor#NoOp }。
 * <li>KeyAwarePacketDecryptor</li>{@link KeyAwarePacketDecryptor }是基于kms系统的数据解密器；
 * <li>PacketDecryptor</li>也可以根据自己的需要实现接口{@link PacketDecryptor}。
 * </ol>
 *
 * <li>bodyExtractor：Packet包体提取器，默认实现是{@link SimpleBodyExtractor}，可根据业务需要实现接口{@link BodyExtractor}。</li>
 *
 * <P></P>
 * <H2>使用示例</H2>
 * public void startServer() {
 *
 * String bmsBaseUrl="http://kms.systoon.com";
 * String tenantId="syswin";
 * KeyAwareVault vault = VaultKeeper.keyAwareVault(bmsBaseUrl, tenantId);
 * PsServer psServer =
 * new PsServer(
 * new TestSessionService(),
 * new TestRequestService(testRequestHandler),
 * serverPort,
 * serverReadIdleTimeSeconds,
 * new SimpleBodyExtractor(new KeyAwarePacketDecryptor(vault)),
 * new KeyAwarePacketSigner(vault),
 * new KeyAwarePacketVerifier(vault),
 * new KeyAwarePacketEncryptor(vault));
 * psServer.start();
 * }
 */
@Slf4j
public class PsServer {

  private final IdleHandler idleHandler;
  private final PsServerHandler psServerHandler;
  private final int port;
  private final int idleTimeSeconds;
  private final BodyExtractor bodyExtractor;
  private final PacketSigner signer;
  private final PacketVerifier verifier;
  private final PacketEncryptor encryptor;

  public PsServer(SessionService sessionService, RequestService requestService, int port, int idleTimeSeconds) {
    this(sessionService, requestService, port, idleTimeSeconds, SimpleBodyExtractor.INSTANCE,
        PacketSigner.NoOp, PacketVerifier.NoOp, PacketEncryptor.NoOp);
  }

  public PsServer(SessionService sessionService, RequestService requestService, int port, int idleTimeSeconds,
      BodyExtractor bodyExtractor) {
    this(sessionService, requestService, port, idleTimeSeconds, bodyExtractor,
        PacketSigner.NoOp, PacketVerifier.NoOp, PacketEncryptor.NoOp);
  }

  public PsServer(SessionService sessionService, RequestService requestService, int port,
      int idleTimeSeconds, BodyExtractor bodyExtractor, PacketSigner signer,
      PacketVerifier verifier, PacketEncryptor encryptor) {
    this.idleHandler = new IdleHandler(sessionService);
    this.port = port;
    this.idleTimeSeconds = idleTimeSeconds;
    this.bodyExtractor = bodyExtractor;
    this.signer = signer;
    this.verifier = verifier;
    this.encryptor = encryptor;
    this.psServerHandler = new PsServerHandler(sessionService, requestService, new HeartBeatService());
  }

  /**
   * 运行PsServer
   *
   * @deprecated 使用 {@link #start }替换
   */
  @Deprecated
  public void run() {
    this.start();
  }

  /**
   * 启动PsServer
   */
  public void start() {
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
                .addLast("packetEncoder", new PacketEncoder(signer, encryptor))
                .addLast("packetDecoder", new PacketDecoder(bodyExtractor, verifier))
                .addLast("psServerHandler", psServerHandler);
          }
        });

    // 异步地绑定服务器;调用sync方法阻塞等待直到绑定完成
    bootstrap.bind().syncUninterruptibly();
    log.info("Temail 服务器已启动,端口号：{}", port);
  }

}
