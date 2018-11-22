package com.syswin.temail.ps.server.service.channels.strategy.one2one;

import com.syswin.temail.ps.server.entity.Session;
import io.netty.channel.Channel;
import io.netty.util.internal.ConcurrentSet;
import java.util.Set;
import lombok.Getter;

@Getter
public class ChannelSessionsBinder {

  private final Set<Session> sessions = new ConcurrentSet<>();

  private final Channel channel;

  public ChannelSessionsBinder(Channel channel) {
    this.channel = channel;
  }

  public void addSession(Session session){
    this.sessions.add(session);
  }

}
