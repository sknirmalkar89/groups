package org.sunbird.dao;

import java.util.List;
import java.util.Map;
import org.sunbird.common.exception.BaseException;
import org.sunbird.models.Member;
import org.sunbird.common.response.Response;

public interface MemberDao {

  Response addMembers(List<Member> member, Map<String,Object> reqContext) throws BaseException;

  Response editMembers(List<Member> member, Map<String,Object> reqContext) throws BaseException;

  Response fetchMembersByGroupIds(List<String> groupIds, Map<String,Object> reqContext) throws BaseException;

  Response fetchGroupByUser(List<String> groupIds, String userId, Map<String,Object> reqContext) throws BaseException;

  /**
   * This method will return group uuids based on userId and return response Object as success
   * response or throw ProjectCommonException.
   *
   * @param userId
   * @return responseObj with Group Details.
   */
  Response readGroupIdsByUserId(String userId, Map<String,Object> reqContext) throws BaseException;

  Response readGroupIdsByUserIds(List<String> memberList, Map<String,Object> reqContext) throws BaseException;

  Response upsertGroupInUserGroup(Map<String, Object> userGroupMap, Map<String,Object> reqContext) throws BaseException;

  Response updateGroupInUserGroup(Map<String, Object> userGroupMap, String userId, Map<String,Object> reqContext)
      throws BaseException;

  void deleteFromUserGroup(String userId, Map<String,Object> reqContext) throws BaseException;

  /**
   * Delete members from a group
   *
   * @param groupId
   * @param members
   * @throws BaseException
   */
  void deleteMemberFromGroup(String groupId, List<String> members, Map<String,Object> reqContext) throws BaseException;
}
