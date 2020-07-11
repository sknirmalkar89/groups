package org.sunbird.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.models.SearchRequest;
import org.sunbird.util.helper.PropertiesCache;

public class ContentSearchUtil implements SearchServiceUtil {

  private static String contentSearchURL = null;
  private static ObjectMapper objectMapper = new ObjectMapper();
  private static Map<String, String> headers;

  static {
    String baseUrl = System.getenv(JsonKey.SUNBIRD_CS_BASE_URL);
    String searchPath = System.getenv(JsonKey.SUNBIRD_CS_SEARCH_URL);
    String authKey = System.getenv(JsonKey.SUNBIRD_CS_AUTH_KEY);
    if (StringUtils.isBlank(searchPath))
      searchPath = PropertiesCache.getInstance().getProperty(JsonKey.SUNBIRD_CS_SEARCH_URL);
    if (StringUtils.isBlank(baseUrl))
      searchPath = PropertiesCache.getInstance().getProperty(JsonKey.SUNBIRD_CS_BASE_URL);
    if (StringUtils.isNotBlank(authKey)) {
      Map<String, String> headerMap = new HashMap<>();
      headerMap.put(JsonKey.AUTHORIZATION, JsonKey.BEARER + authKey);
      headers = SearchServiceUtil.getUpdatedDefaultHeaders(headerMap);
    } else {
      headers = SearchServiceUtil.getUpdatedDefaultHeaders(headers);
    }
    contentSearchURL = baseUrl + searchPath;
  }

  @Override
  public Map<String, Map<String, Object>> searchContent(
      List<String> activityIds, List<String> fields) throws JsonProcessingException {
    Map<String, Map<String, Object>> activityInfoMap = new HashMap<>();
    SearchRequest request = new SearchRequest();
    Map<String, Object> filters = new HashMap<>();
    filters.put(JsonKey.IDENTIFIER, activityIds);
    request.getRequest().put(JsonKey.FIELDS, fields);
    request.getRequest().put(JsonKey.FILTERS, filters);
    String response =
        HttpClientUtil.post(contentSearchURL, objectMapper.writeValueAsString(request), headers);
    if (StringUtils.isNotBlank(response)) {
      JsonNode jsonNode = objectMapper.readTree(response);
      if (null != jsonNode.get(JsonKey.RESULT)
          && null != jsonNode.get(JsonKey.RESULT).get(JsonKey.CONTENT)) {

        ArrayNode jsonArrayNode = (ArrayNode) jsonNode.get(JsonKey.RESULT).get(JsonKey.CONTENT);
        for (JsonNode activityJsonNode : jsonArrayNode) {
          String key =
              activityJsonNode.get(JsonKey.CONTENT_TYPE).asText()
                  + activityJsonNode.get(JsonKey.IDENTIFIER).asText();
          activityInfoMap.put(
              key,
              objectMapper.convertValue(
                  activityJsonNode, new TypeReference<Map<String, Object>>() {}));
        }
      }
    }

    return activityInfoMap;
  }
}
