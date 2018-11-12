package com.syswin.temail.ps.common.packet;

import static com.syswin.temail.ps.common.entity.DataEncryptType.NONE;
import static com.syswin.temail.ps.common.entity.DataEncryptType.valueOf;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.DataEncryptType;
import com.syswin.temail.ps.common.exception.PacketException;
import com.syswin.temail.ps.common.utils.StringUtil;

/**
 * @author 姚华成
 * @date 2018-11-05
 */
public abstract class AbstractPacketEncryptor implements PacketEncryptor {

  /**
   * 根据用户ID获取对应密钥进行加密
   *
   * @param userId 账户ID e.g. temail地址
   * @param data 待加密的明文
   * @return 明文加密后对应的密文Base64编码
   */
  public abstract byte[] encrypt(String userId, byte[] data, DataEncryptType dataEncryptType);

  public abstract byte[] encryptWithPubKey(String publicKey, byte[] data, DataEncryptType dataEncryptType);

  @Override
  public void encrypt(CDTPPacket packet) {
    byte[] data;
    CDTPHeader header;
    DataEncryptType dataEncryptType;
    if (packet != null && (header = packet.getHeader()) != null
        && (dataEncryptType = valueOf(header.getDataEncryptionMethod())) != NONE &&
        ((data = packet.getData()) != null && data.length != 0) &&
        StringUtil.hasText(header.getReceiver())) {
      try {
        packet.setData(encrypt(header.getReceiver(), data, dataEncryptType));
      } catch (Exception e) {
        String receiverPK;
        if (StringUtil.hasText(receiverPK = header.getReceiverPK())) {
          packet.setData(encryptWithPubKey(receiverPK, data, dataEncryptType));
        } else {
          throw new PacketException("加密失败：无法使用kms进行加密，packet里也没有提供receiverPK字段", e, packet);
        }
      }
    }
  }

}
