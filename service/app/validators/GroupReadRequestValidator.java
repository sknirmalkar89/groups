package validators;

import com.google.common.collect.Lists;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.request.Request;
import org.sunbird.common.util.JsonKey;
import org.sunbird.util.LoggerUtil;

import java.text.MessageFormat;

public class GroupReadRequestValidator implements IRequestValidator {

  private static LoggerUtil logger = new LoggerUtil(GroupCreateRequestValidator.class);

  @Override
  public boolean validate(Request request) throws BaseException {
    logger.info(request.getContext(),"Validating the request read group "+ request.getRequest());
    try {
      ValidationUtil.validateRequestObject(request);
      ValidationUtil.validateMandatoryParamsWithType(
              request.getRequest(),
              Lists.newArrayList(JsonKey.GROUP_ID),
              String.class,
              true,
              JsonKey.REQUEST,request.getContext());
      return true;
    }catch (BaseException ex){
      BaseException baseException = new BaseException(ResponseCode.GS_RED02.getErrorCode(),ResponseCode.GS_RED02.getErrorMessage(),ex.getResponseCode());
      logger.error(request.getContext(), MessageFormat.format("GroupReadRequestValidator: Error Code: {0}, ErrMsg {1}",ResponseCode.GS_RED02.getErrorCode(),ex.getMessage()),baseException);
      throw baseException;
    }
  }
}
