package com.syswin.temail.ps.server.service.channels.strategy.one2one;

import static java.util.Collections.emptyMap;
import com.syswin.temail.ps.server.entity.Session;
import com.syswin.temail.ps.server.service.channels.strategy.ChannelManager;
import io.netty.channel.Channel;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChannelManagerOne2One implements ChannelManager {

  private final Map<String, Map<String, Channel>> temail2Channel = new ConcurrentHashMap<>();
  private final BiConsumer<Channel, Session> existingChannelHandler;
  private final DeviceChannelHolder deviceChannelHolder = new DeviceChannelHolder();

  public ChannelManagerOne2One() {
    existingChannelHandler = (channel, session) -> {
      Map<String, Channel> temailDevIdChannels = temail2Channel
          .computeIfAbsent(session.getTemail(), t -> new ConcurrentHashMap<>());
      temailDevIdChannels.put(session.getDeviceId(), channel);
    };
  }

  @Override
  public Collection<Session> addSession(String temail, String deviceId, Channel channel) {
    return deviceChannelHolder.addSession(temail, deviceId, channel,
        existingChannelHandler,
        (newChannel, expiredSessions) -> replaceExistingChannelFromTheSameDevice(temail, deviceId, newChannel,
            expiredSessions));
  }

  private void replaceExistingChannelFromTheSameDevice(String temail,
      String deviceId,
      Channel channel,
      Collection<Session> expiredSessions) {

    removeExpiredSessions(expiredSessions);
    Map<String, Channel> deviceIdChannelMap = temail2Channel.computeIfAbsent(temail, t -> new ConcurrentHashMap<>());
    deviceIdChannelMap.put(deviceId, channel);
  }

  @Override
  public void removeSession(String temail, String deviceId, Channel channel) {
    //移除session时，即使通道没有再绑定任何session了，也不能关闭通道，因为可能client在切换账户
    log.debug("Removed temail {} device {} from channel {} mapping", temail, deviceId, channel);
    deviceChannelHolder.removeSession(temail, deviceId);
    temail2Channel.getOrDefault(temail, emptyMap()).remove(deviceId);
  }

  @Override
  public Collection<Session> removeChannel(Channel channel) {
    Collection<Session> sessions = deviceChannelHolder.removeChannel(channel);

    removeExpiredSessions(sessions);
    log.debug("Removed all sessions {} on channel {}", sessions, channel);
    return sessions;
  }

  private void removeExpiredSessions(Collection<Session> sessionsExpired) {
    for (Session session : sessionsExpired) {
      Map<String, Channel> deviceIdToChannelMap = temail2Channel.getOrDefault(session.getTemail(), emptyMap());
      deviceIdToChannelMap.remove(session.getDeviceId());

      if (deviceIdToChannelMap.isEmpty()) {
        temail2Channel.remove(session.getTemail());
      }
    }
  }

  public boolean hasSession(String temail, String deviceId, Channel channel) {
    return channel == temail2Channel.getOrDefault(temail, emptyMap()).get(deviceId);
  }

  public Iterable<Channel> getChannels(String temail) {
    return temail2Channel.getOrDefault(temail, emptyMap()).values();
  }

  public Iterable<Channel> getChannelsExceptSenderN(String receiver, String sender, String senderDeviceId) {
    Map<String, Channel> deviceId2Channel = temail2Channel.getOrDefault(receiver, emptyMap());
    List<Channel> result = deviceId2Channel.entrySet().stream().filter(en -> {
      return
          //false - 不要的 ： sender == recevier 并且 设备id一致！
          !((senderDeviceId.equals(en.getKey())) && (receiver.equals(sender)));
    }).map(en -> {
      return en.getValue();
    }).collect(
        Collectors.toList());
    log.debug("Receiver: {} , deviceId2Channel: {} , after filetered by sender:{}, senderDeviceId:{} is : {}",
        receiver, sampleStr(deviceId2Channel), sender, senderDeviceId, sampleStr(result));

    return result;
  }

  private Object sampleStr(List<Channel> result) {
    return result.stream().map(channel -> channel.id().toString()).collect(Collectors.toList()).toString();
  }

  private String sampleStr(Map<String, Channel> deviceId2Channel) {
    return deviceId2Channel.entrySet().stream().map(en -> en.getKey() + ":" + en.getValue().id().toString())
        .collect(Collectors.toList()).toString();
  }

}
