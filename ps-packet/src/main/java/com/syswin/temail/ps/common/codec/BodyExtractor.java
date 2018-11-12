package com.syswin.temail.ps.common.codec;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.packet.ByteBuf;

/**
 * CDCTPacket的Data数据解包和数据解密<br>
 * 默认实现{@link SimpleBodyExtractor}是基本的CDTPPacket的解包实现，能满足绝大部分的需求
 */
public interface BodyExtractor {

  byte[] fromBuffer(short commandSpace, short command, ByteBuf byteBuf, int remainingBytes);

  void decrypt(CDTPPacket packet);
}
