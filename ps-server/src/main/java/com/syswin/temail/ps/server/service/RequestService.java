package com.syswin.temail.ps.server.service;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.function.Consumer;

public interface RequestService {

  void handleRequest(CDTPPacket reqPacket, Consumer<CDTPPacket> responseHandler);
}
