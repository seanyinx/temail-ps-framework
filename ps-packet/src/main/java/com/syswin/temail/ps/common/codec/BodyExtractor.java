package com.syswin.temail.ps.common.codec;

import com.syswin.temail.ps.common.utils.ByteBuf;

public interface BodyExtractor {

  byte[] fromBuffer(short commandSpace, short command, ByteBuf byteBuf, int remainingBytes);
}
