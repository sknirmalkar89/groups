package org.sunbird.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.models.ActivitySearchRequestConfig;
import org.sunbird.service.SearchActivityService;
import org.sunbird.util.ActivitySearchRequestGenerator;
import org.sunbird.util.HttpClientUtil;
import org.sunbird.util.JsonKey;

public class SearchActivityServiceImpl implements SearchActivityService {
  Logger logger = LoggerFactory.getLogger(SearchActivityServiceImpl.class);

  private static SearchActivityService searchActivityService = null;
  private static ObjectMapper objectMapper = new ObjectMapper();

  public static SearchActivityService getInstance() {
    if (searchActivityService == null) {
      searchActivityService = new SearchActivityServiceImpl();
    }
    return searchActivityService;
  }

  @Override
  public Map<String, Map<String, Object>> searchActivity(List<Map<String, Object>> activities) {

    Map<String, Map<String, Object>> activityInfoMap = new HashMap<>();

    List<ActivitySearchRequestConfig> activitySearchRequestConfigs =
        ActivitySearchRequestGenerator.generateActivitySearchRequest(activities);
    try {
      for (ActivitySearchRequestConfig activitySearchRequest : activitySearchRequestConfigs) {
        String response =
            HttpClientUtil.post(
                activitySearchRequest.getApiUrl(),
                objectMapper.writeValueAsString(activitySearchRequest.getSearchRequest()),
                activitySearchRequest.getRequestHeader());
        if (StringUtils.isNotBlank(response)) {
          String responseCode = JsonPath.read(response, "$.responseCode");
          if ("OK".equals(responseCode)) {
            List<Map<String, Object>> activityInfoList = new ArrayList<>();
            try {
              activityInfoList = JsonPath.read(response, activitySearchRequest.getResponse());
              for (Map<String, Object> activityInfo : activityInfoList) {
                activityInfoMap.put(
                    (String) activityInfo.get(activitySearchRequest.getIdentifierKey()),
                    activityInfo);
              }
            } catch (Exception ex) {
              logger.info(
                  "No info found for the activity : "
                      + ((Map<String, Object>)
                              activitySearchRequest
                                  .getSearchRequest()
                                  .getRequest()
                                  .get(JsonKey.FILTERS))
                          .get(activitySearchRequest.getIdentifierKey()));
            }
          }
        }
      }
    } catch (Exception ex) {
      logger.error("Error while fetching user details through user service" + ex);
    }

    return activityInfoMap;
  }
}
