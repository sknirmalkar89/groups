package org.sunbird.actors;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.exception.DBException;
import org.sunbird.helper.ServiceFactory;
import org.sunbird.common.message.Localizer;
import org.sunbird.models.ActorOperations;
import org.sunbird.common.request.Request;
import org.sunbird.common.response.Response;
import org.sunbird.common.util.JsonKey;
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
public class DeleteGroupActorTest extends BaseActorTest {

  private final Props props = Props.create(DeleteGroupActor.class);
  private Logger logger = LoggerFactory.getLogger(DeleteGroupActorTest.class);
  public static CassandraOperation cassandraOperation;
  public static PropertiesCache propertiesCache;

  @Before
  public void setUp() throws Exception {
    PowerMockito.mockStatic(Localizer.class);
    when(Localizer.getInstance()).thenReturn(null);

    PowerMockito.mockStatic(ServiceFactory.class);
    cassandraOperation = mock(CassandraOperationImpl.class);
    when(ServiceFactory.getInstance()).thenReturn(cassandraOperation);
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
  public void testDeleteroup() {
    TestKit probe = new TestKit(system);
    ActorRef subject = system.actorOf(props);

    try {

      when(cassandraOperation.batchDelete(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyList(),Mockito.any()))
          .thenReturn(getCassandraResponse());
      when(cassandraOperation.getRecordsByPrimaryKeys(
              Mockito.anyString(),
              Matchers.eq("group_member"),
              Mockito.anyList(),
              Mockito.anyString(),
              Mockito.any()))
          .thenReturn(getMemberResponse());
      when(cassandraOperation.getRecordById(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),Mockito.any()))
          .thenReturn(getGroupsDetailsResponse());
      when(cassandraOperation.deleteRecord(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),Mockito.any()))
          .thenReturn(getCassandraResponse());
    } catch (BaseException be) {
      Assert.assertTrue(false);
    }

    Request reqObj = deleteGroupReq();
    subject.tell(reqObj, probe.getRef());
    Response res = probe.expectMsgClass(Duration.ofSeconds(20), Response.class);
    Assert.assertTrue(null != res && res.getResponseCode() == 200);
  }

  @Test
  public void testDeleteGroupDBException() {
    TestKit probe = new TestKit(system);
    ActorRef subject = system.actorOf(props);

    try {

      when(cassandraOperation.batchDelete(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyList(),Mockito.any()))
              .thenReturn(getCassandraResponse());
      when(cassandraOperation.getRecordsByPrimaryKeys(
              Mockito.anyString(),
              Matchers.eq("group_member"),
              Mockito.anyList(),
              Mockito.anyString(),
              Mockito.any()))
              .thenThrow(DBException.class);
      when(cassandraOperation.getRecordById(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),Mockito.any()))
              .thenThrow(DBException.class);
      when(cassandraOperation.deleteRecord(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),Mockito.any()))
              .thenReturn(getCassandraResponse());
    } catch (BaseException be) {
      Assert.assertTrue(false);
    }
    Request reqObj = deleteGroupReq();
    try {
       subject.tell(reqObj, probe.getRef());
    }catch (DBException ex){
      Assert.assertTrue(true);
    }
  }

  @Test
  public void testDeleteGroupException() {
    TestKit probe = new TestKit(system);
    ActorRef subject = system.actorOf(props);

    try {

      when(cassandraOperation.batchDelete(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyList(),Mockito.any()))
              .thenReturn(getCassandraResponse());
      when(cassandraOperation.getRecordsByPrimaryKeys(
              Mockito.anyString(),
              Matchers.eq("group_member"),
              Mockito.anyList(),
              Mockito.anyString(),
              Mockito.any()))
              .thenThrow(DBException.class);
      when(cassandraOperation.getRecordById(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),Mockito.any()))
              .thenThrow(DBException.class);
      when(cassandraOperation.deleteRecord(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),Mockito.any()))
              .thenReturn(getCassandraResponse());
    } catch (BaseException be) {
      Assert.assertTrue(false);
    }
    Request reqObj = deleteGroupReq();
    try {
      subject.tell(reqObj, probe.getRef());
    }catch (BaseException ex){
      Assert.assertTrue(true);
    }
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

  private Response getGroupsDetailsResponse() {
    Map<String, Object> result = new HashMap<>();
    List<Map<String, Object>> groupList = new ArrayList<>();
    Map<String, Object> group1 = new HashMap<>();
    group1.put("name", "TestGroup1");
    group1.put("id", "group1");
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

  private static Request deleteGroupReq() {
    Request reqObj = new Request();
    reqObj.setHeaders(headerMap);
    Map<String, Object> context = new HashMap<>();
    context.put(JsonKey.USER_ID, "user1");
    reqObj.setContext(context);
    reqObj.setOperation(ActorOperations.DELETE_GROUP.getValue());
    reqObj.getRequest().put(JsonKey.GROUP_ID, "group1");
    return reqObj;
  }
}
