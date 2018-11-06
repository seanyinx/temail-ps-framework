package com.syswin.temail.ps.server;

import com.syswin.temail.kms.vault.KeyAwareVault;
import com.syswin.temail.kms.vault.VaultKeeper;
import com.syswin.temail.ps.common.codec.BodyExtractor;
import com.syswin.temail.ps.common.codec.SimpleBodyExtractor;
import com.syswin.temail.ps.common.packet.KeyAwareEccPacketEncryptor;
import com.syswin.temail.ps.common.packet.KeyAwareEccPacketSigner;
import com.syswin.temail.ps.common.packet.KeyAwarePacketDecryptor;
import com.syswin.temail.ps.common.packet.KeyAwarePacketVerifier;
import com.syswin.temail.ps.common.packet.PacketDecryptor;
import com.syswin.temail.ps.common.packet.PacketEncryptor;
import com.syswin.temail.ps.common.packet.PacketSigner;
import com.syswin.temail.ps.common.packet.PacketVerifier;
import com.syswin.temail.ps.common.utils.StringUtil;
import com.syswin.temail.ps.server.exception.PsServerException;
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
  private PacketDecryptor decryptor;

  private String vaultRegistryUrl;
  private String tenantId;

  public PsServerBuilder() {
  }

  public static PsServer buildDefault(@NonNull SessionService sessionService, @NonNull RequestService requestService) {
    return new PsServer(sessionService, requestService, DEFAULT_PORT, DEFAULT_IDLE_TIME_SECONDS);
  }

  public static PsServer buildFullAuto(@NonNull SessionService sessionService, @NonNull RequestService requestService,
      @NonNull KeyAwareVault vault) {
    return new FullAutoPsServer(sessionService, requestService, DEFAULT_PORT, DEFAULT_IDLE_TIME_SECONDS,
        new SimpleBodyExtractor(), vault);
  }

  /**
   * 构建PsClient对象
   *
   * @return 构造好的PsClient对象
   */
  public PsServer build() {

    KeyAwareVault vault = null;
    if (StringUtil.hasText(vaultRegistryUrl) && StringUtil.hasText(tenantId)) {
      vault = VaultKeeper.keyAwareVault(vaultRegistryUrl, tenantId);
    }
    if (signer == null) {
      if (vault != null) {
        signer = new KeyAwareEccPacketSigner(vault);
      } else {
        throw new PsServerException("自动签名必须设置签名对象signer，或者指定vaultRegistryUrl和tenantId！");
      }
    }

    if (verifier == null) {
      if (vault != null) {
        verifier = new KeyAwarePacketVerifier(vault);
      } else {
        throw new PsServerException("自动验签必须设置签名验证对象verifier，或者指定vaultRegistryUrl和tenantId！");
      }
    }

    if (encryptor == null) {
      if (vault != null) {
        encryptor = new KeyAwareEccPacketEncryptor(vault);
      } else {
        throw new PsServerException("自动加密必须设置加密对象encryptor，或者指定vaultRegistryUrl和tenantId！");
      }
    }

    if (decryptor == null) {
      if (vault != null) {
        decryptor = new KeyAwarePacketDecryptor(vault);
      } else {
        throw new PsServerException("自动解密必须设置解密对象decryptor，或者指定vaultRegistryUrl和tenantId！");
      }
    }

    return new PsServer(sessionService, requestService, port, idleTimeSeconds,
        bodyExtractor, signer, verifier, encryptor, decryptor);
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

  public PsServerBuilder vaultRegistryUrl(String vaultRegistryUrl) {
    this.vaultRegistryUrl = vaultRegistryUrl;
    return this;
  }

  public PsServerBuilder tenantId(String tenantId) {
    this.tenantId = tenantId;
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

  public PsServerBuilder decryptor(PacketDecryptor decryptor) {
    this.decryptor = decryptor;
    return this;
  }
}
