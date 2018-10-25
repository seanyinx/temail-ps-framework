package com.syswin.temail.ps.common;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;

import com.google.gson.Gson;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author 姚华成
 * @date 2018-10-25
 */
public class PacketMaker {

  private static Gson gson = new Gson();

  static CDTPPacket sendSingleCharPacket(String sender, String receiver, String content) {
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

}
