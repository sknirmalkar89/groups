package validators;

import org.sunbird.exception.BaseException;
import org.sunbird.request.Request;

/** * this is an interface class for validating the request */
public interface IRequestValidator {
  default boolean validate(Request request) throws BaseException {
    return false;
  }
}
