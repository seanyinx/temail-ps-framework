package com.syswin.temail.ps.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 姚华成
 * @date 2018-8-29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CDTPPacketTrans {

  private short commandSpace;
  private short command;
  private short version;
  private CDTPHeader header;
  private String data;
}
