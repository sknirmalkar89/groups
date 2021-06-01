package utils.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.models.EntryExitLogEvent;
import org.sunbird.common.request.Request;
import org.sunbird.common.response.Response;
import org.sunbird.common.response.ResponseParams;
import org.sunbird.common.util.JsonKey;
import org.sunbird.common.message.IResponseMessage;


public class PrintEntryExitLog {

  static Logger logger = LoggerFactory.getLogger(PrintEntryExitLog.class);
  private static ObjectMapper objectMapper = new ObjectMapper();

  public static void printEntryLog(Request request) {
    try {
      EntryExitLogEvent entryLogEvent = getLogEvent(request, "ENTRY","TRACE");
      List<Map<String, Object>> params = new ArrayList<>();
      Map<String, Object> reqMap = request.getRequest();
      Map<String, Object> newReqMap = new HashMap<>();
      String url = (String) request.getContext().get(JsonKey.URL);
      newReqMap.putAll(reqMap);
      params.add(newReqMap);
      getParamObject(request.getContext(), params);
      entryLogEvent.setEdataParams(params);
      logger.info(objectMapper.writeValueAsString(entryLogEvent));
    } catch (Exception ex) {
      logger.error("Exception occurred while logging entry log: {}", ex.getMessage());
    }
  }

  public static void printExitLogOnSuccessResponse(Request request, Response response) {
    try {
      EntryExitLogEvent exitLogEvent = getLogEvent(request, "EXIT","TRACE");
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
          getParamObject(resParam,params);
        }
      }
      getParamObject(request.getContext(), params);
      exitLogEvent.setEdataParams(params);
      logger.info(objectMapper.writeValueAsString(exitLogEvent));
    } catch (Exception ex) {
      logger.error("Exception occurred while logging exit log: {}", ex.getMessage());
    }
  }

  private static void getParamObject(Map<String,Object> context, List<Map<String, Object>> params) {
    for (Map.Entry<String,Object> itr: context.entrySet()) {
       if(null != itr.getValue()) {
         params.add(new HashMap<>() {{
           put(itr.getKey(), itr.getValue());
         }});
       }
    }
  }

  public static void printExitLogOnFailure(Request request, Exception ex) {
    try {
      EntryExitLogEvent exitLogEvent = getLogEvent(request, "EXIT","ERROR");
      String requestId = request.getRequestId();
      List<Map<String, Object>> params = new ArrayList<>();
      BaseException exception = null;
      if (null == ex || !(ex instanceof BaseException)) {
        ex =
            new BaseException(
                IResponseMessage.Key.SERVER_ERROR,
                    exception.getMessage(),
                ResponseCode.SERVER_ERROR.getResponseCode());
      }else{
        exception = (BaseException) ex;
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
        getParamObject(resParam,params);
      }
      getParamObject(request.getContext(), params);
      exitLogEvent.setEdataParams(params);
      logger.info(objectMapper.writeValueAsString(exitLogEvent));
    } catch (Exception e) {
      logger.error("Exception occurred while logging exit log: {}", e);
    }
  }

  private static EntryExitLogEvent getLogEvent(Request request, String logType, String eventType) {
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
    entryLogEvent.setEdata("system", eventType, requestId, entryLogMsg, null, null);
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
