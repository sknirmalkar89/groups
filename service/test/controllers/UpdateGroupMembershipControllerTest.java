package controllers;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.util.JsonKey;
import play.mvc.Result;

public class UpdateGroupMembershipControllerTest extends BaseApplicationTest {

  @Before
  public void before() throws BaseException {
    setup(DummyActor.class);
  }

  @Test
  public void testUpdateGroupMembershipPasses() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put(JsonKey.USER_ID, "user1");
    Map<String, Object> request = new HashMap<>();
    List<Map<String, Object>> groups = new ArrayList<>();
    Map<String, Object> group = new HashMap<>();
    group.put(JsonKey.ROLE, JsonKey.MEMBER);
    group.put(JsonKey.STATUS, JsonKey.ACTIVE);
    group.put(JsonKey.GROUP_ID, "group1");
    groups.add(group);
    reqMap.put(JsonKey.GROUPS, groups);
    request.put("request", reqMap);

    Result result = performTest("/v1/group/membership/update", "PATCH", request);
    assertTrue(getResponseStatus(result) == Response.Status.OK.getStatusCode());
  }

  @Test
  public void testEmptyUserId() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put(JsonKey.USER_ID, "");
    Map<String, Object> request = new HashMap<>();
    request.put("request", reqMap);
    Result result = performTest("/v1/group/membership/update", "PATCH", request);
    System.out.println(getResponseStatus(result));
    assertTrue(getResponseStatus(result) == Response.Status.BAD_REQUEST.getStatusCode());
  }

  @Test
  public void testMandatoryUserId() {
    Map<String, Object> reqMap = new HashMap<>();
    Map<String, Object> request = new HashMap<>();
    request.put("request", reqMap);
    Result result = performTest("/v1/group/membership/update", "PATCH", request);
    assertTrue(getResponseStatus(result) == Response.Status.BAD_REQUEST.getStatusCode());
  }

  @Test
  public void testMandatoryGroups() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put(JsonKey.USER_ID, "user1");
    Map<String, Object> request = new HashMap<>();
    request.put("request", reqMap);
    Result result = performTest("/v1/group/membership/update", "PATCH", request);
    assertTrue(getResponseStatus(result) == Response.Status.BAD_REQUEST.getStatusCode());
  }

  @Test
  public void testMandatoryEmptyGroups() {
    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put(JsonKey.USER_ID, "user1");
    reqMap.put(JsonKey.GROUPS, new ArrayList<>());
    Map<String, Object> request = new HashMap<>();
    request.put("request", reqMap);
    Result result = performTest("/v1/group/membership/update", "PATCH", request);
    assertTrue(getResponseStatus(result) == Response.Status.BAD_REQUEST.getStatusCode());
  }
}
