package com.syswin.temail.ps.common.entity;

import com.syswin.temail.ps.common.exception.PacketException;
import lombok.Getter;

/**
 * @author 姚华成
 * @date 2018-9-20
 */
@Getter
public enum SignatureAlgorithm {
  NONE(0),
  RSA2048(1),
  ECC512(2),
  SM2(3),
  ;

  public static final int NONE_CODE = NONE.code;
  public static final int RSA2048_CODE = RSA2048.code;
  public static final int ECC512_CODE = ECC512.code;
  public static final int SM2_CODE = SM2.code;
  private final int code;

  SignatureAlgorithm(int code) {
    this.code = code;
  }

  public static SignatureAlgorithm valueOf(int code) {
    for (SignatureAlgorithm value : values()) {
      if (value.code == code) {
        return value;
      }
    }
    throw new PacketException("不支持的签名算法：" + code);
  }
}
