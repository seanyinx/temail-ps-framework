package com.syswin.temail.ps.common.packet;

import com.syswin.temail.ps.common.entity.CDTPPacket;

/**
 * CDTPPacket签名验签器接口<P>
 * NoOp是一个什么都不做的实现实例
 *
 * @author 姚华成
 * @date 2018-11-06
 */
public interface PacketVerifier {

  PacketVerifier NoOp = packet -> true;

  boolean verify(CDTPPacket packet);
}
