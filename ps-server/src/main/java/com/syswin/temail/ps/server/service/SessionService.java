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
   * 客户端请求登出接口
   *
   * @param channel 当前客户端连接的通道
   * @param reqPacket 登出请求数据包
   */
  void logout(Channel channel, CDTPPacket reqPacket);

  /**
   * 判断当前请求数据包的用户是否已经登录。
   *
   * @param channel 当前客户端连接的通道
   * @param reqPacket 业务请求数据包
   * @return 用户已登录则返回true，用户没有使用则返回false
   */
  boolean isLoggedIn(Channel channel, CDTPPacket reqPacket);

  /**
   * 客户端通道断开(主动或者被动)时，需要处理的服务，如移除该通道相关的会话
   * @param channel 当前客户端连接的通道
   */
  void disconnect(Channel channel);
}
