package org.sunbird.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.junit.Assert;
import org.junit.Test;

public class ActivityConfigReaderTest {

  public static String ACTIVITY_CONFIG_JSON = "activityConfigTest.json";
  public static String ACTIVITY_CONFIG_JSON_WRONG = "activityConfigTestERROR.json";

  @Test
  public void loadActivityConfigMapSuccess() {
    try {
      InputStream in =
          ActivityConfigReader.class.getClassLoader().getResourceAsStream(ACTIVITY_CONFIG_JSON);

      ActivityConfigReader.loadActivityConfigMap(in);
    } catch (IOException e) {
      Assert.assertTrue(false);
    } catch (IllegalAccessException e) {
      Assert.assertTrue(false);
    } catch (InstantiationException e) {
      Assert.assertTrue(false);
    } catch (ClassNotFoundException e) {
      Assert.assertTrue(false);
    }
    SearchServiceUtil searchServiceUtilCourse =
        ActivityConfigReader.getServiceUtilClassName("Course");
    SearchServiceUtil searchServiceUtilTextBook =
        ActivityConfigReader.getServiceUtilClassName("TextBook");
    Assert.assertTrue(searchServiceUtilCourse.equals(searchServiceUtilTextBook));
    Assert.assertEquals(10, ActivityConfigReader.getFieldsLists(searchServiceUtilTextBook).size());
  }

  @Test
  public void loadActivityConfigMapFileDoesNotExist() {
    try {
      InputStream in =
          ActivityConfigReader.class
              .getClassLoader()
              .getResourceAsStream(ACTIVITY_CONFIG_JSON_WRONG);

      ActivityConfigReader.loadActivityConfigMap(in);
    } catch (IOException e) {
      Assert.assertTrue(true);
      Assert.assertEquals("Config file does not exist", e.getMessage());
    } catch (IllegalAccessException e) {
      Assert.assertTrue(false);
    } catch (InstantiationException e) {
      Assert.assertTrue(false);
    } catch (ClassNotFoundException e) {
      Assert.assertTrue(false);
    }
  }
}
