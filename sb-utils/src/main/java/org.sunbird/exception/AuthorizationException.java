package org.sunbird.exception;

import org.sunbird.message.IResponseMessage;
import org.sunbird.message.ResponseCode;

public class AuthorizationException {

  public static class NotAuthorized extends BaseException {
    public NotAuthorized(ResponseCode responseCode) {
      super(responseCode.getErrorCode(), responseCode.getErrorMessage(), 401);
    }
  }
}
