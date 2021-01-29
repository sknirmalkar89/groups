package org.sunbird.models;

import org.sunbird.exception.BaseException;
import org.sunbird.response.Response;

public class ClientErrorResponse extends Response {

  private BaseException exception = null;

  public ClientErrorResponse() {}

  public BaseException getException() {
    return exception;
  }

  public void setException(BaseException exception) {
    this.exception = exception;
  }
}
