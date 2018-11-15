package com.syswin.temail.ps.server.service;

import com.syswin.temail.ps.server.entity.Session;
import io.netty.channel.Channel;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelHolder implements ChannelCollector {

  private final Map<String, Map<String, Channel>> sessionChannelMap = new ConcurrentHashMap<>();
  private final ChannelCollector channelCollector = new MappedChannelCollector();

  public Channel getChannel(String temail, String deviceId) {
    return sessionChannelMap.getOrDefault(temail, Collections.emptyMap()).get(deviceId);
  }

  public Iterable<Channel> getChannels(String temail) {
    return sessionChannelMap.getOrDefault(temail, Collections.emptyMap()).values();
  }

  @Override
  public boolean hasNoSession(Channel channel) {
    return channelCollector.hasNoSession(channel);
  }

  @Override
  public void addSession(String temail, String deviceId, Channel channel) {
    Map<String, Channel> deviceChannelMap = sessionChannelMap.computeIfAbsent(temail, s -> new ConcurrentHashMap<>());
    Channel oldChannel = deviceChannelMap.put(deviceId, channel);
    if (!channel.equals(oldChannel)) {
      if (oldChannel != null) {
        channelCollector.removeSession(temail, deviceId, oldChannel);
      }

      channelCollector.addSession(temail, deviceId, channel);
    }
  }

  @Override
  public void removeSession(String temail, String deviceId, Channel channel) {
    Map<String, Channel> deviceChannelMap = sessionChannelMap.get(temail);
    if (deviceChannelMap != null) {
      // 先移除sessionChannel
      removeSession(deviceChannelMap, temail, deviceId);

      // 再移除channelSession
      channelCollector.removeSession(temail, deviceId, channel);
    }
  }

  @Override
  public Collection<Session> removeChannel(Channel channel) {
    // 先移除channelSession
    Collection<Session> sessions = channelCollector.removeChannel(channel);
    // 再移除sessionChannel
    removeSessions(sessions);
    return sessions;
  }

  private void removeSessions(Iterable<Session> sessions) {
    for (Session session : sessions) {
      Map<String, Channel> deviceChannelMap = sessionChannelMap.get(session.getTemail());
      if (deviceChannelMap != null) {
        removeSession(deviceChannelMap, session.getTemail(), session.getDeviceId());
      }
    }
  }

  private void removeSession(Map<String, Channel> deviceChannelMap, String temail, String deviceId) {
    if (deviceChannelMap.size() > 1) {
      deviceChannelMap.remove(deviceId);
    } else {
      sessionChannelMap.remove(temail);
    }
  }
}
