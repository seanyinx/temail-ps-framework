package com.syswin.temail.ps.common.packet;

import static com.syswin.temail.ps.common.entity.DataEncryptType.NONE;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.DataEncryptType;

/**
 * @author 姚华成
 * @date 2018-11-06
 */
public class NoOpPacketEncryptor implements PacketEncryptor {

  @Override
  public void encrypt(CDTPPacket packet) {
  }

  @Override
  public void encrypt(CDTPPacket packet, DataEncryptType dataEncryptType) {
  }

  @Override
  public DataEncryptType getDefaultDataEncryptType() {
    return NONE;
  }
}
