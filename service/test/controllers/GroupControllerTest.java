package controllers;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.junit.Test;
import play.mvc.Result;

public class GroupControllerTest extends TestHelper {

  @Test
  public void testCreateGroupPasses() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("name", "group");
    Result result = performTest("/v1/group/create", "POST", reqMap, headerMap);
    assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }
}
