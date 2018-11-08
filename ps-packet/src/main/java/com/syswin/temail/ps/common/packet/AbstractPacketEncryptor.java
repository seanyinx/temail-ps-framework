package com.syswin.temail.ps.common.packet;

import static com.syswin.temail.ps.common.entity.DataEncryptType.NONE;
import static com.syswin.temail.ps.common.entity.DataEncryptType.NONE_CODE;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.DataEncryptType;
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
    encrypt(packet, getDefaultDataEncryptType());
  }

  @Override
  public void encrypt(CDTPPacket packet, DataEncryptType dataEncryptType) {
    byte[] data;
    CDTPHeader header;
    if (packet != null && (header = packet.getHeader()) != null) {
      if (dataEncryptType != NONE &&
          ((data = packet.getData()) != null && data.length != 0) &&
          StringUtil.hasText(header.getReceiver())) {
        try {
          packet.setData(encrypt(header.getReceiver(), data, dataEncryptType));
          header.setDataEncryptionMethod(dataEncryptType.getCode());
        } catch (Exception e) {
          String receiverPK;
          if (StringUtil.hasText(receiverPK = header.getReceiverPK())) {
            packet.setData(encryptWithPubKey(receiverPK, data, dataEncryptType));
            header.setDataEncryptionMethod(dataEncryptType.getCode());
          } else {
            // 没有接收的公钥，不加密
            header.setDataEncryptionMethod(NONE_CODE);
          }
        }
      } else {
        header.setDataEncryptionMethod(NONE_CODE);
      }
    }
  }
}
