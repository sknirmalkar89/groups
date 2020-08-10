package org.sunbird.exception;

import java.util.Locale;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.Localizer;

public class ActorServiceException {

  public static class InvalidOperationName extends BaseException {
    public InvalidOperationName(Locale locale) {
      super(
          IResponseMessage.INVALID_OPERATION_NAME,
          Localizer.getInstance().getMessage(IResponseMessage.INVALID_OPERATION_NAME, locale),
          500);
    }
  }
}
