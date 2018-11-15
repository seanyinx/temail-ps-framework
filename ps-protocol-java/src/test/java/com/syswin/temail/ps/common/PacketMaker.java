package com.syswin.temail.ps.common;

import static com.syswin.temail.ps.common.Constants.CDTP_VERSION;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;

import com.google.gson.Gson;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLogin;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLogin.Builder;
import com.syswin.temail.ps.common.entity.CommandType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PacketMaker {

  private static Gson gson = new Gson();

  public static CDTPPacket privateMsgPacket(String sender, String receiver, String content) {
    CDTPPacket packet = new CDTPPacket();
    packet.setCommandSpace(SINGLE_MESSAGE_CODE);
    packet.setCommand((short) 1);
    CDTPHeader header = new CDTPHeader();
    header.setDeviceId("deviceId");
    header.setDataEncryptionMethod(0);
    header.setTimestamp(System.currentTimeMillis());
    header.setPacketId(UUID.randomUUID().toString());

    header.setSender(sender);
    header.setSenderPK("SenderPK");
    header.setReceiver(receiver);
    header.setReceiverPK("ReceiverPK");
    Map<String, Object> extraData = new HashMap<>();
    extraData.put("from", sender);
    extraData.put("to", receiver);
    extraData.put("storeType", "2");
    extraData.put("type", "0");
    extraData.put("msgId", "4298F38F87DC4775B264A3753E77B443");
    header.setExtraData(gson.toJson(extraData));

    packet.setHeader(header);
    packet.setData(content.getBytes());
    return packet;
  }

  public static CDTPPacket loginPacket(String sender, String deviceId) {
    CDTPPacket packet = new CDTPPacket();
    CDTPHeader header = new CDTPHeader();
    header.setDeviceId(deviceId);
    header.setSignatureAlgorithm(1);
    header.setTimestamp(1535713173935L);
    header.setDataEncryptionMethod(0);
    header.setPacketId("PacketId12345");
    header.setSender(sender);
    header.setSenderPK("SenderPK");
//    header.setReceiver("sean@t.email");
//    header.setReceiverPK("ReceiverPK");

    packet.setCommandSpace(CHANNEL_CODE);
    packet.setCommand(CommandType.LOGIN.getCode());
    packet.setVersion(CDTP_VERSION);
    packet.setHeader(header);

    Builder builder = CDTPLogin.newBuilder();

//    builder.setdevId("设备ID");
    builder.setPushToken("推送token");
    builder.setPlatform("ios/android/pc");
    builder.setOsVer("11.4");
    builder.setAppVer("1.0.0");
    builder.setLang("en、ch-zn...");
    builder.setTemail("请求发起方的temail地址");
    builder.setChl("渠道号");
    CDTPLogin cdtpLogin = builder.build();

    packet.setData(cdtpLogin.toByteArray());
    return packet;
  }

}
