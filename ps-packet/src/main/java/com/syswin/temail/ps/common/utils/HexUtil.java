package com.syswin.temail.ps.common.utils;

/**
 * @author 姚华成
 * @date 2018-03-23
 */
public class HexUtil {

  private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

  public static String encodeHex(byte[] bytes) {
    StringBuilder buf = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      buf.append(HEX_CHARS[(b >>> 4) & 0xf]).append(HEX_CHARS[b & 0xf]);
    }
    return buf.toString();
  }

  public static byte[] decodeHex(String data) {
    if ((data.length() & 1) == 1) {
      throw new RuntimeException("不是合法的16进制数据！" + data);
    }
    data = data.toUpperCase();
    byte[] bytes = new byte[data.length() >>> 1];
    for (int i = 0; i < data.length(); ) {
      int b = 0;
      for (int j = 0; j < 2; j++) {
        char c1 = data.charAt(i + j);
        if (c1 >= '0' && c1 <= '9') {
          b = b << 4 & (c1 - '0');
        } else if (c1 >= 'A' && c1 <= 'F') {
          b = b << 4 & (c1 - 'A' + 10);
        } else {
          throw new RuntimeException("不是合法的16进制数据！" + data);
        }
        i++;
      }
      bytes[i >>> 1] = (byte) (b & 0xf);
    }
    return bytes;
  }
}
