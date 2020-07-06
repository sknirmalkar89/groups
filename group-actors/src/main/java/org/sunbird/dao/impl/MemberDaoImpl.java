package org.sunbird.dao.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.sunbird.cassandra.CassandraOperation;
import org.sunbird.dao.MemberDao;
import org.sunbird.exception.BaseException;
import org.sunbird.helper.ServiceFactory;
import org.sunbird.models.Member;
import org.sunbird.response.Response;
import org.sunbird.util.DBUtil;

public class MemberDaoImpl implements MemberDao {

  private static final String TABLE_NAME = "group_member";
  private CassandraOperation cassandraOperation = ServiceFactory.getInstance();
  private ObjectMapper mapper = new ObjectMapper();
  private static MemberDao memberDao = null;

  public static MemberDao getInstance() {
    if (memberDao == null) {
      memberDao = new MemberDaoImpl();
    }
    return memberDao;
  }

  @Override
  public Response addMembers(List<Member> member) throws BaseException {
    List<Map<String, Object>> memberList =
        mapper.convertValue(member, new TypeReference<List<Map<String, Object>>>() {});
    Response response =
        cassandraOperation.batchInsert(DBUtil.KEY_SPACE_NAME, TABLE_NAME, memberList);
    return response;
  }

  @Override
  public Response removeMembers(List<String> members, String groupId) throws BaseException {
    return null;
  }

  @Override
  public Response fetchMembers() throws BaseException {
    return null;
  }
}
