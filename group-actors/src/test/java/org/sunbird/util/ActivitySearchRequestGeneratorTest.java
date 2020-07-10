package org.sunbird.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.junit.Assert;
import org.junit.Test;
import org.sunbird.models.ActivitySearchRequestConfig;

public class ActivitySearchRequestGeneratorTest {

  public static String ACTIVITY_CONFIG_JSON = "activityConfigTest.json";
  public static String ACTIVITY_CONFIG_JSON_WRONG = "activityConfigTestERROR.json";

  @Test
  public void loadActivityConfigMapSuccess() {
    try {
      InputStream in =
          ActivitySearchRequestGeneratorTest.class
              .getClassLoader()
              .getResourceAsStream(ACTIVITY_CONFIG_JSON);

      ActivitySearchRequestGenerator.loadActivityConfigMap(in);
    } catch (IOException e) {
      Assert.assertTrue(false);
    }
    Assert.assertEquals(3, ActivitySearchRequestGenerator.activityServiceSearchUrlMap.size());
    Assert.assertEquals(2, ActivitySearchRequestGenerator.activityServiceSearchRequestMap.size());
    Assert.assertEquals(2, ActivitySearchRequestGenerator.activityServiceHeaderMap.size());
  }

  @Test
  public void loadActivityConfigMapFileDoesNotExist() {
    try {
      InputStream in =
          ActivitySearchRequestGeneratorTest.class
              .getClassLoader()
              .getResourceAsStream(ACTIVITY_CONFIG_JSON_WRONG);

      ActivitySearchRequestGenerator.loadActivityConfigMap(in);
    } catch (IOException e) {
      Assert.assertTrue(true);
      Assert.assertEquals("Config file does not exist", e.getMessage());
    }
  }

  @Test
  public void generateActivitySearchRequestTest() {
    try {
      InputStream in =
          ActivitySearchRequestGeneratorTest.class
              .getClassLoader()
              .getResourceAsStream(ACTIVITY_CONFIG_JSON);
      ActivitySearchRequestGenerator.loadActivityConfigMap(in);
      List<ActivitySearchRequestConfig> searchRequestConfigs =
          ActivitySearchRequestGenerator.generateActivitySearchRequest(createActivitiesParamter());
      Assert.assertEquals(1, searchRequestConfigs.size());

    } catch (IOException e) {
      Assert.assertTrue(false);
    }
  }

  private List<Map<String, Object>> createActivitiesParamter() {
    List<Map<String, Object>> activities = new ArrayList<>();
    Map<String, Object> activity1 = new HashMap<>();
    activity1.put(JsonKey.ID, "do_112470675618004992181");
    activity1.put(JsonKey.TYPE, "Course");

    Map<String, Object> activity2 = new HashMap<>();
    activity2.put(JsonKey.ID, "do_11304065892935270414");
    activity2.put(JsonKey.TYPE, "Textbook");
    activities.add(activity1);
    activities.add(activity2);
    return activities;
  }
}
