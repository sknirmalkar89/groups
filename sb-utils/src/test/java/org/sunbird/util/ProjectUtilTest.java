package org.sunbird.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class ProjectUtilTest {

  @Test
  public void testGetConfigValueWithExistsInPropertyFile() {
    String exists = ProjectUtil.getConfigValue(JsonKey.SUNBIRD_HEALTH_CHECK_ENABLE);
    assertEquals("true", exists);
  }

  @Test
  public void testGetConfigValueWithNotExistsInPropertyFile() {
    String exists = ProjectUtil.getConfigValue("sunbird_health_check_not_enable");
    assertNull(exists);
  }
}
