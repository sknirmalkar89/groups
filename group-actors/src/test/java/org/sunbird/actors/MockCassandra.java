package org.sunbird.actors;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.junit.Assert;
import org.powermock.api.mockito.PowerMockito;
import org.sunbird.cassandraimpl.CassandraOperationImpl;
import org.sunbird.common.Constants;
import org.sunbird.helper.ServiceFactory;
import org.sunbird.request.Request;
import org.sunbird.response.Response;
import org.sunbird.util.JsonKey;

public class MockCassandra {

  public static CassandraOperationImpl mockCassandraOperation() throws Exception {
    EmbeddedCassandra.setUp();
    EmbeddedCassandra.createStatements();
    CassandraOperationImpl cassandraOperation;
    // mock cassandra
    PowerMockito.mockStatic(ServiceFactory.class);
    cassandraOperation = mock(CassandraOperationImpl.class);
    when(ServiceFactory.getInstance()).thenReturn(cassandraOperation);
    return cassandraOperation;
  }

  public static Response getCreateGroupResponse(Request request) {
    Assert.assertNotNull(EmbeddedCassandra.session.getCluster().getClusterName());
    ResultSet resultSet =
        EmbeddedCassandra.session.execute(
            QueryBuilder.select()
                .all()
                .from(EmbeddedCassandra.KEYSPACE, EmbeddedCassandra.GROUP_TABLE));
    // initially group table is empty
    Assert.assertEquals(0, resultSet.all().size());
    EmbeddedCassandra.session.execute(
        EmbeddedCassandra.insertStatement.bind(
            request.getRequest().get(JsonKey.ID),
            request.getRequest().get(JsonKey.GROUP_NAME),
            request.getRequest().get(JsonKey.GROUP_DESC)));
    Response response = new Response();
    response.put(Constants.RESPONSE, Constants.SUCCESS);
    return response;
  }
}
