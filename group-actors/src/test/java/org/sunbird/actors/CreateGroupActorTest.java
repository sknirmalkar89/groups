package org.sunbird.actors;

import static org.powermock.api.mockito.PowerMockito.when;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.datastax.driver.core.ResultSet;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.cassandra.CassandraOperation;
import org.sunbird.cassandraimpl.CassandraOperationImpl;
import org.sunbird.common.CassandraUtil;
import org.sunbird.helper.ServiceFactory;
import org.sunbird.message.Localizer;
import org.sunbird.models.ActorOperations;
import org.sunbird.request.Request;
import org.sunbird.response.Response;
import org.sunbird.util.JsonKey;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
  CassandraOperation.class,
  CassandraOperationImpl.class,
  ServiceFactory.class,
  Localizer.class
})
@PowerMockIgnore({"javax.management.*"})
public class CreateGroupActorTest extends BaseActorTest {

  private final Props props = Props.create(CreateGroupActor.class);

  private static Request reqObj;

  @BeforeClass
  public static void setUp() throws Exception {
    reqObj = createGroupReq();
    PowerMockito.mockStatic(Localizer.class);
    when(Localizer.getInstance()).thenReturn(null);

    // mock CassandraOperation
    CassandraOperationImpl cassandraOperation = MockCassandra.mockCassandraOperation();

    // when inserting record to cassandra insert record in to EmbeddedCassandra
    when(cassandraOperation.insertRecord(
            Mockito.anyString(), Mockito.anyString(), Mockito.anyMap()))
        .thenReturn(MockCassandra.getCreateGroupResponse(reqObj));

    // check record is inserted by querying EmbeddedCassandra
    ResultSet resultSet = EmbeddedCassandra.session.execute(EmbeddedCassandra.selectStatement);
    Response response = CassandraUtil.createResponse(resultSet);
    List<Map<String, Object>> groupList =
        (List<Map<String, Object>>) response.getResult().get(JsonKey.RESPONSE);
    Assert.assertEquals(reqObj.get(JsonKey.GROUP_NAME), groupList.get(0).get(JsonKey.GROUP_NAME));
  }

  @Test
  public void testCreateGroup() {
    TestKit probe = new TestKit(system);
    ActorRef subject = system.actorOf(props);
    subject.tell(reqObj, probe.getRef());
    Response res = probe.expectMsgClass(Duration.ofSeconds(10), Response.class);
    System.out.println(res.getResult());
    Assert.assertTrue(null != res && res.getResponseCode() == 200);
    Assert.assertNotNull(res.getResult().get(JsonKey.GROUP_ID));
  }

  private static Request createGroupReq() {
    Request reqObj = new Request();
    reqObj.setHeaders(headerMap);
    reqObj.setOperation(ActorOperations.CREATE_GROUP.getValue());
    reqObj.getRequest().put(JsonKey.GROUP_NAME, "TestGroup Name");
    reqObj.getRequest().put(JsonKey.GROUP_DESC, "TestGroup Description");
    reqObj.getRequest().put(JsonKey.ID, UUID.randomUUID().toString());
    return reqObj;
  }

  @AfterClass
  public static void tearDown() throws Exception {
    EmbeddedCassandra.close();
  }
}
