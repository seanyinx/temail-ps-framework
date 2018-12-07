package com.syswin.temail.ps.server.service.channels.strategy;

import com.syswin.temail.ps.server.entity.Session;
import io.netty.channel.Channel;
import java.util.Collection;

public interface ChannelManager {

  Collection<Session> addSession(String temail, String deviceId, Channel channel);

  void removeSession(String temail, String deviceId, Channel channel);

  Collection<Session> removeChannel(Channel channel);

  boolean hasSession(String temail, String deviceId, Channel channel) ;

  Iterable<Channel> getChannels(String temail) ;

  Iterable<Channel> getChannelsExceptSender(String receiver, String sender, String senderDeviceId) ;

}
