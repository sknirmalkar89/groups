package controllers;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.util.JsonKey;
import play.mvc.Result;

public class DeleteGroupControllerTest extends BaseApplicationTest {

  @Before
  public void before() throws BaseException {
    setup(DummyActor.class);
  }

  @Test
  public void testDeleteroupPasses() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put(JsonKey.GROUP_ID, "group1");
    Map<String, Object> request = new HashMap<>();
    request.put("request", reqMap);

    Result result = performTest("/v1/group/delete", "POST", request);
    assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }

  @Test
  public void testEmptyGroupId() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put(JsonKey.GROUP_ID, "");
    Map<String, Object> request = new HashMap<>();
    request.put("request", reqMap);
    Result result = performTest("/v1/group/delete", "POST", request);
    assertTrue(getResponseStatus(result) == Response.Status.BAD_REQUEST.getStatusCode());
  }

  @Test
  public void testMandatoryParamGroupId() {
    Map<String, Object> reqMap = new HashMap<>();
    Map<String, Object> request = new HashMap<>();
    request.put("request", reqMap);
    Result result = performTest("/v1/group/delete", "POST", request);
    assertTrue(getResponseStatus(result) == Response.Status.BAD_REQUEST.getStatusCode());
  }
}
