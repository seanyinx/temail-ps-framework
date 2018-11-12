package com.syswin.temail.ps.common.packet;

import static com.syswin.temail.ps.common.entity.SignatureAlgorithm.NONE_CODE;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;

/**
 * CDTPPacket签名器接口<P>
 * NoOp是一个什么都不做的实现实例
 *
 * @author 姚华成
 * @date 2018-11-06
 */
public interface PacketSigner {

  PacketSigner NoOp = new PacketSigner() {
  };

  PacketSigner NonePacketSigner = new PacketSigner() {
    @Override
    public void sign(CDTPPacket packet) {
      CDTPHeader header = packet.getHeader();
      header.setSignatureAlgorithm(NONE_CODE);
      header.setSignature(null);
    }
  };

  default void sign(CDTPPacket packet) {
  }
}
