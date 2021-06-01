package org.sunbird.models;

import org.sunbird.common.exception.BaseException;
import org.sunbird.common.response.Response;

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
