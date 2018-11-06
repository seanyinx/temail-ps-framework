package com.syswin.temail.ps.common.entity;

import com.syswin.temail.ps.common.exception.PacketException;
import lombok.Getter;

/**
 * @author 姚华成
 * @date 2018-9-20
 */
@Getter
public enum DataEncryptType {
  NONE(0),
  RSA_RECEIVER_PUB(1),
  RSA_SENDER_PUB(2),
  AES_CBC_32(3),
  ECC_RECEIVER_PUB(4),
  ECC_SENDER_PUB(5),
  ;
  public static final int NONE_CODE = NONE.code;
  public static final int RSA_RECEIVER_PUB_CODE = RSA_RECEIVER_PUB.code;
  public static final int RSA_SENDER_PUB_CODE = RSA_SENDER_PUB.code;
  public static final int AES_CBC_32_CODE = AES_CBC_32.code;
  public static final int ECC_RECEIVER_PUB_CODE = ECC_RECEIVER_PUB.code;
  public static final int ECC_SENDER_PUB_CODE = ECC_SENDER_PUB.code;

  private final int code;


  DataEncryptType(int code) {
    this.code = code;
  }

  public static DataEncryptType valueOf(int code) {
    for (DataEncryptType value : values()) {
      if (value.code == code) {
        return value;
      }
    }
    throw new PacketException("不支持的签名算法：" + code);
  }
}
