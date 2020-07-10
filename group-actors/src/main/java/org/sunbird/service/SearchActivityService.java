package org.sunbird.service;

import java.util.List;
import java.util.Map;
import org.sunbird.exception.BaseException;

public interface SearchActivityService {

  Map<String, Map<String, Object>> searchActivity(List<Map<String, Object>> activities)
      throws BaseException;
}
