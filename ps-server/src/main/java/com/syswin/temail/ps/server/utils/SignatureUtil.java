package com.syswin.temail.ps.server.utils;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.SignatureAlgorithm;

/**
 * @author 姚华成
 * @date 2018-9-20
 */
public class SignatureUtil {

  public static void resetSignature(CDTPPacket reqPacket) {
    // 请求的数据可能签名，而返回的数据没有签名，需要清除加密标识
    reqPacket.getHeader().setSignatureAlgorithm(SignatureAlgorithm.NONE_CODE);
    reqPacket.getHeader().setSignature(null);
  }

}
