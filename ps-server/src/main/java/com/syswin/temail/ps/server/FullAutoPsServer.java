package com.syswin.temail.ps.server;

import com.syswin.temail.kms.vault.KeyAwareVault;
import com.syswin.temail.ps.common.codec.BodyExtractor;
import com.syswin.temail.ps.common.packet.KeyAwarePacketEncryptor;
import com.syswin.temail.ps.common.packet.KeyAwarePacketSigner;
import com.syswin.temail.ps.common.packet.KeyAwarePacketVerifier;
import com.syswin.temail.ps.server.service.RequestService;
import com.syswin.temail.ps.server.service.SessionService;

/**
 * 本类与PsServer的区别主要在于本类使用kms系统进行管理的自动加密解密签名验签能力。
 *
 * @author 姚华成
 * @date 2018-11-06
 */
public class FullAutoPsServer extends PsServer {

  public FullAutoPsServer(SessionService sessionService,
      RequestService requestService, int port, int idleTimeSeconds,
      BodyExtractor bodyExtractor, KeyAwareVault vault) {
    super(sessionService, requestService, port, idleTimeSeconds, bodyExtractor,
        new KeyAwarePacketSigner(vault), new KeyAwarePacketVerifier(vault),
        new KeyAwarePacketEncryptor(vault));
  }
}
