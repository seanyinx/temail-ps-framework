package com.syswin.temail.ps.common.exception;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import lombok.Getter;

/**
 * @author 姚华成
 * @date 2018-8-24
 */
public class PacketException extends RuntimeException {

  @Getter
  private CDTPPacket packet;

  public PacketException(String message) {
    super(message);
  }

  public PacketException(String message, CDTPPacket packet) {
    super(message);
    this.packet = packet;
  }

  public PacketException(String message, Throwable cause, CDTPPacket packet) {
    super(message, cause);
    this.packet = packet;
  }

  public PacketException(Throwable cause, CDTPPacket packet) {
    super(cause);
    this.packet = packet;
  }

}
