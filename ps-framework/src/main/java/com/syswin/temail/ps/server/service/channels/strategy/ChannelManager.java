package com.syswin.temail.ps.server.service.channels.strategy;

import com.syswin.temail.ps.server.entity.Session;
import io.netty.channel.Channel;
import java.util.Collection;

public interface ChannelManager {

  boolean hasNoSession(Channel channel);

  Collection<Session> addSession(String temail, String deviceId, Channel channel);

  Collection<Session> removeSession(String temail, String deviceId, Channel channel);

  Collection<Session> removeChannel(Channel channel);

  public Channel getChannel(String temail, String deviceId) ;

  public Iterable<Channel> getChannels(String temail) ;

}
