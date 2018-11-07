package com.syswin.temail.ps.common;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.packet.ByteBuf;
import com.syswin.temail.ps.common.packet.PacketUtil;
import com.syswin.temail.ps.common.packet.SimplePacketUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author 姚华成
 * @date 2018-10-25
 */
public class PacketUtilTest {

  private static String sender = "jack@t.email";
  private static String receive = "sean@t.email";
  private static String content = "hello world";
  private static PacketUtil packetUtil = SimplePacketUtil.INSTANCE;

  @Test
  public void testPacketAndUnpacket() {
    CDTPPacket packet = PacketMaker.sendSingleCharPacket(sender, receive, content);
    byte[] bytes = PacketUtil.pack(packet);
    ByteBuf byteBuf = new ByteBuf(bytes.length + 4);
    byteBuf.writeInt(bytes.length);
    byteBuf.writeBytes(bytes);
    CDTPPacket unpackedPacket = packetUtil.unpack(byteBuf.getArray());
    Assert.assertEquals(packet, unpackedPacket);
  }
}
