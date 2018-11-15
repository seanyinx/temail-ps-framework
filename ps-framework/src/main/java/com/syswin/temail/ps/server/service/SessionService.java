package com.syswin.temail.ps.server.service;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.netty.channel.Channel;

/**
 * 会话管理服务
 *
 * @see AbstractSessionService
 */
public interface SessionService {

  /**
   * 客户端请求登录接口
   *
   * @param channel 当前客户端连接的通道
   * @param reqPacket 登录请求数据包
   */
  void login(Channel channel, CDTPPacket reqPacket);

  /**
   * 客户端请求前将通道绑定到会话中。
   *
   * @param channel 当前客户端连接的通道
   * @param reqPacket 登录请求数据包
   */
  void bind(Channel channel, CDTPPacket reqPacket);

  /**
   * 客户端请求登出接口
   *
   * @param channel 当前客户端连接的通道
   * @param reqPacket 登出请求数据包
   */
  void logout(Channel channel, CDTPPacket reqPacket);

  /**
   * 客户端通道断开(主动或者被动)时，需要处理的服务，如移除该通道相关的会话
   *
   * @param channel 当前客户端连接的通道
   */
  void disconnect(Channel channel);
}
