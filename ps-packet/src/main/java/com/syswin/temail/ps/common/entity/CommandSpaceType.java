package com.syswin.temail.ps.common.entity;

import com.syswin.temail.ps.common.exception.PacketException;
import lombok.Getter;

/**
 * @author 姚华成
 * @date 2018-8-25
 */
@Getter
public enum CommandSpaceType {
  CHANNEL(0),
  SINGLE_MESSAGE(1),
  GROUP_MESSAGE(2),
  SYNC_STATUS(3),
  STRATEGY(4),
  ;

  public static final short CHANNEL_CODE = CHANNEL.code;
  public static final short SINGLE_MESSAGE_CODE = SINGLE_MESSAGE.code;
  public static final short GROUP_MESSAGE_CODE = GROUP_MESSAGE.code;
  public static final short SYNC_STATUS_CODE = SYNC_STATUS.code;
  public static final short STRATEGY_CODE = STRATEGY.code;
  private short code;

  CommandSpaceType(int code) {
    this.code = (short) code;
  }

  public static CommandSpaceType valueOf(short code) {
    for (CommandSpaceType value : values()) {
      if (value.code == code) {
        return value;
      }
    }
    throw new PacketException("不支持的CommandSpace的编码：" + code);
  }
}
