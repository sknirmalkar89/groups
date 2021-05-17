package validators;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.exception.BaseException;
import org.sunbird.message.ResponseCode;
import org.sunbird.request.Request;
import org.sunbird.util.JsonKey;

public class GroupReadRequestValidator implements IRequestValidator {

  private static Logger logger = LoggerFactory.getLogger(GroupCreateRequestValidator.class);

  @Override
  public boolean validate(Request request) throws BaseException {
    logger.info("Validating the request read group {}", request.getRequest());
    try {
      ValidationUtil.validateRequestObject(request);
      ValidationUtil.validateMandatoryParamsWithType(
              request.getRequest(),
              Lists.newArrayList(JsonKey.GROUP_ID),
              String.class,
              true,
              JsonKey.REQUEST);
      return true;
    }catch (BaseException ex){
      throw new BaseException(ResponseCode.GS_RED_02.getErrorCode(),ex.getMessage(),ex.getResponseCode());
    }
  }
}
