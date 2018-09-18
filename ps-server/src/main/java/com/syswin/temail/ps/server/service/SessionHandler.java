package com.syswin.temail.ps.server.service;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.server.entity.Session;
import com.syswin.temail.ps.server.handler.LoginHandler;
import com.syswin.temail.ps.server.handler.LogoutHandler;
import java.util.Collection;

public interface SessionHandler {

  void login(CDTPPacket packet, LoginHandler loginHandler);

  void logout(CDTPPacket packet, LogoutHandler logoutHandler);

  void disconnect(Collection<Session> sessions);
}
