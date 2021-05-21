package org.sunbird;

import java.util.Map;
import org.slf4j.MDC;
import org.sunbird.util.JsonKey;

public class BaseLogger {

  /**
   * sets requestId in Sl4j MDC
   *
   * @param trace
   */
  public void setReqId(Map<String, Object> trace) {
    MDC.clear();
    MDC.put(JsonKey.REQUEST_MESSAGE_ID, (String) trace.get(JsonKey.X_REQUEST_ID));
  }

  public String getReqId() {
    return MDC.get(JsonKey.REQUEST_MESSAGE_ID);
  }
}
