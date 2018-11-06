package com.syswin.temail.ps.common.packet;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.DataEncryptType;

/**
 * @author 姚华成
 * @date 2018-11-06
 */
public interface PacketEncryptor {

  void encrypt(CDTPPacket packet);

  void encrypt(CDTPPacket packet, DataEncryptType dataEncryptType);

  DataEncryptType getDefaultDataEncryptType();

  default int getDefaultDataEncryptionMethod() {
    return getDefaultDataEncryptType().getCode();
  }
}
