package com.syswin.temail.ps.server;

import com.syswin.temail.kms.vault.KeyAwareVault;
import com.syswin.temail.ps.common.codec.BodyExtractor;
import com.syswin.temail.ps.common.packet.KeyAwareEccPacketEncryptor;
import com.syswin.temail.ps.common.packet.KeyAwareEccPacketSigner;
import com.syswin.temail.ps.common.packet.KeyAwarePacketVerifier;
import com.syswin.temail.ps.server.service.RequestService;
import com.syswin.temail.ps.server.service.SessionService;

/**
 * @author 姚华成
 * @date 2018-11-06
 */
public class FullAutoPsServer extends PsServer {

  public FullAutoPsServer(SessionService sessionService,
      RequestService requestService, int port, int idleTimeSeconds,
      BodyExtractor bodyExtractor, KeyAwareVault vault) {
    super(sessionService, requestService, port, idleTimeSeconds, bodyExtractor,
        new KeyAwareEccPacketSigner(vault), new KeyAwarePacketVerifier(vault),
        new KeyAwareEccPacketEncryptor(vault));
  }
}
