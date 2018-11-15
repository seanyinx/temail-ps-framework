package com.syswin.temail.ps.server.service;

import com.syswin.temail.ps.server.entity.Session;
import io.netty.channel.Channel;
import java.util.Collection;

public interface ChannelCollector {

  boolean hasNoSession(Channel channel);

  void addSession(String temail, String deviceId, Channel channel);

  void removeSession(String temail, String deviceId, Channel channel);

  Collection<Session> removeChannel(Channel channel);
}
