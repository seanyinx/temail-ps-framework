package com.syswin.temail.ps.common.entity;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.INTERNAL_ERROR_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.PING_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.PONG_CODE;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author 姚华成
 * @date 2018-8-24
 */
@Data
@AllArgsConstructor
public final class CDTPPacket {

  private short commandSpace;
  private short command;
  private short version;
  private CDTPHeader header;
  private byte[] data;

  public CDTPPacket() {
  }

  public CDTPPacket(CDTPPacket other) {
    commandSpace = other.commandSpace;
    command = other.command;
    version = other.version;
    if (other.header != null) {
      header = other.header.clone();
    }
    if (other.data != null) {
      data = Arrays.copyOf(other.data, other.data.length);
    }
  }

  public boolean isHearbeat() {
    return commandSpace == CHANNEL_CODE &&
        (command == PING_CODE || command == PONG_CODE);
  }

  public boolean isInternalError() {
    return commandSpace == CHANNEL_CODE &&
        (command == INTERNAL_ERROR_CODE);
  }
}
