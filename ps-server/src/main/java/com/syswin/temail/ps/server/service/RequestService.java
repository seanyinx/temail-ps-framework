package com.syswin.temail.ps.server.service;

import com.syswin.temail.ps.common.entity.CDTPPacket;

public interface RequestService {

  void handleRequest(CDTPPacket reqPacket, CDTPPacket respPacket);
}
