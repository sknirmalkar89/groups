package validators;

import org.sunbird.common.exception.BaseException;
import org.sunbird.common.request.Request;

/** * this is an interface class for validating the request */
public interface IRequestValidator {
  default boolean validate(Request request) throws BaseException {
    return false;
  }
}
