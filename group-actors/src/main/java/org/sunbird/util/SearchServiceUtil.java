package org.sunbird.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SearchServiceUtil {

  public Map<String, Map<String, Object>> searchContent(
      Map<String, String> activityIds, List<String> fields, Map<String, Object> reqContext)
      throws JsonProcessingException;

  static Map<String, String> getUpdatedDefaultHeaders(Map<String, String> headers) {
    if (null == headers) {
      headers = new HashMap<>();
    }
    headers.put("Content-Type", "application/json");
    headers.put("Accept-Encoding", "application/gzip");
    headers.put("Accept-Charset", "UTF-8");
    return headers;
  }

  public default void setTraceIdInHeader(Map<String, String> header, Map<String, Object> context) {
    if (null != context) {
      header.put(JsonKey.X_TRACE_ENABLED, (String) context.get(JsonKey.X_TRACE_ENABLED));
      header.put(JsonKey.X_REQUEST_ID, (String) context.get(JsonKey.X_REQUEST_ID));
    }
  }
}
