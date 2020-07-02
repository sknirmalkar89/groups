package org.sunbird.actors;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.cassandraunit.CQLDataLoader;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.cassandraunit.dataset.cql.FileCQLDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.sunbird.util.JsonKey;

public class EmbeddedCassandra {

  static final String KEYSPACE = "sunbird";
  static final String GROUP_TABLE = "group";
  static Session session;
  static BoundStatement insertStatement;
  static BoundStatement selectStatement;
  private static CQLDataLoader dataLoader;

  public static void setUp() throws Exception {
    EmbeddedCassandraServerHelper.startEmbeddedCassandra(30000L);
    session = EmbeddedCassandraServerHelper.getSession();
    dataLoader = new CQLDataLoader(session);
    dataLoader.load(new ClassPathCQLDataSet("createGroup.cql", KEYSPACE));
  }

  public void loadData(String cqlFile) {
    dataLoader.load(new FileCQLDataSet(cqlFile, false, false));
  }

  public static void createStatements() {

    // create prepared statements
    PreparedStatement insertGroupQuery =
        session.prepare(
            QueryBuilder.insertInto(KEYSPACE, GROUP_TABLE)
                .value(JsonKey.ID, QueryBuilder.bindMarker())
                .value(JsonKey.GROUP_NAME, QueryBuilder.bindMarker())
                .value(JsonKey.GROUP_DESC, QueryBuilder.bindMarker()));

    // link prepared statements to boundstatements
    PreparedStatement selectGroupQuery =
        session.prepare(QueryBuilder.select().all().from(KEYSPACE, GROUP_TABLE));

    // link prepared statements to boundstatements
    insertStatement = new BoundStatement(insertGroupQuery);
    selectStatement = new BoundStatement(selectGroupQuery);
  }

  public static void close() throws Exception {
    EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
  }
}
