package com.syswin.temail.ps.common.codec;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.packet.ByteBuf;

public interface BodyExtractor {

  byte[] fromBuffer(short commandSpace, short command, ByteBuf byteBuf, int remainingBytes);

  void decrypt(CDTPPacket packet);
}
