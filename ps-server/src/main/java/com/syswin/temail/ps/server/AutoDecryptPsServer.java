package com.syswin.temail.ps.server;

import com.syswin.temail.ps.common.codec.decrypt.AutoDecryptBodyExtractor;
import com.syswin.temail.ps.server.service.RequestService;
import com.syswin.temail.ps.server.service.SessionService;

/**
 * @author 姚华成
 * @date 2018-10-19
 */
public class AutoDecryptPsServer extends PsServer {

  public AutoDecryptPsServer(SessionService sessionService,
      RequestService requestService, int port, int idleTimeSeconds,
      String vaultRegistryUrl, String tenantId) {
    super(sessionService, requestService, port, idleTimeSeconds,
        new AutoDecryptBodyExtractor(vaultRegistryUrl, tenantId));
  }
}
