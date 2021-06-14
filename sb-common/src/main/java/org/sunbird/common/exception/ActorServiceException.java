package org.sunbird.common.exception;

import java.util.Locale;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.Localizer;

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
