package com.syswin.temail.ps.common.codec;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.packet.ByteBuf;
import com.syswin.temail.ps.common.packet.PacketDecryptor;

public class SimpleBodyExtractor implements BodyExtractor {

  public static final SimpleBodyExtractor INSTANCE = new SimpleBodyExtractor();

  private final PacketDecryptor decryptor;

  private SimpleBodyExtractor() {
    this(PacketDecryptor.NoOp);
  }

  public SimpleBodyExtractor(PacketDecryptor decryptor) {
    this.decryptor = decryptor;
  }

  @Override
  public byte[] fromBuffer(short commandSpace, short command, ByteBuf byteBuf, int remainingBytes) {
    byte[] data = new byte[remainingBytes];
    byteBuf.readBytes(data);
    return data;
  }

  @Override
  public void decrypt(CDTPPacket packet) {
    decryptor.decrypt(packet);
  }
}
