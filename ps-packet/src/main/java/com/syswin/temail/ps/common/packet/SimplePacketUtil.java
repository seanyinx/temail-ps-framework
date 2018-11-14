package com.syswin.temail.ps.common.packet;

import com.syswin.temail.ps.common.codec.BodyExtractor;
import com.syswin.temail.ps.common.codec.SimpleBodyExtractor;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import java.nio.charset.StandardCharsets;

/**
 * PacketUtil工具的一个简单实现<br>
 * 静态实例INSTANCE是最基本的实现
 *
 * @author 姚华成
 * @date 2018-11-05
 */
public class SimplePacketUtil extends PacketUtil {

  public static final SimplePacketUtil INSTANCE = new SimplePacketUtil();
  private final BodyExtractor bodyExtractor;

  private SimplePacketUtil() {
    this(SimpleBodyExtractor.INSTANCE);
  }

  public SimplePacketUtil(BodyExtractor bodyExtractor) {
    this.bodyExtractor = bodyExtractor;
  }

  @Override
  protected BodyExtractor getBodyExtractor() {
    return bodyExtractor;
  }

  @Override
  public String encodeData(CDTPPacket packet) {
    byte[] data;
    if (packet == null || (data = packet.getData()) == null) {
      return "";
    }
    return new String(data, StandardCharsets.UTF_8);
  }

  @Override
  public byte[] decodeData(CDTPPacketTrans packet) {
    String data;
    if (packet == null || (data = packet.getData()) == null) {
      return new byte[0];
    }
    return data.getBytes(StandardCharsets.UTF_8);
  }
}
