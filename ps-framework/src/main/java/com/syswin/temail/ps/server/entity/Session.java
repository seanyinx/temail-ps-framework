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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Session session = (Session) o;

    if (temail != null ? !temail.equals(session.temail) : session.temail != null) {
      return false;
    }
    return deviceId != null ? deviceId.equals(session.deviceId) : session.deviceId == null;

  }

  @Override
  public int hashCode() {
    int result = temail != null ? temail.hashCode() : 0;
    result = 31 * result + (deviceId != null ? deviceId.hashCode() : 0);
    return result;
  }
}
