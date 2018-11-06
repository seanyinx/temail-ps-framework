package com.syswin.temail.ps.server.exception;

/**
 * @author 姚华成
 * @date 2018-11-06
 */
public class PsServerException extends RuntimeException {

  public PsServerException() {
  }

  public PsServerException(String message) {
    super(message);
  }

  public PsServerException(Throwable cause) {
    super(cause);
  }

  public PsServerException(String message, Throwable cause) {
    super(message, cause);
  }
}
