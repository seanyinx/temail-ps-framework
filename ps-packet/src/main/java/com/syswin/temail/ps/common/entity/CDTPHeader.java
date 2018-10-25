package com.syswin.temail.ps.common.entity;

import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPHeader.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author 姚华成
 * @date 2018-8-29
 */
@Data
@AllArgsConstructor
public final class CDTPHeader {

  private String deviceId;
  private int signatureAlgorithm;
  private String signature;
  private int dataEncryptionMethod;
  private long timestamp;
  private String packetId;
  private String sender;
  private String senderPK;
  private String receiver;
  private String receiverPK;
  private String at;
  private String topic;
  private String extraData;
  private String targetAddress;

  public CDTPHeader() {
  }

  public CDTPHeader(CDTPProtoBuf.CDTPHeader cdtpHeader) {
    this.setDeviceId(nullable(cdtpHeader.getDeviceId()));
    this.setSignatureAlgorithm(cdtpHeader.getSignatureAlgorithm());
    this.setSignature(nullable(cdtpHeader.getSignature()));
    this.setDataEncryptionMethod(cdtpHeader.getDataEncryptionMethod());
    this.setTimestamp(cdtpHeader.getTimestamp());
    this.setPacketId(nullable(cdtpHeader.getPacketId()));
    this.setSender(nullable(cdtpHeader.getSender()));
    this.setSenderPK(nullable(cdtpHeader.getSenderPK()));
    this.setReceiver(nullable(cdtpHeader.getReceiver()));
    this.setReceiverPK(nullable(cdtpHeader.getReceiverPK()));
    this.setAt(nullable(cdtpHeader.getAt()));
    this.setTopic(nullable(cdtpHeader.getTopic()));
    this.setExtraData(nullable(cdtpHeader.getExtraData()));
    this.setTargetAddress(nullable(cdtpHeader.getTargetAddress()));
  }

  private static String nullable(String value) {
    return value.isEmpty() ? null : value;
  }

  @Override
  public CDTPHeader clone() {
    try {
      return (CDTPHeader) super.clone();
    } catch (CloneNotSupportedException e) {
      return this;
    }
  }

  public CDTPProtoBuf.CDTPHeader toCDTPHeader() {
    Builder builder = CDTPProtoBuf.CDTPHeader.newBuilder();
    if (deviceId != null) {
      builder.setDeviceId(getDeviceId());
    }
    builder.setSignatureAlgorithm(getSignatureAlgorithm());
    if (signature != null) {
      builder.setSignature(getSignature());
    }
    builder.setDataEncryptionMethod(getDataEncryptionMethod());
    builder.setTimestamp(getTimestamp());
    if (packetId != null) {
      builder.setPacketId(getPacketId());
    }
    if (sender != null) {
      builder.setSender(getSender());
    }
    if (senderPK != null) {
      builder.setSenderPK(getSenderPK());
    }
    if (receiver != null) {
      builder.setReceiver(getReceiver());
    }
    if (receiverPK != null) {
      builder.setReceiverPK(getReceiverPK());
    }
    if (at != null) {
      builder.setAt(getAt());
    }
    if (topic != null) {
      builder.setTopic(getTopic());
    }
    if (extraData != null) {
      builder.setExtraData(getExtraData());
    }
    if (targetAddress != null) {
      builder.setTargetAddress(getTargetAddress());
    }
    return builder.build();
  }
}
