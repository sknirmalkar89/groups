package org.sunbird.exception;

import org.sunbird.message.IResponseMessage;

public class AuthorizationException {

  public static class NotAuthorized extends BaseException {
    public NotAuthorized() {
      super(IResponseMessage.Key.UNAUTHORIZED, IResponseMessage.Message.UNAUTHORIZED, 401);
    }
  }
}
