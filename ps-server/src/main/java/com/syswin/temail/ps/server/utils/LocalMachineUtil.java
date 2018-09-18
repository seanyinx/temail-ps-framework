package com.syswin.temail.ps.server.utils;

import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Vector;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalMachineUtil {

  public static String DEFAULT_IP = "127-0-0-1";

  public static String getLocalIp() {
    String osName = System.getProperty("os.name"); // 获取系统名称
    if (osName != null && osName.startsWith("Windows")) { // 如果是Windows系统
      return getWindowsIp();
    } else {
      return geLinuxIp();
    }
  }


  public static String geLinuxIp() {
    Enumeration allNetInterfaces;
    Vector<String> ipAddr = new Vector<String>();
    String ipLocalAddr = null;
    InetAddress ip = null;
    try {
      allNetInterfaces = NetworkInterface.getNetworkInterfaces();
      while (allNetInterfaces.hasMoreElements()) {
        NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
        Enumeration addresses = netInterface.getInetAddresses();
        while (addresses.hasMoreElements()) {
          ip = (InetAddress) addresses.nextElement();
          ipAddr.add(ip.toString());
          if (ip != null && ip instanceof Inet4Address) { // IP是ipv4，ipv6换成Inet6Address
            String hostAddress = ip.getHostAddress();
            if (!hostAddress.equals("127.0.0.1") && !hostAddress.equals("/127.0.0.1")) {
              ipLocalAddr = ip.toString().split("[/]")[1]; // 得到本地IP
            }
          }
        }
      }
    } catch (SocketException ex) {
      log.error("获取本机IP失败", ex);
      ipLocalAddr = DEFAULT_IP;
    }
    ipLocalAddr = ipLocalAddr.replace(".", "-");
    return ipLocalAddr;
  }


  public static String getWindowsIp() {
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


  public static String getLocalProccesId() {
    String localProcessInf = ManagementFactory.getRuntimeMXBean().getName();
    return localProcessInf.split("@")[0];
  }

}
