package utils.module;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.ResponseHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.sunbird.common.exception.AuthorizationException;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.IResponseMessage;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.request.HeaderParam;
import org.sunbird.common.util.JsonKey;
import org.sunbird.util.LoggerUtil;
import org.sunbird.util.SystemConfigUtil;
import org.sunbird.util.helper.PropertiesCache;
import play.http.ActionCreator;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public class OnRequestHandler implements ActionCreator {

  private static LoggerUtil logger = new LoggerUtil(OnRequestHandler.class);
  private static String custodianOrgHashTagId;
  private ObjectMapper mapper = new ObjectMapper();

  @Override
  public Action createAction(Http.Request request, Method method) {
    return new Action.Simple() {
      @Override
      public CompletionStage<Result> call(Http.Request request) {
        Optional<String> optionalMessageId = request.getHeaders().get(JsonKey.REQUEST_MESSAGE_ID);
        String requestId;
        if (optionalMessageId.isPresent()) {
          requestId = optionalMessageId.get();
        } else {
          UUID uuid = UUID.randomUUID();
          requestId = uuid.toString();
        }
        Optional<String> optionalTraceId =
            request.getHeaders().get(HeaderParam.X_REQUEST_ID.getName());
        if (optionalTraceId.isPresent()) {
          MDC.put(
              JsonKey.X_REQUEST_ID,
              request.getHeaders().get(HeaderParam.X_REQUEST_ID.getName()).get());
        } else {
          MDC.put(JsonKey.X_REQUEST_ID, requestId);
        }
        CompletionStage<Result> result;
        Map userAuthentication = RequestInterceptor.verifyRequestData(request);
        String message = (String) userAuthentication.get(JsonKey.USER_ID);
        if (userAuthentication.get(JsonKey.MANAGED_FOR) != null) {
          request =
              request.addAttr(
                  Attrs.MANAGED_FOR, (String) userAuthentication.get(JsonKey.MANAGED_FOR));
        }
        request = initializeContext(request, message, requestId);
        if (!JsonKey.USER_UNAUTH_STATES.contains(message)) {
          request = request.addAttr(Attrs.USERID, message);
          result = delegate.call(request);
        } else if (JsonKey.UNAUTHORIZED.equals(message)) {
          result = getAuthorizedResult(request, message);
        } else {
          result = delegate.call(request);
        }

        return result.thenApply(res -> res.withHeader("Access-Control-Allow-Origin", "*"));
      }
    };
  }

  /**
   *  Set Error code specific to operation
   * @param request
   * @param errorMessage
   * @return
   */
  public CompletionStage<Result> getAuthorizedResult(Http.Request request, String errorMessage) {
    String contextStr= null;
    if (request.attrs() != null && request.attrs().containsKey(Attrs.CONTEXT)) {
      contextStr = (String) request.attrs().get(Attrs.CONTEXT);
    }
    try {
      Map<String, Object> contextObject = new HashMap<>();
      if (StringUtils.isNotBlank(contextStr)) {
        contextObject = mapper.readValue(contextStr, Map.class);
      }
      logger.error((Map<String, Object>) contextObject.get(JsonKey.CONTEXT), "Data error found--" + errorMessage);
      ResponseCode responseCode = ResponseCode.unAuthorized;
      Result result =
              ResponseHandler.handleFailureResponse(new AuthorizationException.NotAuthorized(responseCode), request);
      return CompletableFuture.completedFuture(result);
    }catch (Exception ex){
        logger.error("Error process set request context" ,ex);
        throw new BaseException(
                IResponseMessage.SERVER_ERROR,
                IResponseMessage.INTERNAL_ERROR,
                ResponseCode.SERVER_ERROR.getCode());
      }
   }


  /**
   * Set the Context paramter to the request
   *
   * @param httpReq
   * @param userId
   */
  Http.Request initializeContext(Http.Request httpReq, String userId, String requestId) {
    Map<String, Object> requestContext = new WeakHashMap<>();
    try {
      String env = getEnv(httpReq);
      requestContext.put(JsonKey.ENV, env);
      requestContext.put(JsonKey.REQUEST_TYPE, JsonKey.API_CALL);
      requestContext.put(JsonKey.URL, httpReq.uri());
      requestContext.put(JsonKey.METHOD, httpReq.method());
      Optional<String> optionalChannel = httpReq.getHeaders().get(HeaderParam.CHANNEL_ID.getName());
      String channel;
      if (optionalChannel.isPresent()) {
        channel = optionalChannel.get();
      } else {
        channel = getCustodianOrgHashTagId();
      }
      requestContext.put(JsonKey.CHANNEL, channel);
      requestContext.put(JsonKey.REQUEST_ID, requestId);
      requestContext.put(JsonKey.REQUEST_MESSAGE_ID, requestId);

      requestContext.putAll(cacheTelemetryPdata());
      Optional<String> optionalAppId = httpReq.getHeaders().get(HeaderParam.X_APP_ID.getName());
      if (optionalAppId.isPresent()) {
        requestContext.put(JsonKey.APP_ID, optionalAppId.get());
      }
      Optional<String> optionalDeviceId =
          httpReq.getHeaders().get(HeaderParam.X_Device_ID.getName());
      if (optionalDeviceId.isPresent()) {
        requestContext.put(JsonKey.DEVICE_ID, optionalDeviceId.get());
      }
      Optional<String> optionalTraceEnabled =
          httpReq.getHeaders().get(HeaderParam.X_TRACE_ENABLED.getName());
      if (optionalTraceEnabled.isPresent()) {
        requestContext.put(JsonKey.X_TRACE_ENABLED, optionalTraceEnabled.get());
      }
      Optional<String> optionalTraceId =
          httpReq.getHeaders().get(HeaderParam.X_REQUEST_ID.getName());
      if (optionalTraceId.isPresent()) {
        requestContext.put(JsonKey.X_REQUEST_ID, optionalTraceId.get());
        httpReq = httpReq.addAttr(Attrs.X_REQUEST_ID, optionalTraceId.get());
      } else {
        httpReq = httpReq.addAttr(Attrs.X_REQUEST_ID, requestId);
        requestContext.put(JsonKey.X_REQUEST_ID, requestId);
      }
      if (null != userId && !JsonKey.USER_UNAUTH_STATES.contains(userId)) {
        requestContext.put(JsonKey.ACTOR_ID, userId);
        requestContext.put(JsonKey.ACTOR_TYPE, StringUtils.capitalize(JsonKey.USER));
      } else {
        Optional<String> optionalConsumerId =
            httpReq.getHeaders().get(HeaderParam.X_Consumer_ID.getName());
        String consumerId;
        if (optionalConsumerId.isPresent()) {
          consumerId = optionalConsumerId.get();
        } else {
          consumerId = JsonKey.DEFAULT_CONSUMER_ID;
        }
        requestContext.put(JsonKey.ACTOR_ID, consumerId);
        requestContext.put(JsonKey.ACTOR_TYPE, StringUtils.capitalize(JsonKey.CONSUMER));
      }
      Map<String, Object> map = new WeakHashMap<>();
      map.put(JsonKey.CONTEXT, requestContext);
      return httpReq.addAttr(Attrs.CONTEXT, mapper.writeValueAsString(map));
    } catch (Exception ex) {
      logger.error(requestContext,"Error process set request context" + ex.getMessage());
      throw new BaseException(
          IResponseMessage.SERVER_ERROR,
          IResponseMessage.INTERNAL_ERROR,
          ResponseCode.SERVER_ERROR.getCode());
    }
  }

  private String getCustodianOrgHashTagId() {
    if (null != custodianOrgHashTagId) {
      return custodianOrgHashTagId;
    }
    synchronized (OnRequestHandler.class) {
      if (custodianOrgHashTagId == null) {
        try {
          // Get hash tag ID of custodian org
          Map<String, Object> custodianOrgDetails = SystemConfigUtil.getCustodianOrgDetails();
          if (null != custodianOrgDetails && !custodianOrgDetails.isEmpty()) {
            custodianOrgHashTagId = (String) custodianOrgDetails.get(JsonKey.HASH_TAG_ID);
          } else {
            custodianOrgHashTagId = "";
          }

        } catch (Exception ex) {
          custodianOrgHashTagId = "";
        }
      }
    }
    return custodianOrgHashTagId;
  }

  private static Map<String, Object> cacheTelemetryPdata() {
    Map<String, Object> telemetryPdata = new HashMap<>();
    telemetryPdata.put("telemetry_pdata_id", PropertiesCache.getConfigValue("telemetry_pdata_id"));
    telemetryPdata.put(
        "telemetry_pdata_pid", PropertiesCache.getConfigValue("telemetry_pdata_pid"));
    telemetryPdata.put(
        "telemetry_pdata_ver", PropertiesCache.getConfigValue("telemetry_pdata_ver"));
    return telemetryPdata;
  }

  private static String getEnv(Http.Request request) {
    String uri = request.uri();
    String env = "";
    if (uri.startsWith("/v1/group")) {
      env = JsonKey.GROUPS;
    }
    return env;
  }
}
