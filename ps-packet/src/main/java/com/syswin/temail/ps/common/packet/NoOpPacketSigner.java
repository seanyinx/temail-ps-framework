package com.syswin.temail.ps.common.packet;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.SignatureAlgorithm;

/**
 * @author 姚华成
 * @date 2018-11-06
 */
public class NoOpPacketSigner implements PacketSigner {

  @Override
  public void sign(CDTPPacket packet) {
  }

  @Override
  public void sign(CDTPPacket packet, SignatureAlgorithm algorithm) {
  }

  @Override
  public SignatureAlgorithm getDefaultAlgorithm() {
    return SignatureAlgorithm.NONE;
  }
}
