package com.syswin.temail.ps.server.service.channels.strategy.one2one;

import com.syswin.temail.ps.server.entity.Session;
import com.syswin.temail.ps.server.service.channels.strategy.ChannelManager;
import io.netty.channel.Channel;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 */
public class ChannelManagerOne2One implements ChannelManager {

  private final Map<String, ChannelSessionsBinder> devIdBinderMap = new ConcurrentHashMap<>();

  private final Map<Channel, String> channelDevIdMap = new ConcurrentHashMap<>();

  private final Map<String, Map<String, Channel>> temail2Channel = new ConcurrentHashMap<>();

  @Override
  public boolean hasNoSession(Channel channel) {
    return false;
  }


  /**
   * @param temail
   * @param deviceId
   * @param channel
   * @return
   */
  @Override
  public Collection<Session> addSession(String temail, String deviceId, Channel channel) {
    ChannelSessionsBinder curBinder = devIdBinderMap
        .computeIfAbsent(deviceId, t -> new ChannelSessionsBinder(channel));

    if (channel.equals(curBinder.getChannel())) {
      curBinder.getSessions().add(new Session(temail, deviceId));
      channelDevIdMap.put(channel, deviceId);
      Map<String, Channel> temailDevIdChannels = temail2Channel.computeIfAbsent(temail, t -> new ConcurrentHashMap<>());
      temailDevIdChannels.put(deviceId, channel);
      return Collections.emptyList();

    } else {
      ChannelSessionsBinder binder = new ChannelSessionsBinder(channel);
      binder.getSessions().add(new Session(temail, deviceId));
      ChannelSessionsBinder oldBinder = devIdBinderMap.put(deviceId, binder);
      Collection<Session> rmedSessions = oldBinder.getSessions();
      channelDevIdMap.remove(oldBinder.getChannel());
      channelDevIdMap.put(channel, deviceId);
      for (Session session : rmedSessions) {
        Optional.ofNullable(temail2Channel.get(session.getTemail())).ifPresent(t -> t.remove(session.getDeviceId()));
        Optional.ofNullable(temail2Channel.get(session.getTemail())).ifPresent(t -> {
          if (t.isEmpty()) {
            temail2Channel.remove(temail);
          }
        });
      }
      Map<String, Channel> orDefault = temail2Channel.computeIfAbsent(temail, t -> new ConcurrentHashMap<>());
      orDefault.put(deviceId, channel);
      oldBinder.getChannel().close();
      rmedSessions.removeIf(s -> s.getTemail().equals(temail) && s.getDeviceId().equals(deviceId));
      return rmedSessions;
    }
  }

  @Override
  public void removeSession(String temail, String deviceId, Channel channel) {
    devIdBinderMap.get(deviceId).getSessions()
        .removeIf(s -> s.getDeviceId().equals(deviceId) && s.getTemail().equals(temail));
    Optional.ofNullable(temail2Channel.get(temail)).ifPresent(t -> t.remove(deviceId));
  }

  @Override
  public Collection<Session> removeChannel(Channel channel) {
    String devId = channelDevIdMap.remove(channel);
    ChannelSessionsBinder rmedBinder = devIdBinderMap.remove(devId);
    return rmedBinder == null ? rmedBinder.getSessions() : Collections.emptyList();
  }

  public Channel getChannel(String temail, String deviceId) {
    Map map = temail2Channel.get(temail);
    return map.get(deviceId) == null ? null : (Channel) map.get(deviceId);
  }

  public Iterable<Channel> getChannels(String temail) {
    Map map = temail2Channel.get(temail);
    return map == null ? Collections.emptyList() : map.values();
  }
}
