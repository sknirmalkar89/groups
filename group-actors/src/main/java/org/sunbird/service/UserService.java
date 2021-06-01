package org.sunbird.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.response.Response;
import org.sunbird.common.util.JsonKey;

public interface UserService {

  public Response searchUserByIds(List<String> userIds, Map<String, Object> context)
      throws BaseException;

  public Response getSystemSettings() throws BaseException;

  public Response getOrganisationDetails(String orgId) throws BaseException;

  default void getUpdatedRequestHeader(Map<String, String> header, Map<String, Object> context) {
    if (null == header) {
      header = new HashMap<>();
    }
    header.put("Content-Type", "application/json");
    setTraceIdInHeader(header, context);
  }

  public static void setTraceIdInHeader(Map<String, String> header, Map<String, Object> context) {
    if (null != context) {
      header.put(JsonKey.X_TRACE_ENABLED, (String) context.get(JsonKey.X_TRACE_ENABLED));
      header.put(JsonKey.X_REQUEST_ID, (String) context.get(JsonKey.X_REQUEST_ID));
    }
  }
}
