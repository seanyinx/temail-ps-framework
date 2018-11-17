package com.syswin.temail.ps.server.utils;

import com.syswin.temail.ps.common.entity.CDTPPacket;

public class SignatureUtil {

  private static final int NONE = 0;

  public static void resetSignature(CDTPPacket reqPacket) {
    // 请求的数据可能签名，而返回的数据没有签名，需要清除加密标识
    reqPacket.getHeader().setSignatureAlgorithm(NONE);
    reqPacket.getHeader().setSignature(null);
  }

}
