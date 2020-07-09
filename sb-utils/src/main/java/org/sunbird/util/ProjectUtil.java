package org.sunbird.util;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.util.helper.PropertiesCache;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This class will contains all the common utility methods.
 *
 * @author Manzarul
 */
public class ProjectUtil {
  
  public static PropertiesCache propertiesCache;
  
  static {
    propertiesCache = PropertiesCache.getInstance();
  }

  public enum Method {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH
  }

  /**
   * This method will provide formatted date
   *
   * @return
   */
  public static String getFormattedDate() {
    return getDateFormatter().format(new Date());
  }

  public static SimpleDateFormat getDateFormatter() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSSZ");
    simpleDateFormat.setLenient(false);
    return simpleDateFormat;
  }
  
  public static String getConfigValue(String key) {
    if (StringUtils.isNotBlank(System.getenv(key))) {
      return System.getenv(key);
    }
    return propertiesCache.readProperty(key);
  }
}
