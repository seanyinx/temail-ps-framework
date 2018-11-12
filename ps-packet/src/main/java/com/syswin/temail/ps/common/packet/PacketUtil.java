package com.syswin.temail.ps.common.packet;

import static com.syswin.temail.ps.common.Constants.LENGTH_FIELD_LENGTH;
import static com.syswin.temail.ps.common.utils.StringUtil.defaultString;

import com.google.protobuf.InvalidProtocolBufferException;
import com.syswin.temail.ps.common.codec.BodyExtractor;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf;
import com.syswin.temail.ps.common.exception.PacketException;
import com.syswin.temail.ps.common.utils.DigestUtil;
import com.syswin.temail.ps.common.utils.HexUtil;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

/**
 * CDTPPacket操作的工具类<br>
 * 可以对CDTPPacket包进行封包解包，Data编码、解码，与CDTPPacketTrans的转换以及获取待签名数据等操作<br>
 *
 * @author 姚华成
 * @date 2018-10-25
 */
@Slf4j
public abstract class PacketUtil {

  public static CDTPPacket unpack(String packetData, BodyExtractor bodyExtractor) {
    return unpack(Base64.getUrlDecoder().decode(packetData), bodyExtractor);
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
    try {
      bodyExtractor.decrypt(packet);
    } catch (Exception e) {
      // 解密失败，招聘异常
      throw new PacketException("数据解密失败！", e, packet);
    }
    return packet;
  }

  public static byte[] pack(CDTPPacket packet) {
    return pack(packet, false);
  }

  public static byte[] pack(CDTPPacket packet, boolean includeLength) {
    CDTPHeader header = packet.getHeader();
    byte[] headerBytes;
    if (header != null) {
      headerBytes = header.toProtobufHeader().toByteArray();
    } else {
      headerBytes = new byte[0];
    }
    int byteBufLen = (includeLength ? LENGTH_FIELD_LENGTH : 0) + 8 + headerBytes.length + packet.getData().length;
    ByteBuf byteBuf = new ByteBuf(byteBufLen);
    if (includeLength) {
      byteBuf.writeInt(byteBufLen - LENGTH_FIELD_LENGTH);
    }
    byteBuf.writeShort(packet.getCommandSpace());
    byteBuf.writeShort(packet.getCommand());
    byteBuf.writeShort(packet.getVersion());
    byteBuf.writeShort(headerBytes.length);
    byteBuf.writeBytes(headerBytes);
    byteBuf.writeBytes(packet.getData());
    return byteBuf.getBuf();
  }

  public static String getUnsignData(CDTPPacket packet) {
    CDTPHeader header = packet.getHeader();
    byte[] data = packet.getData();
    String targetAddress = defaultString(header.getTargetAddress());
    String dataSha256 = data == null ? "" : HexUtil.encodeHex(DigestUtil.sha256(data));
    return String.valueOf(packet.getCommandSpace() + packet.getCommand())
        + targetAddress
        + String.valueOf(header.getTimestamp())
        + dataSha256;
  }

  protected abstract BodyExtractor getBodyExtractor();

  /**
   * 将收到的字节数组的CDTPPacket进行解包，生成Message对象。主要用于单聊、群聊等服务端保留完整Packet的情况
   *
   * @param packetData Base64UrlSafe形式的CDTPPacket包，包含前导的长度
   * @return 解包后的CDTPPacket对象
   */
  public CDTPPacket unpack(String packetData) {
    return unpack(Base64.getUrlDecoder().decode(packetData), getBodyExtractor());
  }

  /**
   * 将收到的字节数组的CDTPPacket进行解包，生成CDTPPacket对象。主要用于单聊、群聊等服务端保留完整Packet的情况
   *
   * @param packetData 字节数组形式的CDTPPacket包，包含前导的长度
   * @return 解包后的CDTPPacket对象
   */
  public CDTPPacket unpack(byte[] packetData) {
    return unpack(packetData, getBodyExtractor());
  }

  protected abstract String encodeData(CDTPPacket packet);

  protected abstract byte[] decodeData(CDTPPacketTrans packet);

  public CDTPPacketTrans toTrans(CDTPPacket packet) {
    if (packet == null) {
      return null;
    }
    return new CDTPPacketTrans(packet.getCommandSpace(), packet.getCommand(), packet.getVersion(),
        packet.getHeader().clone(), encodeData(packet));
  }

  public CDTPPacket fromTrans(CDTPPacketTrans packetTrans) {
    if (packetTrans == null) {
      return null;
    }
    return new CDTPPacket(packetTrans.getCommandSpace(), packetTrans.getCommand(), packetTrans.getVersion(),
        packetTrans.getHeader().clone(),
        decodeData(packetTrans));
  }
}
