package com.syswin.temail.ps.common.entity;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author 姚华成
 * @date 2018-8-29
 */
@Data
@AllArgsConstructor
public class CDTPPacketTrans {

  private short commandSpace;
  private short command;
  private short version;
  private CDTPHeader header;
  private String data;

  public CDTPPacketTrans() {
  }

  public CDTPPacketTrans(CDTPPacket packet) {
    this(packet.getCommandSpace(), packet.getCommand(), packet.getVersion(), packet.getHeader().clone(),
        encodeData(packet));
  }

  private static byte[] decodeData(CDTPPacketTrans packet) {
    String data = packet.getData();
    if (data == null) {
      return new byte[0];
    }
    if (isSendSingleMsg(packet.getCommandSpace(), packet.getCommand())) {
      return Base64.getUrlDecoder().decode(data);
    } else {
      return data.getBytes(StandardCharsets.UTF_8);
    }
  }

  private static String encodeData(CDTPPacket packet) {
    if (isSendSingleMsg(packet.getCommandSpace(), packet.getCommand())) {
      return Base64.getUrlEncoder().encodeToString(packet.getData());
    } else {
      return new String(packet.getData(), StandardCharsets.UTF_8);
    }
  }

  private static boolean isSendSingleMsg(short commandSpace, short command) {
    return commandSpace == CommandSpaceType.SINGLE_MESSAGE_CODE && command == 1;
  }

  public CDTPPacket toCDTPPacket() {
    return new CDTPPacket(commandSpace, command, version, header.clone(),
        decodeData(this));
  }

}
