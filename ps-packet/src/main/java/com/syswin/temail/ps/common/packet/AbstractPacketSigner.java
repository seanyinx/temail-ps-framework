package com.syswin.temail.ps.common.packet;


import static com.syswin.temail.ps.common.entity.SignatureAlgorithm.NONE;
import static com.syswin.temail.ps.common.entity.SignatureAlgorithm.NONE_CODE;
import static com.syswin.temail.ps.common.packet.PacketUtil.getUnsignData;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.SignatureAlgorithm;

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
    sign(packet, getDefaultAlgorithm());
  }

  @Override
  public void sign(CDTPPacket packet, SignatureAlgorithm algorithm) {
    CDTPHeader header;
    if (algorithm != NONE &&
        packet != null &&
        (header = packet.getHeader()) != null) {
      String unsignData = getUnsignData(packet);
      try {
        header.setSignature(sign(header.getSender(), unsignData, algorithm));
        header.setSignatureAlgorithm(algorithm.getCode());
      } catch (Exception e) {
        // 无法获取私钥进行签名，则不签名
        header.setSignature(null);
        header.setSignatureAlgorithm(NONE_CODE);
      }
    }
  }
}
