package com.syswin.temail.ps.common.packet;

import com.syswin.temail.ps.common.entity.CDTPPacket;

/**
 * CDTPPacket解密器接口<P>
 * NoOp是一个什么都不做的实现实例
 *
 * @author 姚华成
 * @date 2018-11-06
 */
public interface PacketDecryptor {

  PacketDecryptor NoOp = packet -> {
  };

  void decrypt(CDTPPacket packet);
}
