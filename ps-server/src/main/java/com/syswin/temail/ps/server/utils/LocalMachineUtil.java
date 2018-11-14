package com.syswin.temail.ps.server.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalMachineUtil {

  private static final String DEFAULT_IP = "127-0-0-1";

  public static String getLocalIp() {
    String osName = System.getProperty("os.name"); // 获取系统名称
    if (osName != null && osName.startsWith("Windows")) { // 如果是Windows系统
      return getWindowsIp();
    } else {
      return geLinuxIp();
    }
  }


  private static String geLinuxIp() {
    String ipLocalAddr = DEFAULT_IP;
    InetAddress ip;
    try {
      Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
      while (allNetInterfaces.hasMoreElements()) {
        NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
        Enumeration addresses = netInterface.getInetAddresses();
        while (addresses.hasMoreElements()) {
          ip = (InetAddress) addresses.nextElement();
          if (ip instanceof Inet4Address) { // IP是ipv4，ipv6换成Inet6Address
            String hostAddress = ip.getHostAddress();
            if (!hostAddress.equals("127.0.0.1") && !hostAddress.equals("/127.0.0.1")) {
              ipLocalAddr = ip.toString().split("[/]")[1]; // 得到本地IP
            }
          }
        }
      }
    } catch (SocketException ex) {
      log.error("获取本机IP失败", ex);
    }
    ipLocalAddr = ipLocalAddr.replace(".", "-");
    return ipLocalAddr;
  }


  private static String getWindowsIp() {
    String localIp = "";
    try {
      InetAddress addr = InetAddress.getLocalHost();
      localIp = addr.getHostAddress(); // 获取本机ip
      localIp = localIp.replace(".", "-");
    } catch (Exception ex) {
      log.error("获取本机IP失败", ex);
      localIp = DEFAULT_IP;
    }
    return localIp;
  }
}
