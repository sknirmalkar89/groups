package validators;

import com.google.common.collect.Lists;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.request.Request;
import org.sunbird.common.util.JsonKey;
import org.sunbird.util.LoggerUtil;

public class GroupCreateRequestValidator implements IRequestValidator {

  private static LoggerUtil logger = new LoggerUtil(GroupCreateRequestValidator.class);

  @Override
  public boolean validate(Request request) throws BaseException {
    logger.info(request.getContext(), MessageFormat.format("Validating the create group request {0}", request.getRequest()));
    try {
      ValidationUtil.validateRequestObject(request);
      ValidationUtil.validateMandatoryParamsWithType(
              request.getRequest(),
              Lists.newArrayList(JsonKey.GROUP_NAME),
              String.class,
              true,
              JsonKey.REQUEST,request.getContext());
      ValidationUtil.validateParamsWithType(request.getRequest(),Lists.newArrayList(JsonKey.MEMBERS,JsonKey.ACTIVITIES),
              List.class,JsonKey.REQUEST,request.getContext());
      // validate value of status and role of members and userId if provided in request
      validateRoleAndStatus(request);
      validateActivityList(request);

    }catch (BaseException ex){
      BaseException baseException = new BaseException(ResponseCode.GS_CRT02.getErrorCode(),ResponseCode.GS_CRT02.getErrorMessage(),ex.getResponseCode());
      logger.error(request.getContext(),MessageFormat.format("GroupCreateRequestValidator: Error Code: {0}, ErrMsg {1}",ResponseCode.GS_CRT02.getErrorCode(),ex.getMessage()),baseException);
      throw baseException;
    }
    return true;
  }

  /**
   * validates UserId, role, status
   *
   * @param request
   * @throws BaseException
   */
  private void validateRoleAndStatus(Request request) throws BaseException {
    Map<String, List<String>> paramValue = new HashMap<>();
    paramValue.put(JsonKey.STATUS, Lists.newArrayList(JsonKey.ACTIVE, JsonKey.INACTIVE));
    paramValue.put(JsonKey.ROLE, Lists.newArrayList(JsonKey.ADMIN, JsonKey.MEMBER));
    List<Map<String, Object>> memberList =
        (List<Map<String, Object>>) request.getRequest().get(JsonKey.MEMBERS);
    if (CollectionUtils.isNotEmpty(memberList)) {
      for (Map<String, Object> member : memberList) {
        ValidationUtil.validateMandatoryParamsWithType(
            member,
            Lists.newArrayList(JsonKey.USER_ID),
            String.class,
            true,
            JsonKey.MEMBERS + "[" + memberList.indexOf(member) + "]",request.getContext());
        ValidationUtil.validateParamValue(
            member,
            Lists.newArrayList(JsonKey.STATUS, JsonKey.ROLE),
            paramValue,
            JsonKey.MEMBERS + "[" + memberList.indexOf(member) + "]",request.getContext());
      }
    }
  }

  /**
   * checks mandatory param id and type of activity
   *
   * @param request
   * @throws BaseException
   */
  private void validateActivityList(Request request) throws BaseException {
    List<Map<String, Object>> activityList =
        (List<Map<String, Object>>) request.getRequest().get(JsonKey.ACTIVITIES);
    if (CollectionUtils.isNotEmpty(activityList)) {
      for (Map<String, Object> activity : activityList) {
        ValidationUtil.validateMandatoryParamsWithType(
            activity,
            Lists.newArrayList(JsonKey.ID, JsonKey.TYPE),
            String.class,
            true,
            JsonKey.ACTIVITIES + "[" + activityList.indexOf(activity) + "]", request.getContext());
      }
    }
  }
}
