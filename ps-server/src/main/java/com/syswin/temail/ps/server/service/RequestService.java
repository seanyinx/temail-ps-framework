package com.syswin.temail.ps.server.service;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.function.Consumer;

/**
 * 请求处理服务
 */
public interface RequestService {

  /**
   * 请求处理方法
   *
   * @param reqPacket 请求的数据包
   * @param responseHandler 响应的回写处理器，在业务请求处理完后，需要调用responseHandler.accept方法，将响应数据返回到客户端
   */
  default void handleRequest(CDTPPacket reqPacket, Consumer<CDTPPacket> responseHandler) {
    responseHandler.accept(reqPacket);
  }
}
