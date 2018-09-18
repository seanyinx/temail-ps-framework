package com.syswin.temail.ps.server.handler;


import com.syswin.temail.ps.common.entity.CDTPPacket;

public interface LoginHandler {

  void onSucceed(CDTPPacket request, CDTPPacket response);

  void onFailed(CDTPPacket response);
}
