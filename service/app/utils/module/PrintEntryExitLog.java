package utils.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.exception.BaseException;
import org.sunbird.message.ResponseCode;
import org.sunbird.models.EntryExitLogEvent;
import org.sunbird.request.Request;
import org.sunbird.response.Response;
import org.sunbird.response.ResponseParams;
import org.sunbird.util.JsonKey;

public class PrintEntryExitLog {

  static Logger logger = LoggerFactory.getLogger(PrintEntryExitLog.class);
  private static ObjectMapper objectMapper = new ObjectMapper();

  public static void printEntryLog(Request request) {
    try {
      EntryExitLogEvent entryLogEvent = getLogEvent(request, "ENTRY");
      List<Map<String, Object>> params = new ArrayList<>();
      Map<String, Object> reqMap = request.getRequest();
      Map<String, Object> newReqMap = new HashMap<>();
      String url = (String) request.getContext().get(JsonKey.URL);
      newReqMap.putAll(reqMap);
      params.add(newReqMap);
      entryLogEvent.setEdataParams(params);
      entryLogEvent.setEdataContext(request.getContext());
      logger.info(objectMapper.writeValueAsString(entryLogEvent));
    } catch (Exception ex) {
      logger.error("Exception occurred while logging entry log: {}", ex.getMessage());
    }
  }

  public static void printExitLogOnSuccessResponse(Request request, Response response) {
    try {
      EntryExitLogEvent exitLogEvent = getLogEvent(request, "EXIT");
      String url = (String) request.getContext().get(JsonKey.URL);
      List<Map<String, Object>> params = new ArrayList<>();
      if (null != response) {
        if (MapUtils.isNotEmpty(response.getResult())) {
          Map<String, Object> resMap = response.getResult();
          Map<String, Object> newRespMap = new HashMap<>();
          newRespMap.putAll(resMap);
          params.add(newRespMap);
        }
        if (null != response.getParams()) {
          Map<String, Object> resParam = new HashMap<>();
          resParam.putAll(objectMapper.convertValue(response.getParams(), Map.class));
          resParam.put(JsonKey.RESPONSE_CODE, response.getResponseCode());
          params.add(resParam);
        }
      }
      exitLogEvent.setEdataParams(params);
      exitLogEvent.setEdataContext(request.getContext());
      logger.info(objectMapper.writeValueAsString(exitLogEvent));
    } catch (Exception ex) {
      logger.error("Exception occurred while logging exit log: {}", ex.getMessage());
    }
  }

  public static void printExitLogOnFailure(Request request, BaseException exception) {
    try {
      EntryExitLogEvent exitLogEvent = getLogEvent(request, "EXIT");
      String requestId = request.getRequestId();
      List<Map<String, Object>> params = new ArrayList<>();
      if (null == exception) {
        exception =
            new BaseException(
                ResponseCode.internalError.getErrorCode(),
                ResponseCode.internalError.getErrorMessage(),
                ResponseCode.SERVER_ERROR.getResponseCode());
      }

      ResponseCode code = ResponseCode.getResponse(exception.getCode());
      if (code == null) {
        code = ResponseCode.SERVER_ERROR;
      }
      ResponseParams responseParams =
          createResponseParamObj(code, exception.getMessage(), requestId);
      if (responseParams != null) {
        responseParams.setStatus(JsonKey.FAILED);
        if (exception.getCode() != null) {
          responseParams.setStatus(JsonKey.FAILED);
        }
        if (!StringUtils.isBlank(responseParams.getErrmsg())
            && responseParams.getErrmsg().contains("{0}")) {
          responseParams.setErrmsg(exception.getMessage());
        }
      }
      if (null != responseParams) {
        Map<String, Object> resParam = new HashMap<>();
        resParam.putAll(objectMapper.convertValue(responseParams, Map.class));
        resParam.put(JsonKey.RESPONSE_CODE, exception.getResponseCode());
        params.add(resParam);
      }
      exitLogEvent.setEdataParams(params);
      exitLogEvent.setEdataContext(request.getContext());
      logger.info(objectMapper.writeValueAsString(exitLogEvent));
    } catch (Exception ex) {
      logger.error("Exception occurred while logging exit log: {}", ex);
    }
  }

  private static EntryExitLogEvent getLogEvent(Request request, String logType) {
    EntryExitLogEvent entryLogEvent = new EntryExitLogEvent();
    entryLogEvent.setEid("LOG");
    String url = (String) request.getContext().get(JsonKey.URL);
    String entryLogMsg =
        logType
            + " LOG: method : "
            + request.getContext().get(JsonKey.METHOD)
            + ", url: "
            + url
            + " , For Operation : "
            + request.getOperation();
    String requestId = (String)( request.getContext() != null ? request.getContext().get(JsonKey.X_REQUEST_ID) : "");
    entryLogEvent.setEdata("system", "trace", requestId, entryLogMsg, null, null);
    return entryLogEvent;
  }

  public static ResponseParams createResponseParamObj(
      ResponseCode code, String customMessage, String requestId) {
    ResponseParams params = new ResponseParams();
    if (code.getResponseCode() != 200) {
      params.setErr(code.getErrorCode());
      params.setErrmsg(
          StringUtils.isNotBlank(customMessage) ? customMessage : code.getErrorMessage());
    }
    params.setStatus(ResponseCode.getHeaderResponseCode(code.getResponseCode()).name());
    params.setMsgid(requestId);
    return params;
  }
}
