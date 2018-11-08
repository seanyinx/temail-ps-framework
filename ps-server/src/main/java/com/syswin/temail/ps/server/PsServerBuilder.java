package com.syswin.temail.ps.server;

import com.syswin.temail.kms.vault.KeyAwareVault;
import com.syswin.temail.ps.common.codec.BodyExtractor;
import com.syswin.temail.ps.common.codec.SimpleBodyExtractor;
import com.syswin.temail.ps.common.packet.KeyAwarePacketDecryptor;
import com.syswin.temail.ps.common.packet.PacketEncryptor;
import com.syswin.temail.ps.common.packet.PacketSigner;
import com.syswin.temail.ps.common.packet.PacketVerifier;
import com.syswin.temail.ps.server.service.RequestService;
import com.syswin.temail.ps.server.service.SessionService;
import lombok.NonNull;

/**
 * @author 姚华成
 * @date 2018-11-06
 */
public class PsServerBuilder {

  private static final int DEFAULT_IDLE_TIME_SECONDS = 30;
  private static final int DEFAULT_PORT = 8099;
  private SessionService sessionService;
  private RequestService requestService;
  private int port = DEFAULT_PORT;
  private int idleTimeSeconds = DEFAULT_IDLE_TIME_SECONDS;

  private BodyExtractor bodyExtractor;
  private PacketSigner signer;
  private PacketVerifier verifier;
  private PacketEncryptor encryptor;

  public PsServerBuilder() {
  }

  public static PsServer buildDefault(@NonNull SessionService sessionService, @NonNull RequestService requestService) {
    return new PsServer(sessionService, requestService, DEFAULT_PORT, DEFAULT_IDLE_TIME_SECONDS);
  }

  public static PsServer buildFullAuto(@NonNull SessionService sessionService, @NonNull RequestService requestService,
      @NonNull KeyAwareVault vault) {
    return new FullAutoPsServer(sessionService, requestService, DEFAULT_PORT, DEFAULT_IDLE_TIME_SECONDS,
        new SimpleBodyExtractor(new KeyAwarePacketDecryptor(vault)), vault);
  }

  /**
   * 构建PsClient对象
   *
   * @return 构造好的PsClient对象
   */
  public PsServer build() {

    if (signer == null) {
      signer = PacketSigner.NoOp;
    }
    if (verifier == null) {
      verifier = PacketVerifier.NoOp;
    }
    if (encryptor == null) {
      encryptor = PacketEncryptor.NoOp;
    }
    if (bodyExtractor == null) {
      bodyExtractor = SimpleBodyExtractor.INSTANCE;
    }

    return new PsServer(sessionService, requestService, port, idleTimeSeconds,
        bodyExtractor, signer, verifier, encryptor);
  }

  public PsServerBuilder sessionService(SessionService sessionService) {
    this.sessionService = sessionService;
    return this;
  }

  public PsServerBuilder requestService(RequestService requestService) {
    this.requestService = requestService;
    return this;
  }

  public PsServerBuilder port(int port) {
    this.port = port;
    return this;
  }

  public PsServerBuilder idleTimeSeconds(int idleTimeSeconds) {
    this.idleTimeSeconds = idleTimeSeconds;
    return this;
  }

  public PsServerBuilder bodyExtractor(BodyExtractor bodyExtractor) {
    this.bodyExtractor = bodyExtractor;
    return this;
  }

  public PsServerBuilder signer(PacketSigner signer) {
    this.signer = signer;
    return this;
  }

  public PsServerBuilder verifier(PacketVerifier verifier) {
    this.verifier = verifier;
    return this;
  }

  public PsServerBuilder encryptor(PacketEncryptor encryptor) {
    this.encryptor = encryptor;
    return this;
  }
}
