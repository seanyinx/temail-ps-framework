package com.syswin.temail.ps.common.codec;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.packet.ByteBuf;
import com.syswin.temail.ps.common.packet.PacketDecryptor;

/**
 * BodyExtractor的默认实现，能解决绝大部分需求。如果没有解密需求，可以直接使用实例INSTANCE<br>
 * 其中PacketDecryptor是解密器，根据具体的解密算法实现。如果不解密，使用{@link PacketDecryptor#NoOp}
 */
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
