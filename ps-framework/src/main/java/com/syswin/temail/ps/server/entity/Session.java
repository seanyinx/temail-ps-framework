package com.syswin.temail.ps.server.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Session {

  private String temail;
  private String deviceId;
}
