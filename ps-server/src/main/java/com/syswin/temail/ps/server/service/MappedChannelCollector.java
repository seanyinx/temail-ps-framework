package com.syswin.temail.ps.server.service;

import com.syswin.temail.ps.server.entity.Session;
import io.netty.channel.Channel;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class MappedChannelCollector implements ChannelCollector {

  private final Map<Channel, Collection<Session>> channelSessionMap = new ConcurrentHashMap<>();

  @Override
  public boolean hasNoSession(Channel channel) {
    Collection<Session> sessions = channelSessionMap.get(channel);
    return sessions == null || sessions.isEmpty();
  }

  @Override
  public void addSession(String temail, String deviceId, Channel channel) {
    Collection<Session> sessions = channelSessionMap.computeIfAbsent(channel, s -> new ConcurrentLinkedQueue<>());
    sessions.add(new Session(temail, deviceId));
  }

  @Override
  public void removeSession(String temail, String deviceId, Channel channel) {
    Collection<Session> sessions = channelSessionMap.getOrDefault(channel, Collections.emptyList());
    sessions.removeIf(session -> temail.equals(session.getTemail()) && deviceId.equals(session.getDeviceId()));
    if (sessions.isEmpty()) {
      channelSessionMap.remove(channel);
      log.info("连接关闭前的请求堆栈信息",new RuntimeException(channel.toString()));
      channel.close();
    }
  }

  @Override
  public Collection<Session> removeChannel(Channel channel) {
    Collection<Session> sessions = channelSessionMap.remove(channel);
    channel.close();
    return sessions == null ? Collections.emptyList() : sessions;
  }
}
