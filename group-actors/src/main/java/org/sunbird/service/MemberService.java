package org.sunbird.service;

import java.util.List;
import java.util.Map;
import org.sunbird.common.exception.BaseException;
import org.sunbird.models.Member;
import org.sunbird.models.MemberResponse;
import org.sunbird.common.response.Response;

public interface MemberService {

  Response addMembers(List<Member> member, List<Map<String, Object>> userGroupsList)
      throws BaseException;

  Response editMembers(List<Member> member) throws BaseException;

  Response removeMembers(List<Member> member) throws BaseException;

  void handleMemberOperations(Map memberOperationMap, String groupId, String contextUserId)
      throws BaseException;

  Response handleMemberAddition(
      List<Map<String, Object>> memberList,
      String groupId,
      String contextUserId,
      List<Map<String, Object>> userGroupsList)
      throws BaseException;

  List<MemberResponse> readGroupMembers(String groupId, Map<String, Object> reqContext)
      throws BaseException;

  List<MemberResponse> fetchMembersByGroupId(String groupId) throws BaseException;

  List<MemberResponse> fetchMembersByGroupIds(List<String> groupIds) throws BaseException;

  List<Map<String, Object>> fetchGroupByUser(List<String> groupIds, String userId)
      throws BaseException;

  public List<Map<String, Object>> getGroupIdsforUserIds(List<String> memberList);

  public void removeGroupInUserGroup(
      List<Member> memberList, List<Map<String, Object>> dbResGroupIds) throws BaseException;

  public void deleteGroupMembers(String groupId, List<String> members) throws BaseException;
}
