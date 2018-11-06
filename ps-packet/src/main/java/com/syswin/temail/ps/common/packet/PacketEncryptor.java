package com.syswin.temail.ps.common.packet;

import static com.syswin.temail.ps.common.entity.DataEncryptType.NONE;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.DataEncryptType;

/**
 * @author 姚华成
 * @date 2018-11-06
 */
public interface PacketEncryptor {

  PacketEncryptor NoOp = new PacketEncryptor() {
  };

  default void encrypt(CDTPPacket packet) {
  }

  default void encrypt(CDTPPacket packet, DataEncryptType dataEncryptType) {
  }

  default DataEncryptType getDefaultDataEncryptType() {
    return NONE;
  }

  default int getDefaultDataEncryptionMethod() {
    return getDefaultDataEncryptType().getCode();
  }
}
