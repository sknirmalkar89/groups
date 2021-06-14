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
public class UpdateGroupMembershipActorTest extends BaseActorTest {
  private Logger logger = LoggerFactory.getLogger(UpdateGroupMembershipActor.class);
  Props props = Props.create(UpdateGroupMembershipActor.class);

  @Test
  public void testUpdateGroupMembership() throws Exception {
    TestKit probe = new TestKit(system);
    PowerMockito.mockStatic(Localizer.class);
    when(Localizer.getInstance()).thenReturn(null);
    mockCacheActor();
    PowerMockito.mockStatic(SystemConfigUtil.class);
    ActorRef subject = system.actorOf(props);
    CassandraOperation cassandraOperation;
    PowerMockito.mockStatic(ServiceFactory.class);
    cassandraOperation = mock(CassandraOperationImpl.class);
    when(ServiceFactory.getInstance()).thenReturn(cassandraOperation);
    try {
      when(cassandraOperation.batchUpdate(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyList(),Mockito.any()))
          .thenReturn(getCassandraResponse());

    } catch (BaseException be) {
      Assert.assertTrue(false);
    }

    Request reqObj = updateGroupMembershipReq();
    subject.tell(reqObj, probe.getRef());
    Response res = probe.expectMsgClass(Duration.ofSeconds(20), Response.class);
    Assert.assertTrue(null != res && res.getResponseCode() == 200);
  }

  @Test
  public void testUpdateGroupMembershipDBException() throws Exception {
    TestKit probe = new TestKit(system);
    PowerMockito.mockStatic(Localizer.class);
    when(Localizer.getInstance()).thenReturn(null);
    mockCacheActor();
    PowerMockito.mockStatic(SystemConfigUtil.class);
    ActorRef subject = system.actorOf(props);
    CassandraOperation cassandraOperation;
    PowerMockito.mockStatic(ServiceFactory.class);
    cassandraOperation = mock(CassandraOperationImpl.class);
    when(ServiceFactory.getInstance()).thenReturn(cassandraOperation);
    try {
      when(cassandraOperation.batchUpdate(
              Mockito.anyString(), Mockito.anyString(), Mockito.anyList(),Mockito.any()))
              .thenThrow(DBException.class);

    } catch (BaseException be) {
      Assert.assertTrue(false);
    }

    Request reqObj = updateGroupMembershipReq();
    try {
      subject.tell(reqObj, probe.getRef());
    }catch (DBException ex){
      Assert.assertTrue(true);
    }
  }

  private static Request updateGroupMembershipReq() {
    Request reqObj = new Request();
    reqObj.setHeaders(headerMap);
    Map<String, Object> context = new HashMap<>();
    context.put(JsonKey.USER_ID, "user1");
    reqObj.setContext(context);
    List<Map<String, Object>> groups = new ArrayList<>();
    Map<String, Object> group = new HashMap<>();
    group.put(JsonKey.GROUP_ID, "group1");
    group.put(JsonKey.VISITED, true);
    groups.add(group);
    reqObj.setOperation(ActorOperations.UPDATE_GROUP_MEMBERSHIP.getValue());
    reqObj.getRequest().put(JsonKey.USER_ID, "user1");
    reqObj.getRequest().put(JsonKey.GROUPS, groups);
    return reqObj;
  }
}
