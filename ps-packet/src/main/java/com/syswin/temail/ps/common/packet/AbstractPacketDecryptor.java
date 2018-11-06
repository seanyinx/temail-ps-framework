package com.syswin.temail.ps.common.packet;

import static com.syswin.temail.ps.common.entity.DataEncryptType.NONE;
import static com.syswin.temail.ps.common.entity.DataEncryptType.NONE_CODE;
import static com.syswin.temail.ps.common.entity.DataEncryptType.valueOf;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.DataEncryptType;

/**
 * @author 姚华成
 * @date 2018-11-05
 */
public abstract class AbstractPacketDecryptor implements PacketDecryptor {

  /**
   * 根据用户ID获取对应密钥进行解密
   *
   * @param userId 账户ID e.g. temail地址
   * @param encryptedData 待解密的密文Base64编码
   * @return 解密后的明文
   */
  public abstract byte[] decrypt(String userId, byte[] encryptedData, DataEncryptType dataEncryptType);

  @Override
  public void decrypt(CDTPPacket packet) {
    byte[] data;
    DataEncryptType dataEncryptType;
    CDTPHeader header;
    if (packet != null &&
        ((data = packet.getData()) != null && data.length != 0) &&
        (header = packet.getHeader()) != null &&
        (dataEncryptType = valueOf(header.getDataEncryptionMethod())) != NONE) {
      packet.setData(decrypt(header.getReceiver(), data, dataEncryptType));
      header.setDataEncryptionMethod(NONE_CODE);
    }
  }
}
