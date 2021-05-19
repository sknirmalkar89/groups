package validators;

import com.google.common.collect.Lists;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.exception.BaseException;
import org.sunbird.message.ResponseCode;
import org.sunbird.request.Request;
import org.sunbird.util.JsonKey;

public class GroupMembershipUpdateRequestValidator implements IRequestValidator {
  private static Logger logger =
      LoggerFactory.getLogger(GroupMembershipUpdateRequestValidator.class);

  @Override
  public boolean validate(Request request) throws BaseException {
    logger.info("Validating the update group membership request {}", request.getRequest());
    try {
      ValidationUtil.validateRequestObject(request);

      ValidationUtil.validateMandatoryParamsWithType(
              request.getRequest(),
              Lists.newArrayList(JsonKey.USER_ID),
              String.class,
              true,
              JsonKey.REQUEST);
      ValidationUtil.validateMandatoryParamsWithType(
              request.getRequest(),
              Lists.newArrayList(JsonKey.GROUPS),
              List.class,
              true,
              JsonKey.REQUEST);

      return true;
    }catch (BaseException ex){
      logger.error("GroupMembershipUpdateRequestValidator: {}",ex.getMessage());
      throw new BaseException(ResponseCode.GS_MBRSHP_UDT02.getErrorCode(),ex.getMessage(),ex.getResponseCode());
    }
  }
}
