package com.syswin.temail.ps.common.packet;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.SignatureAlgorithm;

/**
 * @author 姚华成
 * @date 2018-11-06
 */
public interface PacketSigner {

  void sign(CDTPPacket packet);

  void sign(CDTPPacket packet, SignatureAlgorithm algorithm);

  /**
   * 获取当前签名生成器使用的签名算法
   *
   * @return 签名算法的编号
   */
  SignatureAlgorithm getDefaultAlgorithm();

  default int getDefaultAlgorithmCode() {
    return getDefaultAlgorithm().getCode();
  }
}
