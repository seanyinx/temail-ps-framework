package com.syswin.temail.ps.server.service.channels.strategy.many2many;

import com.syswin.temail.ps.server.entity.Session;
import com.syswin.temail.ps.server.service.channels.strategy.ChannelManager;
import io.netty.channel.Channel;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class MappedChannelCollector implements ChannelManager {

  private final Map<Channel, Collection<Session>> channelSessionMap = new ConcurrentHashMap<>();

  @Override
  public boolean hasNoSession(Channel channel) {
    Collection<Session> sessions = channelSessionMap.get(channel);
    return sessions == null || sessions.isEmpty();
  }

  @Override
  public Collection<Session> addSession(String temail, String deviceId, Channel channel) {
    Collection<Session> sessions = channelSessionMap.computeIfAbsent(channel, s -> new ConcurrentLinkedQueue<>());
    sessions.add(new Session(temail, deviceId));
    return Collections.emptyList();
  }

  @Override
  public void removeSession(String temail, String deviceId, Channel channel) {
    Collection<Session> sessions = channelSessionMap.getOrDefault(channel, Collections.emptyList());
    sessions.removeIf(session -> temail.equals(session.getTemail()) && deviceId.equals(session.getDeviceId()));
    if (sessions.isEmpty()) {
      channelSessionMap.remove(channel);
    }
  }

  @Override
  public Collection<Session> removeChannel(Channel channel) {
    Collection<Session> sessions = channelSessionMap.remove(channel);
    return sessions == null ? Collections.emptyList() : sessions;
  }

  public Channel getChannel(String temail, String deviceId) {
    throw new UnsupportedOperationException();
  }

  public Iterable<Channel> getChannels(String temail) {
    throw new UnsupportedOperationException();
  }

}
