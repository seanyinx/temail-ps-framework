package com.syswin.temail.ps.common.packet;


import static com.syswin.temail.ps.common.entity.SignatureAlgorithm.NONE;
import static com.syswin.temail.ps.common.entity.SignatureAlgorithm.valueOf;
import static com.syswin.temail.ps.common.utils.PacketUtil.getUnsignData;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.SignatureAlgorithm;

/**
 * @author 姚华成
 * @date 2018-9-25
 */
public abstract class AbstractPacketVerifier implements PacketVerifier {

  /**
   * 根据用户ID获取对应密钥进行验签
   *
   * @param userId 账户ID e.g. temail地址
   * @param unsignData 用于签名的明文
   * @param signature 待验签的签名Base64编码
   * @return 签名是否与明文匹配
   */
  public abstract boolean verify(String userId, String unsignData, String signature,
      SignatureAlgorithm algorithm);

  @Override
  public boolean verify(CDTPPacket packet) {
    CDTPHeader header;
    SignatureAlgorithm algorithm;
    if (packet != null &&
        (header = packet.getHeader()) != null &&
        (algorithm = valueOf(header.getSignatureAlgorithm())) != NONE) {
      String unsignData = getUnsignData(packet);
      return verify(header.getSender(), unsignData, header.getSignature(), algorithm);
    }
    return false;
  }
}
