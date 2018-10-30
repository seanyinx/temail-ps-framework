package com.syswin.temail.ps.common.utils;

import static com.syswin.temail.ps.common.Constants.LENGTH_FIELD_LENGTH;
import static com.syswin.temail.ps.common.utils.ByteBuf.DEFAULT_ALLOC_LENGTH;

import com.google.protobuf.InvalidProtocolBufferException;
import com.syswin.temail.ps.common.codec.BodyExtractor;
import com.syswin.temail.ps.common.codec.SimpleBodyExtractor;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf;
import com.syswin.temail.ps.common.exception.PacketException;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 姚华成
 * @date 2018-10-25
 */
@Slf4j
public class PacketUtil {

  private static final SimpleBodyExtractor SIMPLE_BODY_EXTRACTOR = new SimpleBodyExtractor();

  /**
   * 将收到的字节数组的CDTPPacket进行解包，生成Message对象。主要用于单聊、群聊等服务端保留完整Packet的情况
   *
   * @param packetData Base64UrlSafe形式的CDTPPacket包，包含前导的长度
   * @return 解包后的CDTPPacket对象
   */
  public static CDTPPacket unpack(String packetData) {
    return unpack(Base64.getUrlDecoder().decode(packetData), SIMPLE_BODY_EXTRACTOR);
  }

  public static CDTPPacket unpack(String packetData, BodyExtractor bodyExtractor) {
    return unpack(Base64.getUrlDecoder().decode(packetData), bodyExtractor);
  }

  /**
   * 将收到的字节数组的CDTPPacket进行解包，生成CDTPPacket对象。主要用于单聊、群聊等服务端保留完整Packet的情况
   *
   * @param packetData 字节数组形式的CDTPPacket包，包含前导的长度
   * @return 解包后的CDTPPacket对象
   */
  public static CDTPPacket unpack(byte[] packetData) {
    return unpack(packetData, SIMPLE_BODY_EXTRACTOR);
  }

  public static CDTPPacket unpack(byte[] packetData, BodyExtractor bodyExtractor) {
    if (packetData == null || packetData.length <= LENGTH_FIELD_LENGTH) {
      return null;
    }

    ByteBuf byteBuf = new ByteBuf(packetData);
    CDTPPacket packet = new CDTPPacket();

    byteBuf.markReaderIndex();
    int packetLength = byteBuf.readInt();
    if (packetLength <= 0) {
      throw new PacketException("包长度不合法：" + packetLength);
    }
    if (byteBuf.readableBytes() < packetLength) {
      byteBuf.resetReaderIndex();
      return null;
    }
    short commandSpace = byteBuf.readShort();
    if (commandSpace < 0) {
      throw new PacketException("命令空间不合法，commandSpace=" + commandSpace);
    }
    packet.setCommandSpace(commandSpace);

    short command = byteBuf.readShort();
    if (command <= 0) {
      throw new PacketException("命令不合法，command=" + command);
    }
    packet.setCommand(command);

    short version = byteBuf.readShort();
    packet.setVersion(version);

    short headerLength = byteBuf.readShort();
    CDTPProtoBuf.CDTPHeader cdtpHeader;
    if (headerLength < 0) {
      throw new PacketException("headerLength长度错误，headerLength=" + headerLength);
    }
    if (headerLength > 0) {
      if (byteBuf.readableBytes() < headerLength) {
        throw new PacketException("无法读取到HeaderLength指定的全部Header数据：headerLength=" + headerLength
            + "，剩余可读取的数据长度" + byteBuf.readableBytes());
      }
      byte[] headerBytes = new byte[headerLength];
      byteBuf.readBytes(headerBytes);
      try {
        cdtpHeader = CDTPProtoBuf.CDTPHeader.parseFrom(headerBytes);
      } catch (InvalidProtocolBufferException e) {
        log.error("解包错误", e);
        throw new PacketException("解包错误：" + e.getMessage());
      }
      packet.setHeader(new CDTPHeader(cdtpHeader));
    }

    byte[] data = bodyExtractor.fromBuffer(commandSpace, command, byteBuf, packetLength - headerLength - 8);

    packet.setData(data);

    bodyExtractor.decrypt(packet);
    return packet;
  }


  public static byte[] pack(CDTPPacket packet) {
    ByteBuf byteBuf = new ByteBuf(DEFAULT_ALLOC_LENGTH);
    byteBuf.writeShort(packet.getCommandSpace());
    byteBuf.writeShort(packet.getCommand());
    byteBuf.writeShort(packet.getVersion());
    CDTPHeader header = packet.getHeader();
    if (header != null) {
      byte[] headerBytes = header.toCDTPHeader().toByteArray();
      byteBuf.writeShort(headerBytes.length);
      byteBuf.writeBytes(headerBytes);
    } else {
      byteBuf.writeShort(0);
    }
    byteBuf.writeBytes(packet.getData());
    return byteBuf.getArray();
  }
}
