package org.sunbird.service;

import java.util.List;
import java.util.Map;
import org.sunbird.common.exception.BaseException;
import org.sunbird.models.Member;
import org.sunbird.models.MemberResponse;
import org.sunbird.common.response.Response;

public interface MemberService {

  Response addMembers(List<Member> member, List<Map<String, Object>> userGroupsList, Map<String, Object> reqContext)
      throws BaseException;

  Response editMembers(List<Member> member, Map<String, Object> reqContext) throws BaseException;

  Response removeMembers(List<Member> member, Map<String, Object> reqContext) throws BaseException;

  void handleMemberOperations(Map memberOperationMap, String groupId, String contextUserId, Map<String, Object> reqContext)
      throws BaseException;

  Response handleMemberAddition(
      List<Map<String, Object>> memberList,
      String groupId,
      String contextUserId,
      List<Map<String, Object>> userGroupsList,
      Map<String, Object> reqContext)
      throws BaseException;

  List<MemberResponse> readGroupMembers(String groupId, Map<String, Object> reqContext)
      throws BaseException;

  List<MemberResponse> fetchMembersByGroupId(String groupId, Map<String, Object> reqContext) throws BaseException;

  List<MemberResponse> fetchMembersByGroupIds(List<String> groupIds, Map<String, Object> reqContext) throws BaseException;

  List<Map<String, Object>> fetchGroupByUser(List<String> groupIds, String userId, Map<String, Object> reqContext)
      throws BaseException;

  public List<Map<String, Object>> getGroupIdsforUserIds(List<String> memberList, Map<String, Object> reqContext);

  public void removeGroupInUserGroup(
      List<Member> memberList, List<Map<String, Object>> dbResGroupIds, Map<String, Object> reqContext) throws BaseException;

  public void deleteGroupMembers(String groupId, List<String> members, Map<String, Object> reqContext) throws BaseException;
}
