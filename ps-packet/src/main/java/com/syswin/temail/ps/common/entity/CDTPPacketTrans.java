package com.syswin.temail.ps.common.entity;

import static com.syswin.temail.ps.common.utils.PacketUtil.decodeData;
import static com.syswin.temail.ps.common.utils.PacketUtil.encodeData;

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

  public CDTPPacket toCDTPPacket() {
    return new CDTPPacket(commandSpace, command, version, header.clone(),
        decodeData(this, false));
  }

}
