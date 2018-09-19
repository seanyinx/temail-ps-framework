package com.syswin.temail.ps.server.service;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.netty.channel.Channel;

public interface SessionService {

  void login(Channel channel, CDTPPacket reqPacket);

  void logout(Channel channel, CDTPPacket reqPacket);

  boolean isLoggedIn(Channel channel, CDTPPacket packet);

  void disconnect(Channel channel);
}
