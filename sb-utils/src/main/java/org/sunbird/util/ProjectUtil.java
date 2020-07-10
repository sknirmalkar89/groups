package org.sunbird.util;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.util.helper.PropertiesCache;

/**
 * This class will contains all the common utility methods.
 *
 * @author Manzarul
 */
public class ProjectUtil {
  
  public static final PropertiesCache propertiesCache;
  
  static {
    propertiesCache = PropertiesCache.getInstance();
  }
  
  public static String getConfigValue(String key) {
    if (StringUtils.isNotBlank(System.getenv(key))) {
      return System.getenv(key);
    }
    return propertiesCache.readProperty(key);
  }
}
