package org.sunbird.actors;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import java.time.Duration;
import java.util.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.Application;
import org.sunbird.cassandra.CassandraOperation;
import org.sunbird.cassandraimpl.CassandraOperationImpl;
import org.sunbird.exception.BaseException;
import org.sunbird.helper.ServiceFactory;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.Localizer;
import org.sunbird.models.ActorOperations;
import org.sunbird.request.Request;
import org.sunbird.response.Response;
import org.sunbird.util.JsonKey;
import org.sunbird.util.SystemConfigUtil;
import org.sunbird.util.helper.PropertiesCache;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
  CassandraOperation.class,
  CassandraOperationImpl.class,
  ServiceFactory.class,
  Localizer.class,
  Application.class,
  SystemConfigUtil.class,
  PropertiesCache.class
})
@PowerMockIgnore({"javax.management.*", "jdk.internal.reflect.*"})
public class UpdateGroupActorTest extends BaseActorTest {

  private final Props props = Props.create(UpdateGroupActor.class);
  private Logger logger = LoggerFactory.getLogger(UpdateGroupActorTest.class);
  public static PropertiesCache propertiesCache;

  @Before
  public void setUp() throws Exception {

    PowerMockito.mockStatic(Localizer.class);
    when(Localizer.getInstance()).thenReturn(null);

    mockCacheActor();

    PowerMockito.mockStatic(SystemConfigUtil.class);
    PowerMockito.mockStatic(PropertiesCache.class);
    propertiesCache = mock(PropertiesCache.class);
    when(PropertiesCache.getInstance()).thenReturn(propertiesCache);
    when(PropertiesCache.getInstance().getProperty(JsonKey.MAX_GROUP_MEMBERS_LIMIT))
        .thenReturn("4");
    when(PropertiesCache.getInstance().getProperty(JsonKey.MAX_ACTIVITY_LIMIT)).thenReturn("4");
  }

