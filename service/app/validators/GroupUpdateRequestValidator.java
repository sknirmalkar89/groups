package validators;

import com.google.common.collect.Lists;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.exception.ValidationException;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.request.Request;
import org.sunbird.common.util.JsonKey;
import org.sunbird.util.LoggerUtil;

public class GroupUpdateRequestValidator implements IRequestValidator {

  private static LoggerUtil logger = new LoggerUtil(GroupUpdateRequestValidator.class);

  @Override
  public boolean validate(Request request) throws BaseException {
    logger.info(request.getContext(),"Validating the update group request "+request.getRequest());
    try {
      ValidationUtil.validateRequestObject(request);
      ValidationUtil.validateMandatoryParamsWithType(
              request.getRequest(),
              Lists.newArrayList(JsonKey.GROUP_ID),
              String.class,
              true,
              JsonKey.REQUEST,request.getContext());
      ValidationUtil.validateParamsWithType(request.getRequest(),Lists.newArrayList(JsonKey.MEMBERS,JsonKey.ACTIVITIES),
              Map.class,JsonKey.REQUEST,request.getContext());
      validateActivityList(request);

      return true;
    }catch (BaseException ex){
      BaseException baseException = new BaseException(ResponseCode.GS_UDT02.getErrorCode(),ResponseCode.GS_UDT02.getErrorMessage(),ex.getResponseCode());
      logger.error(request.getContext(), MessageFormat.format("GroupUpdateRequestValidator: Error Code: {0}, ErrMsg {1}",ResponseCode.GS_UDT02.getErrorCode(),ex.getMessage()),baseException);
      throw baseException;
    }
  }

  /**
   * checks mandatory param id and type of activity
   *
   * @param request
   * @throws BaseException
   */
  private void validateActivityList(Request request) throws BaseException {
    if (request.getRequest().containsKey(JsonKey.ACTIVITIES)) {
      checkDataTypeOfParam(
          request.getRequest().get(JsonKey.ACTIVITIES),
          JsonKey.ACTIVITIES,
          JsonKey.REQUEST,
          Map.class,request.getContext());
      Map<String, Object> activityList =
          (Map<String, Object>) request.getRequest().get(JsonKey.ACTIVITIES);
      if (MapUtils.isNotEmpty(activityList)) {
        // validate add activity list
        if (activityList.containsKey(JsonKey.ADD)) {
          checkDataTypeOfParam(
              activityList.get(JsonKey.ADD),
              JsonKey.ADD,
              JsonKey.REQUEST + "." + JsonKey.ACTIVITIES,
              List.class,request.getContext());
          List<Map<String, Object>> addActivity =
              (List<Map<String, Object>>) activityList.get(JsonKey.ADD);
          if (CollectionUtils.isNotEmpty(addActivity)) {
            for (Map<String, Object> activity : addActivity) {
              ValidationUtil.validateMandatoryParamsWithType(
                  activity,
                  Lists.newArrayList(JsonKey.ID, JsonKey.TYPE),
                  String.class,
                  true,
                  JsonKey.REQUEST
                      + "."
                      + JsonKey.ACTIVITIES
                      + "."
                      + JsonKey.ADD
                      + "["
                      + addActivity.indexOf(activity)
                      + "]",request.getContext());
            }
          }
        }
        // validate remove list
        if (activityList.containsKey(JsonKey.REMOVE)) {
          checkDataTypeOfParam(
              activityList.get(JsonKey.REMOVE),
              JsonKey.REMOVE,
              JsonKey.REQUEST + "." + JsonKey.ACTIVITIES,
              List.class,request.getContext());
        }
      }
    }
  }

  private void checkDataTypeOfParam(Object object, String key, String parentKey, Class type,Map<String,Object> reqCOntext) {
    if (ObjectUtils.isNotEmpty(object) && !(ValidationUtil.isInstanceOf(object.getClass(), type))) {
      logger.error(reqCOntext,"validateMandatoryParamsOfStringType:incorrect request provided");
      throw new ValidationException.ParamDataTypeError(parentKey + "." + key, type.getName());
    }
  }
}
