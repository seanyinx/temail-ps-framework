package com.syswin.temail.ps.common.utils;

/**
 * @author 姚华成
 * @date 2018-9-14
 */
public class StringUtil {

  public static final String EMPTY = "";

  public static boolean isEmpty(Object str) {
    return (str == null || "".equals(str));
  }

  public static boolean hasLength(CharSequence str) {
    return (str != null && str.length() > 0);
  }

  public static boolean hasLength(String str) {
    return (str != null && !str.isEmpty());
  }

  public static boolean hasText(CharSequence str) {
    return (str != null && str.length() > 0 && containsText(str));
  }

  public static boolean hasText(String str) {
    return (str != null && !str.isEmpty() && containsText(str));
  }

  private static boolean containsText(CharSequence str) {
    int strLen = str.length();
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  public static String defaultString(final String str) {
    return str == null ? EMPTY : str;
  }

  public static String defaultString(final String str, final String defaultStr) {
    return str == null ? defaultStr : str;
  }
}