  @Test
  public void testUpdateGroup() {
    TestKit probe = new TestKit(system);
    ActorRef subject = system.actorOf(props);
    PowerMockito.mockStatic(ServiceFactory.class);
    CassandraOperation cassandraOperation = mock(CassandraOperationImpl.class);
    when(ServiceFactory.getInstance()).thenReturn(cassandraOperation);

    try {
      when(cassandraOperation.updateRecord(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyObject()))
          .thenReturn(getCassandraResponse());
      when(cassandraOperation.batchInsert(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyList()))
          .thenReturn(getCassandraResponse());
      when(cassandraOperation.updateAddSetRecord(
              Mockito.anyString(),
              Mockito.anyString(),
              Mockito.anyMap(),
              Mockito.anyString(),
              Mockito.anyObject()))
          .thenReturn(getCassandraResponse())
          .thenReturn(getCassandraResponse());
      when(cassandraOperation.updateRemoveSetRecord(
              Mockito.anyString(),
              Mockito.anyString(),
              Mockito.anyMap(),
              Mockito.anyString(),
              Mockito.anyObject()))
          .thenReturn(getCassandraResponse());
      when(cassandraOperation.batchUpdate(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyList()))
          .thenReturn(getCassandraResponse());
      when(cassandraOperation.executeSelectQuery(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyMap(), Mockito.anyObject()))
          .thenReturn(memberSizeResponse());
      when(cassandraOperation.getRecordsByPrimaryKeys(
              Mockito.anyString(),
              Matchers.eq("group_member"),
              Mockito.anyList(),
              Mockito.anyString()))
          .thenReturn(getMemberResponse());
      when(cassandraOperation.getRecordById(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
          .thenReturn(getGroupsDetailsResponse());
      when(cassandraOperation.deleteRecord(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
          .thenReturn(getCassandraResponse());
    } catch (BaseException be) {
      Assert.assertTrue(false);
    }

    Request reqObj = updateGroupReq();
    subject.tell(reqObj, probe.getRef());
    Response res = probe.expectMsgClass(Duration.ofSeconds(30), Response.class);
    Assert.assertTrue(null != res && res.getResponseCode() == 200);
  }

  @Test
  public void testUpdateGroupWithMaxMemberExceeded() {
    TestKit probe = new TestKit(system);
    ActorRef subject = system.actorOf(props);
    subject.tell(getUpdateGroupReqForMaxMember(), probe.getRef());
    Response res = probe.expectMsgClass(Duration.ofSeconds(100), Response.class);
    Assert.assertTrue(null != res && res.getResponseCode() == 200);
    Map error = (Map) res.getResult().get(JsonKey.ERROR);
    List errorList = (List) error.get(JsonKey.MEMBERS);
    Assert.assertEquals(
        ((Map) errorList.get(0)).get(JsonKey.ERROR_CODE),
        IResponseMessage.Key.GS_UDT_06);
  }

  @Test
  public void testUpdateGroupWithMaxActivityExceeded() {
    TestKit probe = new TestKit(system);
    ActorRef subject = system.actorOf(props);
    subject.tell(getUpdateGroupReqForMaxActivity(), probe.getRef());
    Response res = probe.expectMsgClass(Duration.ofSeconds(20), Response.class);
    Assert.assertTrue(null != res && res.getResponseCode() == 200);
    System.out.println(res.getResult().get(JsonKey.ERROR));
    Map error = (Map) res.getResult().get(JsonKey.ERROR);
    List errorList = (List) error.get(JsonKey.ACTIVITIES);
    Assert.assertEquals(
        ((Map) errorList.get(0)).get(JsonKey.ERROR_CODE),
        IResponseMessage.Key.GS_UDT_07);
  }

  @Test
  public void testSuspendGroupByAdminActiveGroup() {
    TestKit probe = new TestKit(system);
    ActorRef subject = system.actorOf(props);
    PowerMockito.mockStatic(ServiceFactory.class);
    CassandraOperation cassandraOperation = mock(CassandraOperationImpl.class);
    when(ServiceFactory.getInstance()).thenReturn(cassandraOperation);
    try {
      when(cassandraOperation.updateRecord(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyObject()))
          .thenReturn(getCassandraResponse());
      when(cassandraOperation.batchInsert(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyList()))
          .thenReturn(getCassandraResponse());
      when(cassandraOperation.updateAddSetRecord(
              Mockito.anyString(),
              Mockito.anyString(),
              Mockito.anyMap(),
              Mockito.anyString(),
              Mockito.anyObject()))
          .thenReturn(getCassandraResponse())
          .thenReturn(getCassandraResponse());
      when(cassandraOperation.updateRemoveSetRecord(
              Mockito.anyString(),
              Mockito.anyString(),
              Mockito.anyMap(),
              Mockito.anyString(),
              Mockito.anyObject()))
          .thenReturn(getCassandraResponse());
      when(cassandraOperation.batchUpdate(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyList()))
          .thenReturn(getCassandraResponse());
      when(cassandraOperation.executeSelectQuery(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyMap(), Mockito.anyObject()))
          .thenReturn(memberSizeResponse());
      when(cassandraOperation.getRecordById(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
          .thenReturn(getGroupsDetailsResponse());
      when(cassandraOperation.getRecordsByPrimaryKeys(
              Mockito.anyString(),
              Matchers.eq("group_member"),
              Mockito.anyList(),
              Mockito.anyString()))
          .thenReturn(getMemberResponse());
      when(cassandraOperation.deleteRecord(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
          .thenReturn(getCassandraResponse());
    } catch (BaseException be) {
      Assert.assertTrue(false);
    }

    Request reqObj = updateSuspendGroupReq();
    subject.tell(reqObj, probe.getRef());
    Response res = probe.expectMsgClass(Duration.ofSeconds(10), Response.class);
    Assert.assertTrue(null != res && res.getResponseCode() == 200);
  }

  @Test
  public void testSuspendGroupByNonAdminActiveGroup() {
    TestKit probe = new TestKit(system);
    ActorRef subject = system.actorOf(props);
    PowerMockito.mockStatic(ServiceFactory.class);
    CassandraOperation cassandraOperation = mock(CassandraOperationImpl.class);
    when(ServiceFactory.getInstance()).thenReturn(cassandraOperation);
    try {
      when(cassandraOperation.updateRecord(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyObject()))
          .thenReturn(getCassandraResponse());
      when(cassandraOperation.batchInsert(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyList()))
          .thenReturn(getCassandraResponse());
      when(cassandraOperation.updateAddSetRecord(
              Mockito.anyString(),
              Mockito.anyString(),
              Mockito.anyMap(),
              Mockito.anyString(),
              Mockito.anyObject()))
          .thenReturn(getCassandraResponse())
          .thenReturn(getCassandraResponse());
      when(cassandraOperation.updateRemoveSetRecord(
              Mockito.anyString(),
              Mockito.anyString(),
              Mockito.anyMap(),
              Mockito.anyString(),
              Mockito.anyObject()))
          .thenReturn(getCassandraResponse());
      when(cassandraOperation.batchUpdate(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyList()))
          .thenReturn(getCassandraResponse());
      when(cassandraOperation.executeSelectQuery(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyMap(), Mockito.anyObject()))
          .thenReturn(memberSizeResponse());
      when(cassandraOperation.getRecordById(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
          .thenReturn(getGroupsDetailsResponse());
      when(cassandraOperation.getRecordsByPrimaryKeys(
              Mockito.anyString(),
              Matchers.eq("group_member"),
              Mockito.anyList(),
              Mockito.anyString()))
          .thenReturn(getMemberResponse());
      when(cassandraOperation.deleteRecord(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
          .thenReturn(getCassandraResponse());
    } catch (BaseException be) {
      Assert.assertTrue(false);
    }

    Request reqObj = updateSuspendNonAdminUserGroupReq();
    subject.tell(reqObj, probe.getRef());
  }

  private Response memberSizeResponse() {
    Response response = new Response();
    Map<String, Object> result = new HashMap<>();

    Map<String, Object> count = new HashMap<>();
    List<Map<String, Object>> countlist = new ArrayList<>();
    countlist.add(count);
    count.put(JsonKey.COUNT, 3l);
    result.put(JsonKey.RESPONSE, countlist);
    response.putAll(result);
    return response;
  }

  private static Request getUpdateGroupReqForMaxMember() {
    Request reqObj = new Request();
    reqObj.setHeaders(headerMap);
    Map<String, Object> context = new HashMap<>();
    context.put(JsonKey.USER_ID, "user1");
    reqObj.setContext(context);
    Map<String, List<Map<String, Object>>> memberOperations = new HashMap<>();
    List<Map<String, Object>> members = new ArrayList<>();
    Map<String, Object> member = new HashMap<>();
    member.put(JsonKey.USER_ID, "userID1");
    members.add(member);
    member = new HashMap<>();
    member.put(JsonKey.USER_ID, "userID2");
    members.add(member);
    member = new HashMap<>();
    member.put(JsonKey.USER_ID, "userID3");
    members.add(member);
    member = new HashMap<>();
    member.put(JsonKey.USER_ID, "userID4");
    members.add(member);
    member = new HashMap<>();
    member.put(JsonKey.USER_ID, "userID5");
    members.add(member);
    memberOperations.put(JsonKey.ADD, members);
    reqObj.getRequest().put(JsonKey.MEMBERS, memberOperations);
    reqObj.setOperation(ActorOperations.UPDATE_GROUP.getValue());
    return reqObj;
  }

  private static Request getUpdateGroupReqForMaxActivity() {
    Request reqObj = new Request();
    reqObj.setHeaders(headerMap);
    Map<String, Object> context = new HashMap<>();
    context.put(JsonKey.USER_ID, "user1");
    reqObj.setContext(context);
    Map<String, List<Map<String, Object>>> activityOperations = new HashMap<>();
    List<Map<String, Object>> activities = new ArrayList<>();
    Map<String, Object> activity = new HashMap<>();
    activity.put(JsonKey.ID, "activity1");
    activity.put(JsonKey.TYPE, "COURSE");
    activities.add(activity);
    activity = new HashMap<>();
    activity.put(JsonKey.ID, "activity2");
    activity.put(JsonKey.TYPE, "COURSE");
    activities.add(activity);
    activity = new HashMap<>();
    activity.put(JsonKey.ID, "activity3");
    activity.put(JsonKey.TYPE, "COURSE");
    activities.add(activity);
    activity = new HashMap<>();
    activity.put(JsonKey.ID, "activity4");
    activity.put(JsonKey.TYPE, "COURSE");
    activities.add(activity);
    activity = new HashMap<>();
    activity.put(JsonKey.ID, "activity5");
    activity.put(JsonKey.TYPE, "COURSE");
    activities.add(activity);
    activityOperations.put(JsonKey.ADD, activities);
    reqObj.getRequest().put(JsonKey.ACTIVITIES, activityOperations);
    reqObj.setOperation(ActorOperations.UPDATE_GROUP.getValue());
    return reqObj;
  }

  private static Request updateGroupReq() {
    Request reqObj = new Request();
    reqObj.setHeaders(headerMap);
    Map<String, Object> context = new HashMap<>();
    context.put(JsonKey.USER_ID, "user1");
    reqObj.setContext(context);
    reqObj.setOperation(ActorOperations.UPDATE_GROUP.getValue());
    reqObj.getRequest().put(JsonKey.GROUP_NAME, "TestGroup Name1");

    Map<String, List<Map<String, Object>>> memberOpearations = new HashMap<>();
    List<Map<String, Object>> members = new ArrayList<>();
    Map<String, Object> member = new HashMap<>();
    member.put(JsonKey.USER_ID, "userID1");
    member.put(JsonKey.STATUS, "active");
    member.put(JsonKey.ROLE, "member");
    members.add(member);
    member = new HashMap<>();
    member.put(JsonKey.USER_ID, "userID2");
    member.put(JsonKey.STATUS, "active");
    member.put(JsonKey.ROLE, "member");
    members.add(member);
    memberOpearations.put("add", members);
    // Edit
    members = new ArrayList<>();
    member = new HashMap<>();
    member.put(JsonKey.USER_ID, "userID1");
    member.put(JsonKey.ROLE, "admin");
    members.add(member);
    memberOpearations.put("edit", members);
    // Remove
    ArrayList removeMembers = new ArrayList();
    removeMembers.add("userID1");
    memberOpearations.put("remove", removeMembers);
    // Add activities
    Map<String, List<Map<String, Object>>> activitiesOperations = new HashMap<>();
    List<Map<String, Object>> activities = new ArrayList<>();
    Map<String, Object> activity = new HashMap<>();
    activity.put(JsonKey.TYPE, "COURSE");
    activity.put(JsonKey.ID, "activityId1");
    activities.add(activity);
    activity = new HashMap<>();
    activity.put(JsonKey.TYPE, "COURSE");
    activity.put(JsonKey.ID, "activityId2");
    activities.add(activity);
    activitiesOperations.put("add", activities);
    // Remove
    ArrayList removeActivities = new ArrayList();
    removeActivities.add("activityId2");
    activitiesOperations.put("remove", removeActivities);
    reqObj.getRequest().put(JsonKey.ACTIVITIES, activitiesOperations);
    reqObj.getRequest().put(JsonKey.MEMBERS, memberOpearations);
    reqObj.getRequest().put(JsonKey.GROUP_ID, "group1");
    return reqObj;
  }

  private static Request updateSuspendGroupReq() {
    Request reqObj = new Request();
    reqObj.setHeaders(headerMap);
    Map<String, Object> context = new HashMap<>();
    context.put(JsonKey.USER_ID, "user1");
    reqObj.setContext(context);
    reqObj.setOperation(ActorOperations.UPDATE_GROUP.getValue());
    reqObj.getRequest().put(JsonKey.GROUP_NAME, "TestGroup");
    reqObj.getRequest().put(JsonKey.STATUS, "suspended");
    reqObj.getRequest().put(JsonKey.GROUP_ID, "group1");
    return reqObj;
  }

  private static Request updateSuspendNonAdminUserGroupReq() {
    Request reqObj = new Request();
    reqObj.setHeaders(headerMap);
    Map<String, Object> context = new HashMap<>();
    context.put(JsonKey.USER_ID, "userID22");
    reqObj.setContext(context);
    reqObj.setOperation(ActorOperations.UPDATE_GROUP.getValue());
    reqObj.getRequest().put(JsonKey.GROUP_NAME, "TestGroup");
    reqObj.getRequest().put(JsonKey.STATUS, "suspended");
    reqObj.getRequest().put(JsonKey.GROUP_ID, "group1");
    return reqObj;
  }

  private Response getGroupsDetailsResponse() {
    Map<String, Object> result = new HashMap<>();
    List<Map<String, Object>> groupList = new ArrayList<>();
    Map<String, Object> group1 = new HashMap<>();
    group1.put("name", "TestGroup1");
    group1.put("id", "TestGroup");
    group1.put("status", "active");
    group1.put("createdBy", "user1");
    List<Map<String, Object>> activities = new ArrayList<>();
    Map<String, Object> activity1 = new HashMap<>();
    activity1.put(JsonKey.ID, "do_112470675618004992181");
    activity1.put(JsonKey.TYPE, "Course");

    Map<String, Object> activity2 = new HashMap<>();
    activity2.put(JsonKey.ID, "do_11304065892935270414");
    activity2.put(JsonKey.TYPE, "Textbook");
    activities.add(activity1);
    activities.add(activity2);
    group1.put(JsonKey.ACTIVITIES, activities);
    groupList.add(group1);
    result.put(JsonKey.RESPONSE, groupList);
    Response response = new Response();
    response.putAll(result);
    return response;
  }

  static Response getMemberResponse() {
    List<Map<String, Object>> members = new ArrayList<>();
    Map<String, Object> member = new HashMap<>();
    member.put(JsonKey.USER_ID, "user1");
    member.put(JsonKey.ROLE, JsonKey.ADMIN);
    member.put(JsonKey.STATUS, JsonKey.ACTIVE);
    member.put(JsonKey.CREATED_BY, "user1");
    members.add(member);
    member = new HashMap<>();
    member.put(JsonKey.USER_ID, "userID22");
    member.put(JsonKey.ROLE, JsonKey.MEMBER);
    member.put(JsonKey.STATUS, JsonKey.ACTIVE);
    member.put(JsonKey.CREATED_BY, "user1");

    members.add(member);
    Map<String, Object> result = new HashMap<>();
    result.put(JsonKey.RESPONSE, members);
    Response response = new Response();
    response.putAll(result);
    return response;
  }

  static Response getUserGroupResponse() {
    List<Map<String, Object>> userGroups = new ArrayList<>();
    Map<String, Object> userGroup = new HashMap<>();
    userGroup.put(JsonKey.USER_ID, "user1");
    userGroup.put(JsonKey.GROUP_ID, Arrays.asList("group1", "group2"));
    userGroups.add(userGroup);
    userGroup.put(JsonKey.USER_ID, "user2");
    userGroup.put(JsonKey.GROUP_ID, Arrays.asList("group1", "group2"));
    userGroups.add(userGroup);
    Map<String, Object> result = new HashMap<>();
    result.put(JsonKey.RESPONSE, userGroups);
    Response response = new Response();
    response.putAll(result);
    return response;
  }
}
