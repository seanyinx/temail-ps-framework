package com.syswin.temail.ps.common.packet;

import static com.syswin.temail.ps.common.entity.SignatureAlgorithm.NONE;
import static com.syswin.temail.ps.common.entity.SignatureAlgorithm.NONE_CODE;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.SignatureAlgorithm;

/**
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

    @Override
    public void sign(CDTPPacket packet, SignatureAlgorithm algorithm) {
      sign(packet);
    }
  };

  default void sign(CDTPPacket packet) {
  }

  default void sign(CDTPPacket packet, SignatureAlgorithm algorithm) {
  }

  /**
   * 获取当前签名生成器使用的签名算法
   *
   * @return 签名算法的编号
   */
  default SignatureAlgorithm getDefaultAlgorithm() {
    return NONE;
  }

  default int getDefaultAlgorithmCode() {
    return getDefaultAlgorithm().getCode();
  }
}
