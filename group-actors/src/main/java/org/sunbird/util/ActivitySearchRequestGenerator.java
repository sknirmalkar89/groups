package org.sunbird.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.models.ActivitySearchRequestConfig;
import org.sunbird.models.SearchRequest;
import org.sunbird.service.impl.SearchActivityServiceImpl;
import org.sunbird.util.helper.PropertiesCache;

public class ActivitySearchRequestGenerator {

  private static Logger logger = LoggerFactory.getLogger(ActivitySearchRequestGenerator.class);
  private static final String ACTIVITY_CONFIG_FILE = "activityConfig.json";

  static Map<String, String> activityServiceSearchUrlMap = new HashMap<>();
  static Map<String, Map<String, Object>> activityServiceSearchRequestMap = new HashMap<>();
  static Map<String, String> activityServiceResponseFieldMap = new HashMap<>();
  static Map<String, Map<String, String>> activityServiceHeaderMap = new HashMap<>();

  public static void init() {
    try {
      InputStream in =
          SearchActivityServiceImpl.class
              .getClassLoader()
              .getResourceAsStream(ACTIVITY_CONFIG_FILE);

      loadActivityConfigMap(in);

    } catch (IOException e) {
      logger.error("Error while loading activity Config" + e);
    }
  }

  static void loadActivityConfigMap(InputStream in) throws IOException {
    if (null != in) {
      JsonParser jsonParser = new JsonParser();
      JsonObject jsonObject = (JsonObject) jsonParser.parse(new InputStreamReader(in, "UTF-8"));
      Map<String, Object> activitiesMap = new Gson().fromJson(jsonObject, Map.class);
      List<Map<String, Object>> activitiesConfigList =
          (List<Map<String, Object>>) activitiesMap.get(JsonKey.ACTIVITIES);
      if (null != activitiesConfigList && !activitiesConfigList.isEmpty()) {
        initializeActivityContext(activitiesConfigList);
      }
    } else {
      throw new IOException("Config file does not exist");
    }
  }

  private static void initializeActivityContext(List<Map<String, Object>> activitiesConfigList) {
    for (Map<String, Object> activityConfig : activitiesConfigList) {
      String baseUrl = System.getenv((String) activityConfig.get(JsonKey.BASE_URL));
      String apiUrl = System.getenv((String) activityConfig.get(JsonKey.API_URL));
      Map<String, String> headers = (Map<String, String>) activityConfig.get(JsonKey.HEADERS);
      String authToken = System.getenv(headers.get(JsonKey.AUTHORIZATION));
      if (!StringUtils.isNotBlank(baseUrl)) {
        baseUrl =
            PropertiesCache.getInstance()
                .getProperty((String) activityConfig.get(JsonKey.BASE_URL));
      }
      if (!StringUtils.isNotBlank(apiUrl)) {
        apiUrl =
            PropertiesCache.getInstance().getProperty((String) activityConfig.get(JsonKey.API_URL));
      }
      if (!StringUtils.isNotBlank(authToken)) {
        authToken = PropertiesCache.getInstance().getProperty(headers.get(JsonKey.AUTHORIZATION));
      }
      headers.put(JsonKey.AUTHORIZATION, JsonKey.BEARER + authToken);
      for (String activityType : (List<String>) activityConfig.get("type")) {
        String searchApiUrl = baseUrl + apiUrl;
        activityServiceSearchUrlMap.put(activityType, searchApiUrl);
        activityServiceSearchRequestMap.put(
            searchApiUrl, (Map<String, Object>) activityConfig.get(JsonKey.REQUEST));
        activityServiceResponseFieldMap.put(
            searchApiUrl, (String) activityConfig.get(JsonKey.RESPONSE));
        activityServiceHeaderMap.put(searchApiUrl, headers);
      }
    }
  }

  public static List<ActivitySearchRequestConfig> generateActivitySearchRequest(
      List<Map<String, Object>> activities) {
    Map<String, List<String>> idListMap = new HashMap<>();
    for (Map<String, Object> activity : activities) {
      String apiUrl = (String) activityServiceSearchUrlMap.get(activity.get(JsonKey.TYPE));
      if (idListMap.containsKey(apiUrl)) {
        List<String> identifiers = idListMap.get(apiUrl);
        identifiers.add((String) activity.get(JsonKey.ID));
      } else {
        List<String> identifiers = new ArrayList<>();
        identifiers.add((String) activity.get(JsonKey.ID));
        idListMap.put(apiUrl, identifiers);
      }
    }
    List<ActivitySearchRequestConfig> activitySearchRequestConfigs =
        createMultiSearchActivityRequest(idListMap);

    return activitySearchRequestConfigs;
  }

  private static List<ActivitySearchRequestConfig> createMultiSearchActivityRequest(
      Map<String, List<String>> idListMap) {
    List<ActivitySearchRequestConfig> activitySearchRequestConfigs = new ArrayList<>();

    for (Map.Entry<String, List<String>> idList : idListMap.entrySet()) {
      ActivitySearchRequestConfig activitySearchRequestConfig = new ActivitySearchRequestConfig();
      SearchRequest searchRequest = new SearchRequest();
      String apiUrl = idList.getKey();
      List<String> identifiers = idList.getValue();
      Map<String, Object> requestPayload = new HashMap<>();
      Map<String, Object> filters = new HashMap<>();
      Map<String, Object> requestConfig = activityServiceSearchRequestMap.get(apiUrl);
      filters.put((String) requestConfig.get(JsonKey.ID), identifiers);
      requestPayload.put(JsonKey.FILTERS, filters);
      requestPayload.put(JsonKey.FIELDS, requestConfig.get(JsonKey.FIELDS));
      searchRequest.setRequest(requestPayload);
      activitySearchRequestConfig.setApiUrl(apiUrl);
      activitySearchRequestConfig.setSearchRequest(searchRequest);
      activitySearchRequestConfig.setResponse(activityServiceResponseFieldMap.get(apiUrl));
      activitySearchRequestConfig.setRequestHeader(activityServiceHeaderMap.get(apiUrl));
      activitySearchRequestConfig.setIdentifierKey((String) requestConfig.get(JsonKey.ID));
      activitySearchRequestConfigs.add(activitySearchRequestConfig);
    }
    return activitySearchRequestConfigs;
  }
}
