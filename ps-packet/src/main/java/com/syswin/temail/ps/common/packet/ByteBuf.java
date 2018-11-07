package com.syswin.temail.ps.common.packet;

import com.syswin.temail.ps.common.exception.PacketException;

/**
 * 对应于Netty的ByteBuf，为减少对Netty的依赖而编写
 *
 * @author 姚华成
 * @date 2018-10-25
 */
public class ByteBuf {

  // TODO 时间紧，只写用到的功能点
  public static final int DEFAULT_ALLOC_LENGTH = 1024;
  private int readerIndex;
  private int writerIndex;
  private int markedReaderIndex;
  private int markedWriterIndex;

  private byte[] buf;

  public ByteBuf(int size) {
    this.buf = new byte[size];
  }

  public ByteBuf(byte[] buf) {
    if (buf == null) {
      this.buf = new byte[DEFAULT_ALLOC_LENGTH];
    } else {
      this.buf = buf;
    }
    this.writerIndex = this.buf.length;
  }

  public void markReaderIndex() {
    markedReaderIndex = readerIndex;
  }

  public void markWriterIndex() {
    markedWriterIndex = writerIndex;
  }

  public void resetReaderIndex() {
    readerIndex = markedReaderIndex;
  }

  public void resetWriterIndex() {
    writerIndex = markedWriterIndex;
  }

  public int readableBytes() {
    return writerIndex - readerIndex;
  }

  public int readInt() {
    checkReadLen(4);
    return (buf[readerIndex++] & 0xff) << 24 |
        (buf[readerIndex++] & 0xff) << 16 |
        (buf[readerIndex++] & 0xff) << 8 |
        (buf[readerIndex++] & 0xff);
  }

  public short readShort() {
    checkReadLen(2);
    return (short) ((buf[readerIndex++] & 0xff) << 8 | (buf[readerIndex++] & 0xff));
  }

  public void readBytes(byte[] bytes) {
    if (bytes == null || bytes.length == 0) {
      return;
    }
    checkReadLen(bytes.length);
    System.arraycopy(buf, readerIndex, bytes, 0, bytes.length);
    readerIndex += bytes.length;
  }

  private void checkReadLen(int intendReadLen) {
    if (this.readerIndex + intendReadLen > this.buf.length) {
      throw new PacketException("试图读取的长度" + intendReadLen + "已经超过可读取的长度" + this.readableBytes());
    }
  }

  public void writeInt(int data) {
    extendBuf(4);
    buf[writerIndex++] = (byte) (data >>> 24 & 0xff);
    buf[writerIndex++] = (byte) (data >>> 16 & 0xff);
    buf[writerIndex++] = (byte) (data >>> 8 & 0xff);
    buf[writerIndex++] = (byte) (data & 0xff);
  }

  public void writeShort(int data) {
    extendBuf(2);
    buf[writerIndex++] = (byte) (data >>> 8 & 0xff);
    buf[writerIndex++] = (byte) (data & 0xff);
  }

  public void writeBytes(byte[] bytes) {
    extendBuf(bytes.length);
    System.arraycopy(bytes, 0, buf, writerIndex, bytes.length);
    writerIndex += bytes.length;
  }

  public byte[] getArray() {
    byte[] bytes = new byte[writerIndex];
    System.arraycopy(buf, 0, bytes, 0, writerIndex);
    return bytes;
  }

  public byte[] getBuf() {
    return this.buf;
  }

  private void extendBuf(int length) {
    if (buf.length < writerIndex + length) {
      byte[] bytes = new byte[(writerIndex + length) * 2];
      System.arraycopy(buf, 0, bytes, 0, writerIndex);
      this.buf = bytes;
    }
  }
}
