package validators;

import com.google.common.collect.Lists;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.BaseException;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.Localizer;
import org.sunbird.message.ResponseCode;
import org.sunbird.request.Request;
import utils.JsonKey;

public class GroupCreateRequestValidator implements IRequestValidator {

  static Logger logger = LoggerFactory.getLogger(GroupCreateRequestValidator.class);
  static List<String> mandatoryParamsList = Lists.newArrayList(JsonKey.NAME);
  private Localizer localizer = Localizer.getInstance();

  private Request request;

  @Override
  public void validate(Request request) throws BaseException {
    this.request = request;
    logger.info(
        "GroupCreateRequestValidator:started validating the request with request"
            + request.getRequest());
    validateMandatoryParams();
  }

  private void validateMandatoryParams() throws BaseException {
    Map<String, Object> createGroupReq = request.getRequest();
    if (MapUtils.isEmpty(request.getRequest())) {
      logger.error("validateMandatoryParams:incorrect request provided");
      throw new BaseException(
          IResponseMessage.INVALID_REQUESTED_DATA,
          getLocalizedMessage(IResponseMessage.INVALID_REQUESTED_DATA, null),
          ResponseCode.CLIENT_ERROR.getCode());
    }
    for (String param : mandatoryParamsList) {
      if (!createGroupReq.containsKey(param)) {
        logger.error("validateMandatoryParams:incorrect request provided");
        throw new BaseException(
            IResponseMessage.INVALID_REQUESTED_DATA,
            MessageFormat.format(
                getLocalizedMessage(IResponseMessage.MISSING_MANDATORY_PARAMS, null), param),
            ResponseCode.CLIENT_ERROR.getCode());
      }
      if (!(createGroupReq.get(param) instanceof String)) {
        logger.error("validateMandatoryParams:incorrect request provided");
        throw new BaseException(
            IResponseMessage.INVALID_REQUESTED_DATA,
            MessageFormat.format(
                getLocalizedMessage(IResponseMessage.DATA_TYPE_ERROR, null), param, "string"),
            ResponseCode.CLIENT_ERROR.getCode());
      }
      validatePresence(param, (String) createGroupReq.get(param));
    }
  }

  private void validatePresence(String key, String value) throws BaseException {
    if (StringUtils.isBlank(value)) {
      logger.error("validatePresence:incorrect request provided");
      throw new BaseException(
          IResponseMessage.INVALID_REQUESTED_DATA,
          MessageFormat.format(
              getLocalizedMessage(IResponseMessage.MISSING_MANDATORY_PARAMS, null), key),
          ResponseCode.CLIENT_ERROR.getCode());
    }
  }

  private String getLocalizedMessage(String key, Locale locale) {
    return localizer.getMessage(key, locale);
  }
}
