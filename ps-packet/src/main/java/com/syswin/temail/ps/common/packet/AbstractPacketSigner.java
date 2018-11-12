package com.syswin.temail.ps.common.packet;


import static com.syswin.temail.ps.common.entity.SignatureAlgorithm.NONE;
import static com.syswin.temail.ps.common.entity.SignatureAlgorithm.valueOf;
import static com.syswin.temail.ps.common.packet.PacketUtil.getUnsignData;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.SignatureAlgorithm;
import com.syswin.temail.ps.common.exception.PacketException;

/**
 * @author 姚华成
 * @date 2018-9-25
 */
public abstract class AbstractPacketSigner implements PacketSigner {

  /**
   * 根据用户ID获取对应密钥进行签名
   *
   * @param userId 账户ID e.g. temail地址
   * @param unsignData 用于签名的明文
   * @return 明文对应的签名Base64编码
   */
  public abstract String sign(String userId, String unsignData, SignatureAlgorithm signatureAlgorithm);

  @Override
  public void sign(CDTPPacket packet) {
    CDTPHeader header;
    SignatureAlgorithm algorithm;
    if (packet != null && (header = packet.getHeader()) != null &&
        (algorithm = valueOf(header.getSignatureAlgorithm())) != NONE) {
      String unsignData = getUnsignData(packet);
      try {
        header.setSignature(sign(header.getSender(), unsignData, algorithm));
      } catch (Exception e) {
        throw new PacketException("签名失败！", e, packet);
      }
    }
  }
}
