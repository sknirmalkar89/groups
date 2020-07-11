package org.sunbird.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SearchServiceUtil {

  public Map<String, Map<String, Object>> searchContent(
      List<String> activityIds, List<String> fields) throws JsonProcessingException;

  static Map<String, String> getUpdatedDefaultHeaders(Map<String, String> headers) {
    if (null == headers) {
      headers = new HashMap<>();
    }
    headers.put("Content-Type", "application/json");
    headers.put("Accept-Encoding", "application/gzip");
    headers.put("Accept-Charset", "UTF-8");
    return headers;
  }
}
