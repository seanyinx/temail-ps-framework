package com.syswin.temail.ps.server.service.channels.strategy.one2one;

import static java.util.Collections.emptyList;

import com.syswin.temail.ps.server.entity.Session;
import io.netty.channel.Channel;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class DeviceChannelHolder {

  // very unlikely to have concurrent device IDs situation, since a single device has to make 2 connections at the same time
  // and requests on a single channel are handled serially.
  private final Map<Channel, String> channelDevIdMap = new ConcurrentHashMap<>();
  private final Map<String, ChannelSessionsBinder> devIdBinderMap = new ConcurrentHashMap<>();

  Collection<Session> addSession(String temail,
      String deviceId,
      Channel channel,
      BiConsumer<Channel, Session> existingChannelHandler,
      BiConsumer<Channel, Collection<Session>> replacedChannelHandler) {

    ChannelSessionsBinder currentBinder = devIdBinderMap
        .computeIfAbsent(deviceId, t -> new ChannelSessionsBinder(channel));

    if (channel.equals(currentBinder.getChannel())) {
      Session session = currentBinder.addSession(temail, deviceId);
      channelDevIdMap.put(channel, deviceId);
      existingChannelHandler.accept(channel, session);
      log.debug("Added temail {} device {} channel {} mapping", temail, deviceId, channel);
      return Collections.emptyList();
    }

    bindToNewChannel(temail, deviceId, channel);

    Channel oldChannel = currentBinder.getChannel();
    channelDevIdMap.remove(oldChannel);
    channelDevIdMap.put(channel, deviceId);

    // close before cleaning up temail:channel mapping to avoid temail binding on old channel again on request
    oldChannel.close();
    log.info("Closed and replaced channel {} with {} in temail {} device {} mapping due to new connection from this device",
        oldChannel,
        channel,
        temail,
        deviceId);

    Collection<Session> sessionsExpired = currentBinder.getSessions();
    sessionsExpired.removeIf(session -> isSameSession(temail, deviceId, session));
    replacedChannelHandler.accept(channel, sessionsExpired);

    return sessionsExpired;
  }

  private Session bindToNewChannel(String temail, String deviceId, Channel channel) {
    ChannelSessionsBinder binder = new ChannelSessionsBinder(channel);
    Session session = binder.addSession(temail, deviceId);
    devIdBinderMap.put(deviceId, binder);
    return session;
  }

  void removeSession(String temail, String deviceId) {
    ChannelSessionsBinder binder = devIdBinderMap.get(deviceId);
    if (binder != null) {
      binder.getSessions()
          .removeIf(session -> isSameSession(temail, deviceId, session));
    }
  }

  Collection<Session> removeChannel(Channel channel) {
    String devId = channelDevIdMap.remove(channel);
    ChannelSessionsBinder binder = devIdBinderMap.remove(devId);

    return binder != null ? binder.getSessions() : emptyList();
  }

  private boolean isSameSession(String temail, String deviceId, Session session) {
    return session.getDeviceId().equals(deviceId)
        && session.getTemail().equals(temail);
  }
}