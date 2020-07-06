package validators;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.exception.BaseException;
import org.sunbird.request.Request;
import org.sunbird.util.JsonKey;

public class GroupCreateRequestValidator implements IRequestValidator {

  private static Logger logger = LoggerFactory.getLogger(GroupCreateRequestValidator.class);

  @Override
  public boolean validate(Request request) throws BaseException {
    logger.info(
        "GroupCreateRequestValidator:started validating the request with request"
            + request.getRequest());
    ValidationUtil.validateRequestObject(request);
    ValidationUtil.validateMandatoryParamsOfStringType(
        request, Lists.newArrayList(JsonKey.GROUP_NAME));
    // validate status and role of members if provided in request
    validateRoleAndStatus(request);
    return true;
  }

  private void validateRoleAndStatus(Request request) throws BaseException {
    Map<String, List<String>> paramValue = new HashMap<>();
    paramValue.put(JsonKey.STATUS, Lists.newArrayList(JsonKey.ACTIVE, JsonKey.INACTIVE));
    paramValue.put(JsonKey.ROLE, Lists.newArrayList(JsonKey.ADMIN, JsonKey.MEMBER));
    List<Map<String, Object>> memberList =
        (List<Map<String, Object>>) request.getRequest().get(JsonKey.MEMBERS);
    if (!memberList.isEmpty()) {
      for (Map<String, Object> member : memberList) {
        ValidationUtil.validateParamValue(
            member, Lists.newArrayList(JsonKey.STATUS, JsonKey.ROLE), paramValue);
      }
    }
  }
}
