package org.sunbird.dao;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.cassandra.CassandraOperation;
import org.sunbird.common.CassandraUtil;
import org.sunbird.common.Constants;
import org.sunbird.common.exception.BaseException;
import org.sunbird.helper.ServiceFactory;
import org.sunbird.models.Member;
import org.sunbird.common.response.Response;
import org.sunbird.util.DBUtil;
import org.sunbird.common.util.JsonKey;

public class MemberDaoImpl implements MemberDao {

  private static final String GROUP_MEMBER_TABLE = "group_member";
  private static final String USER_GROUP_TABLE = "user_group";
  private CassandraOperation cassandraOperation = ServiceFactory.getInstance();
  private ObjectMapper mapper = new ObjectMapper();
  private static MemberDao memberDao = null;
  private Logger logger = LoggerFactory.getLogger(MemberDaoImpl.class);

  public static MemberDao getInstance() {
    if (memberDao == null) {
      memberDao = new MemberDaoImpl();
    }
    return memberDao;
  }

  @Override
  public Response addMembers(List<Member> member, Map<String,Object> reqContext) throws BaseException {
    List<Map<String, Object>> memberList =
        mapper.convertValue(member, new TypeReference<List<Map<String, Object>>>() {});
    Response response =
        cassandraOperation.batchInsert(DBUtil.KEY_SPACE_NAME, GROUP_MEMBER_TABLE, memberList, reqContext);
    return response;
  }

  @Override
  public Response readGroupIdsByUserId(String userId, Map<String,Object> reqContext) throws BaseException {
    List<String> userIds = new ArrayList<>();
    userIds.add(userId);
    return readGroupIdsByUserIds(userIds,reqContext);
  }

  public Response readGroupIdsByUserIds(List<String> memberList, Map<String,Object> reqContext) throws BaseException {
    Response responseObj =
        cassandraOperation.getRecordsByPrimaryKeys(
            DBUtil.KEY_SPACE_NAME, USER_GROUP_TABLE, memberList, JsonKey.USER_ID, reqContext);
    return responseObj;
  }

  public Response upsertGroupInUserGroup(Map<String, Object> userGroupMap, Map<String,Object> reqContext) throws BaseException {
    Response responseObj =
        cassandraOperation.upsertRecord(DBUtil.KEY_SPACE_NAME, USER_GROUP_TABLE, userGroupMap, reqContext);
    return responseObj;
  }

  public Response updateGroupInUserGroup(Map<String, Object> userGroupMap, String userId, Map<String,Object> reqContext)
      throws BaseException {
    Map<String, Object> compositeKeyMap = new HashMap<>();
    compositeKeyMap.put(JsonKey.USER_ID, userId);

    Response responseObj =
        cassandraOperation.updateRecord(
            DBUtil.KEY_SPACE_NAME, USER_GROUP_TABLE, userGroupMap, compositeKeyMap,reqContext);
    return responseObj;
  }

  public void deleteFromUserGroup(String userId, Map<String,Object> reqContext) throws BaseException {
    Map<String, String> compositeKeyMap = new HashMap<>();
    compositeKeyMap.put(JsonKey.USER_ID, userId);
    cassandraOperation.deleteRecord(DBUtil.KEY_SPACE_NAME, USER_GROUP_TABLE, compositeKeyMap,reqContext);
  }

  @Override
  public void deleteMemberFromGroup(String groupId, List<String> members, Map<String,Object> reqContext) throws BaseException {
    List<Map<String, Object>> compositeKeyMap = new ArrayList<>();
    members.forEach(
        memberId -> {
          Map<String, Object> primaryKeys = new HashMap<>();
          primaryKeys.put(JsonKey.GROUP_ID, groupId);
          primaryKeys.put(JsonKey.USER_ID, memberId);
          compositeKeyMap.add(primaryKeys);
        });
    cassandraOperation.batchDelete(DBUtil.KEY_SPACE_NAME, GROUP_MEMBER_TABLE, compositeKeyMap,reqContext);
  }

  @Override
  public Response editMembers(List<Member> member, Map<String,Object> reqContext) throws BaseException {
    List<Map<String, Map<String, Object>>> list = new ArrayList<>();
    for (Member memberObj : member) {
      list.add(CassandraUtil.batchUpdateQuery(memberObj));
    }
    for (Map<String, Map<String, Object>> record : list) {
      Map<String, Object> nonPKRecord = record.get(Constants.NON_PRIMARY_KEY);
      Map<String, Object> filteredNonPKRecord =
          nonPKRecord
              .entrySet()
              .stream()
              .filter(map -> (map.getValue() != null))
              .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
      record.put(Constants.NON_PRIMARY_KEY, filteredNonPKRecord);
    }
    Response response =
        cassandraOperation.batchUpdate(DBUtil.KEY_SPACE_NAME, GROUP_MEMBER_TABLE, list,reqContext);
    return response;
  }

  @Override
  public Response fetchMembersByGroupIds(List<String> groupIds, Map<String,Object> reqContext) throws BaseException {
    Map<String, Object> properties = new HashMap<>();
    properties.put(JsonKey.GROUP_ID, groupIds);
    Response responseObj =
        cassandraOperation.getRecordsByPrimaryKeys(
            DBUtil.KEY_SPACE_NAME, GROUP_MEMBER_TABLE, groupIds, JsonKey.GROUP_ID,reqContext);
    return responseObj;
  }

  @Override
  public Response fetchGroupByUser(List<String> groupIds, String userId, Map<String,Object> reqContext) throws BaseException {
    Map<String, Object> properties = new LinkedHashMap<>();
    properties.put(JsonKey.GROUP_ID, groupIds);
    properties.put(JsonKey.USER_ID, userId);
    Response responseObj =
        cassandraOperation.getRecordsByProperties(
            DBUtil.KEY_SPACE_NAME, GROUP_MEMBER_TABLE, properties,reqContext);
    return responseObj;
  }
}
