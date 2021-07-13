package validators;

import com.google.common.collect.Lists;

import java.text.MessageFormat;
import java.util.List;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.request.Request;
import org.sunbird.common.util.JsonKey;
import org.sunbird.util.LoggerUtil;

public class GroupMembershipUpdateRequestValidator implements IRequestValidator {
  private static LoggerUtil logger =
    new LoggerUtil(GroupMembershipUpdateRequestValidator.class);

  @Override
  public boolean validate(Request request) throws BaseException {
    logger.info(request.getContext(),"Validating the update group membership request" + request.getRequest());
    try {
      ValidationUtil.validateRequestObject(request);

      ValidationUtil.validateMandatoryParamsWithType(
              request.getRequest(),
              Lists.newArrayList(JsonKey.USER_ID),
              String.class,
              true,
              JsonKey.REQUEST,request.getContext());
      ValidationUtil.validateMandatoryParamsWithType(
              request.getRequest(),
              Lists.newArrayList(JsonKey.GROUPS),
              List.class,
              true,
              JsonKey.REQUEST,request.getContext());

      return true;
    }catch (BaseException ex){
      BaseException baseException = new BaseException(ResponseCode.GS_MBRSHP_UDT02.getErrorCode(),ResponseCode.GS_MBRSHP_UDT02.getErrorMessage(),ex.getResponseCode());
      logger.error(request.getContext(), MessageFormat.format("GroupMembershipUpdateRequestValidator: Error Code: {0}, ErrMsg {1}",ResponseCode.GS_MBRSHP_UDT02.getErrorCode(),ex.getMessage()),baseException);
      throw baseException;
    }
  }
}
