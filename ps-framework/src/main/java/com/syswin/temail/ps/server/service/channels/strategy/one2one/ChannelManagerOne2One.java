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
 * with this strategy , one gateway server instance will hold only one channel per client.
 */
public class ChannelManagerOne2One implements ChannelManager {


  /**
   * 使用情景：
   * <p>
   * dev -> channel : 用于保证client -> channel 唯一
   * <p>
   * temail -> channnel : 发送消息使用
   * <p>
   * channel -> Collection[sessions] : 用于清理channel时候使用
   */

  private final Map<String, ChannelSessionsBinder> devIdBinderMap = new ConcurrentHashMap<>();

  private final Map<Channel, String> channelDevIdMap = new ConcurrentHashMap<>();

  private final Map<String, Map<String, Channel>> temail2Channel = new ConcurrentHashMap<>();

  @Override
  public boolean hasNoSession(Channel channel) {
    return false;
  }

  @Override
  public Collection<Session> addSession(String temail, String deviceId, Channel channel) {

    ChannelSessionsBinder oldBinder = devIdBinderMap.get(deviceId);

    if (oldBinder == null) {
      //该客户端之前没有在当前server上创建链接

      //用以控制 client - server 的channe 唯一
      ChannelSessionsBinder binder = new ChannelSessionsBinder(channel);
      binder.getSessions().add(new Session(temail, deviceId));
      devIdBinderMap.put(deviceId, binder);

      //根据channel 查找 设备号， 以便清理某个channel下的全部session时使用
      channelDevIdMap.put(channel, deviceId);

      //记录 temail -> 设备号 -> channel 的数据，以便多个客户端同时接受消息
      Map<String, Channel> temailDevIdChannels = temail2Channel.getOrDefault(temail, new ConcurrentHashMap<>());
      temailDevIdChannels.put(deviceId, channel);

      //此时没有多余的session需要清理！
      return Collections.emptyList();

    } else {

      if (channel.equals(oldBinder.getChannel())) {

        //该客户端到当前server的channel之前就存在，并且本次使用的就是这个， 那么可能之前就有多个session已经绑定到这个channel上了。

        //devIdBinderMap（devId - channel）不必重新处理绑定关系了，因为已经绑定了。 只需要添加session即可
        oldBinder.getSessions().add(new Session(temail, deviceId));

        //channelDevIdMap 之前也绑定了 channel 到 devId的关系，也不必再处理
        //channelDevIdMap.put(channel, deviceId);

        //temail2Channel - 虽然channel是同一个，但是temail不一定是同一个，所以一还需要绑定 ： 即多个不同账户通过同一个设备登录
        Map<String, Channel> temailDevIdChannels = temail2Channel.getOrDefault(temail, new ConcurrentHashMap<>());
        temailDevIdChannels.put(deviceId, channel);   //如果devId相同只是做了一个 k 和 v都相同的替换而已

        //因为通道是同一个，session 没有失效，不需要清理。
        return Collections.emptyList();

      } else {

        //如果客户端重新创建了一个通道

        //devIdBinderMap
        //建立新的 ddevId->channel的绑定关系
        ChannelSessionsBinder binder = new ChannelSessionsBinder(channel);
        binder.getSessions().add(new Session(temail, deviceId));
        devIdBinderMap.put(deviceId, binder);

        //旧的channel绑定的sessions除了当前绑定的全部清理
        Collection<Session> rmedSessions = oldBinder.getSessions();

        //channelDevIdMap
        //旧的绑定关系要删除
        channelDevIdMap.remove(oldBinder.getChannel());

        //绑定新的channel 到 devId 之间的关系！
        channelDevIdMap.put(channel, deviceId);

        //temail2Channel
        //删除旧的 temail -> devId -> channel 之间定关系, 因为某一个channel的失效代表 N个session失效
        //那么就需要从temailDevIdChannels中删除全部的旧的对应关系
        for (Session session : rmedSessions) {
          Optional.ofNullable(temail2Channel.get(session.getTemail())).ifPresent(t -> t.remove(session.getDeviceId()));
          Optional.ofNullable(temail2Channel.get(session.getTemail())).ifPresent(t -> {
            if (t.isEmpty()) {
              temail2Channel.remove(temail);
            }
          });
        }

        //建立新的对应关系
        Map<String, Channel> orDefault = temail2Channel.getOrDefault(temail, new ConcurrentHashMap<>());
        orDefault.put(deviceId, channel);

        //最后关闭channel
        oldBinder.getChannel().close();

        //和channel-server同步的时候，不需要将当前session删除
        rmedSessions.removeIf(s -> s.getTemail().equals(temail) && s.getDeviceId().equals(deviceId));
        return rmedSessions;
      }
    }
  }

  @Override
  public void removeSession(String temail, String deviceId, Channel channel) {

    //只是解除绑定关系 - channel 并不下线！

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

  public Channel getChannel(String temail, String deviceId){

    Map map = temail2Channel.get(temail);

    return map.get(deviceId) == null ? null : (Channel) map.get(deviceId);

  }

  public Iterable<Channel> getChannels(String temail){
    Map map = temail2Channel.get(temail);
    return map== null ? Collections.emptyList() : map.values();
  }

}
