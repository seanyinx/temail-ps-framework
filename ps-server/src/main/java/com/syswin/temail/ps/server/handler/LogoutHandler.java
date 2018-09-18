package com.syswin.temail.ps.server.handler;


import com.syswin.temail.ps.common.entity.CDTPPacket;

public interface LogoutHandler {

  void onSucceeded(CDTPPacket request, CDTPPacket response);
}
