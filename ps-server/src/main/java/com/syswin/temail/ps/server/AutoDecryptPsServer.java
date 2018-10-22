package com.syswin.temail.ps.server;

import com.syswin.temail.ps.common.codec.decrypt.AutoDecryptBodyExtractor;
import com.syswin.temail.ps.server.service.RequestService;
import com.syswin.temail.ps.server.service.SessionService;

/**
 * <H1>AutoDecryptPsServer服务器对象</H1>
 * 本类与PsServer的区别主要是：本类会自动对加密的数据进行解密。
 * 因此需要配置解密所需要的KMS相关的信息：地址：vaultRegistryUrl，租户ID：tenantId
 */
public class AutoDecryptPsServer extends PsServer {

  public AutoDecryptPsServer(SessionService sessionService,
      RequestService requestService, int port, int idleTimeSeconds,
      String vaultRegistryUrl, String tenantId) {
    super(sessionService, requestService, port, idleTimeSeconds,
        new AutoDecryptBodyExtractor(vaultRegistryUrl, tenantId));
  }
}
