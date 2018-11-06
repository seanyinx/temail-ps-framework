package com.syswin.temail.ps.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author 姚华成
 * @date 2018-03-23
 */
public class DigestUtil {

  private static final String ALGORITHM_MD5 = "MD5";
  private static final String ALGORITHM_SHA224 = "SHA-224";
  private static final String ALGORITHM_SHA256 = "SHA-256";
  private static final String ALGORITHM_SHA384 = "SHA-384";
  private static final String ALGORITHM_SHA512 = "SHA-512";

  /**
   * 对数据进行md5签名
   *
   * @param data 原始数据
   * @return 数据的签名
   */
  public static byte[] md5(byte[] data) {
    return digest(ALGORITHM_MD5, data);
  }

  /**
   * 对数据进行sha224签名
   *
   * @param data 原始数据
   * @return 数据的签名
   */
  public static byte[] sha224(byte[] data) {
    return digest(ALGORITHM_SHA224, data);
  }

  /**
   * 对数据进行sha256签名
   *
   * @param data 原始数据
   * @return 数据的签名
   */
  public static byte[] sha256(byte[] data) {
    return digest(ALGORITHM_SHA256, data);
  }

  /**
   * 对数据进行sha384签名
   *
   * @param data 原始数据
   * @return 数据的签名
   */
  public static byte[] sha384(byte[] data) {
    return digest(ALGORITHM_SHA384, data);
  }

  /**
   * 对数据进行sha512签名
   *
   * @param data 原始数据
   * @return 数据的签名
   */
  public static byte[] sha512(byte[] data) {
    return digest(ALGORITHM_SHA512, data);
  }

  private static MessageDigest getDigest(String algorithm) {
    try {
      return MessageDigest.getInstance(algorithm);
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("Could not find MessageDigest with algorithm \"" + algorithm + "\"", ex);
    }
  }

  public static byte[] digest(String algorithm, byte[] data) {
    return getDigest(algorithm).digest(data);
  }

}
