package validators;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.request.Request;
import org.sunbird.common.util.JsonKey;

public class GroupDeleteRequestValidator implements IRequestValidator {
  private static Logger logger = LoggerFactory.getLogger(GroupDeleteRequestValidator.class);

  @Override
  public boolean validate(Request request) throws BaseException {
    logger.info("Validating the update request {}", request.getRequest());
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
      logger.error("GroupDeleteRequestValidator:Error Code: {}, ErrMsg {}",ResponseCode.GS_DLT02.getErrorCode(),ex.getMessage());
      throw new BaseException(ResponseCode.GS_DLT02.getErrorCode(),ResponseCode.GS_DLT02.getErrorMessage(),ex.getResponseCode());
    }
  }
}
