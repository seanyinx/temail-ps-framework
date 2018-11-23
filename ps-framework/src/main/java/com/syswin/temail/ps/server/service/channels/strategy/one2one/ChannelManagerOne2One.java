package com.syswin.temail.ps.server.service.channels.strategy.one2one;

import static java.util.Collections.emptyList;
import com.syswin.temail.ps.server.entity.Session;
import com.syswin.temail.ps.server.service.channels.strategy.ChannelManager;
import io.netty.channel.Channel;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
      log.info("准备向已经已经建立的channel ：{}  添加session, deviceId : {}  temail : {}", channel, deviceId, temail);
      curBinder.getSessions().add(new Session(temail, deviceId));
      channelDevIdMap.put(channel, deviceId);
      Map<String, Channel> temailDevIdChannels = temail2Channel.computeIfAbsent(temail, t -> new ConcurrentHashMap<>());
      temailDevIdChannels.put(deviceId, channel);
      return emptyList();

    } else {
      log.info("准备向新建立的channel ：{}  添加session, deviceId : {}  temail : {}", channel.id(), deviceId, temail);
      ChannelSessionsBinder binder = new ChannelSessionsBinder(channel);
      binder.getSessions().add(new Session(temail, deviceId));
      ChannelSessionsBinder oldBinder = devIdBinderMap.put(deviceId, binder);
      Collection<Session> rmedSessions = oldBinder.getSessions();

      log.debug("从 channel -> devicdId 目前映射关系为：{}", channelDevIdMap.toString());
      channelDevIdMap.remove(oldBinder.getChannel());
      log.debug("从 channel -> devicdId 移除旧的key {} 后映射关系为 : ", oldBinder.getChannel(), channelDevIdMap.toString());
      channelDevIdMap.put(channel, deviceId);
      log.debug("向 channel -> devicdId 添加新的key-val {}:{} 后映射关系为 : {}", channel, deviceId, channelDevIdMap.toString());

      log.debug("从现有的 temail -> deviceId -> channel 映射关系 : {} 中移除因为新channel建立导致失效的就session : {}",
          temail2Channel.toString(), rmedSessions.toString());
      for (Session session : rmedSessions) {
        Optional.ofNullable(temail2Channel.get(session.getTemail())).ifPresent(t -> t.remove(session.getDeviceId()));
        Optional.ofNullable(temail2Channel.get(session.getTemail())).ifPresent(t -> {
          if (t.isEmpty()) {
            temail2Channel.remove(temail);
          }
        });
      }
      log.debug("移除失效session后 temail -> deviceId -> channel 的映射关系为：{}", temail2Channel.toString());

      Map<String, Channel> orDefault = temail2Channel.computeIfAbsent(temail, t -> new ConcurrentHashMap<>());
      orDefault.put(deviceId, channel);
      oldBinder.getChannel().close();
      log.debug("向 temail -> deviceId -> channel 添加新的映射关系 temail:{} deviceId:{} channel:{} 后:{}",
          temail2Channel.toString());

      log.debug("目前失效的session集合为:{}", rmedSessions.toString());
      rmedSessions.removeIf(s -> s.getTemail().equals(temail) && s.getDeviceId().equals(deviceId));
      log.debug("从失效的session集合中移除 temail:{} deviceId:{} 这个session后，剩余的 : {} 就是需要从channel-server中移除的注册数据！");
      return rmedSessions;
    }
  }

  @Override
  public Collection<Session> removeSession(String temail, String deviceId, Channel channel) {
    //移除session时，即使通道没有再绑定任何session了，也不能关闭通道，因为可能client在切换账户
    log.info("从channel : {} 移除session temail : {} deviceId : {}", channel, temail, deviceId);
    log.debug("移除前channel绑定的session集合为 : {}",
        Optional.ofNullable(devIdBinderMap.get(deviceId)).orElse(new ChannelSessionsBinder(null)).getSessions()
            .toString());

    Optional.ofNullable(devIdBinderMap.get(deviceId)).ifPresent(
        t -> t.getSessions().removeIf(s -> s.getDeviceId().equals(deviceId) && s.getTemail().equals(temail)));
    log.debug("移除后channel绑定的session集合为 : {}",
        Optional.ofNullable(devIdBinderMap.get(deviceId)).orElse(new ChannelSessionsBinder(null)).getSessions()
            .toString());

    log.debug("从 temila -> deviceId -> channel 映射{} 中移除 temail:{} deviceId:{} channel:{} 的绑定关系", temail2Channel, temail,
        deviceId, channel);
    Optional.ofNullable(temail2Channel.get(temail)).ifPresent(t -> t.remove(deviceId));
       log.debug("从 temila -> deviceId -> channel 映射{} 中移除 temail:{} deviceId:{} channel:{} 操作后的绑定关系", temail2Channel, temail,
        deviceId, channel);

    return Collections.singletonList(new Session(temail,deviceId));
  }

  @Override
  public Collection<Session> removeChannel(Channel channel) {
    log.info("移除channel : {}", channel);

    String devId = channelDevIdMap.remove(channel);
    log.debug("需要被移除的channel:{} 对应的设备号为 deviceId :{}", channel, devId);

    ChannelSessionsBinder rmedBinder = devIdBinderMap.remove(devId);
    log.debug("设备号 deviceId : {} 对应的ChannelSessionsBinder为：{}",devId, rmedBinder);

    Collection<Session> sessions = rmedBinder != null ? rmedBinder.getSessions() : emptyList();
    log.info("因为channel:{} 被移除导致需要从channel-server移除的session集合为：{}", sessions.toString());

    return sessions;
  }

  public Channel getChannel(String temail, String deviceId) {
    Map map = temail2Channel.get(temail);
    return map.get(deviceId) == null ? null : (Channel) map.get(deviceId);
  }

  public Iterable<Channel> getChannels(String temail) {
    Map map = temail2Channel.get(temail);
    return map == null ? emptyList() : map.values();
  }
}
