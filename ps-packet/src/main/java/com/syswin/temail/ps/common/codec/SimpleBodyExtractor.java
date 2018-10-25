package com.syswin.temail.ps.common.codec;

import com.syswin.temail.ps.common.utils.ByteBuf;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SimpleBodyExtractor implements BodyExtractor {

  @Override
  public byte[] fromBuffer(short commandSpace, short command, ByteBuf byteBuf, int remainingBytes) {
    byte[] data = new byte[remainingBytes];
    byteBuf.readBytes(data);
    return data;
  }
}
