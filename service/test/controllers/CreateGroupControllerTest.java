package controllers;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Result;

public class CreateGroupControllerTest extends TestHelper {

  // TODO - Mock Cassandra and bring this live.
  @Ignore
  @Test
  public void testCreateGroupPasses() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("name", "group");
    Map<String, Object> request = new HashMap<>();
    request.put("request", reqMap);
    Result result = performTest("/v1/group/create", "POST", request, headerMap);
    assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }

  @Test
  public void testMandatoryParamGroupName() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("description", "group");
    Map<String, Object> request = new HashMap<>();
    request.put("request", reqMap);
    Result result = performTest("/v1/group/create", "POST", request, headerMap);
    assertTrue(getResponseStatus(result) == Response.Status.BAD_REQUEST.getStatusCode());
  }

  @Test
  public void testGroupNameType() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("name", 123);
    Map<String, Object> request = new HashMap<>();
    request.put("request", reqMap);
    Result result = performTest("/v1/group/create", "POST", request, headerMap);
    assertTrue(getResponseStatus(result) == Response.Status.BAD_REQUEST.getStatusCode());
  }

  @Test
  public void testCreateGroupWithEmptyRequestObject() {
    Map<String, Object> request = new HashMap<>();
    request.put("name", "groupName");
    Result result = performTest("/v1/group/create", "POST", request, headerMap);
    assertTrue(getResponseStatus(result) == Response.Status.BAD_REQUEST.getStatusCode());
  }
}
