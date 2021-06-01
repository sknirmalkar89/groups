package validators;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.request.Request;
import org.sunbird.common.util.JsonKey;

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
      logger.error("GroupReadRequestValidator: Error Code: {}, ErrMsg {}",ResponseCode.GS_RED02.getErrorCode(),ex.getMessage());
      throw new BaseException(ResponseCode.GS_RED02.getErrorCode(),ResponseCode.GS_RED02.getErrorMessage(),ex.getResponseCode());
    }
  }
}
