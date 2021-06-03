package org.sunbird.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.dao.MemberDao;
import org.sunbird.dao.MemberDaoImpl;
import org.sunbird.common.exception.BaseException;
import org.sunbird.models.Member;
import org.sunbird.models.MemberResponse;
import org.sunbird.common.response.Response;
import org.sunbird.util.GroupUtil;
import org.sunbird.common.util.JsonKey;
import org.sunbird.util.LoggerUtil;

public class MemberServiceImpl implements MemberService {

  private static MemberDao memberDao = MemberDaoImpl.getInstance();
  private static LoggerUtil logger = new LoggerUtil(MemberServiceImpl.class);
  private static ObjectMapper objectMapper = new ObjectMapper();
  private static UserService userService = UserServiceImpl.getInstance();

  @Override
  public Response addMembers(List<Member> member, List<Map<String, Object>> dbResGroupIds, Map<String, Object> reqContext)
      throws BaseException {
    Response response = memberDao.addMembers(member, reqContext);
    if (response != null
        && response.getResult().get(JsonKey.RESPONSE) != null
        && dbResGroupIds == null) {
      dbResGroupIds = getGroupIdsforUserIds(GroupUtil.getMemberIdList(member), reqContext);
    }
    // Update if userId is already in DB, otherwise insert
    addGroupInUserGroup(member, dbResGroupIds, reqContext);
    return response;
  }

  @Override
  public List<Map<String, Object>> getGroupIdsforUserIds(List<String> member, Map<String, Object> reqContext) {
    Response userGroupResponseObj = memberDao.readGroupIdsByUserIds(member, reqContext);

    List<Map<String, Object>> dbResGroupIds = null;
    if (null != userGroupResponseObj && null != userGroupResponseObj.getResult()) {
      dbResGroupIds =
          (List<Map<String, Object>>) userGroupResponseObj.getResult().get(JsonKey.RESPONSE);
    }
    return dbResGroupIds;
  }

  private void addGroupInUserGroup(List<Member> memberList, List<Map<String, Object>> dbResGroupIds, Map<String, Object> reqContext)
      throws BaseException {
    logger.info(
        reqContext, MessageFormat.format("User Group table update started for the group id {0}", memberList.get(0).getGroupId()));
    memberList
        .stream()
        .forEach(
            data -> {
              Map<String, Object> userGroupMap = createUserGroupRecord(new HashSet<>(), data);
              if (CollectionUtils.isNotEmpty(dbResGroupIds)) {
                Map<String, Object> userDbMap =
                    dbResGroupIds
                        .stream()
                        .filter(
                            dbMap -> (data.getUserId()).equals((String) dbMap.get(JsonKey.USER_ID)))
                        .findFirst()
                        .orElse(null);
                if (MapUtils.isNotEmpty(userDbMap)) {
                  userGroupMap =
                      createUserGroupRecord((Set<String>) userDbMap.get(JsonKey.GROUP_ID), data);
                }
              }
              if (MapUtils.isNotEmpty(userGroupMap)) {
                memberDao.upsertGroupInUserGroup(userGroupMap, reqContext);
              }
            });
  }

  private Map<String, Object> createUserGroupRecord(Set<String> groupSet, Member data) {
    Map<String, Object> userGroupMap = new HashMap<>();
    groupSet.add(data.getGroupId());
    userGroupMap.put(JsonKey.USER_ID, data.getUserId());
    userGroupMap.put(JsonKey.GROUP_ID, groupSet);
    return userGroupMap;
  }

  @Override
  public Response editMembers(List<Member> member, Map<String, Object> reqContext) throws BaseException {
    Response response = memberDao.editMembers(member, reqContext);
    return response;
  }

  @Override
  public Response removeMembers(List<Member> member, Map<String, Object> reqContext) throws BaseException {
    Response response = memberDao.editMembers(member, reqContext);

    if (response != null && response.getResult().get(JsonKey.RESPONSE) != null) {
      List<Map<String, Object>> dbResGroupIds =
          getGroupIdsforUserIds(GroupUtil.getMemberIdList(member), reqContext);
      removeGroupInUserGroup(member, dbResGroupIds, reqContext);
    }
    return response;
  }

  @Override
  public void removeGroupInUserGroup(
      List<Member> memberList, List<Map<String, Object>> dbResGroupIds, Map<String, Object> reqContext) throws BaseException {
    logger.info(
        reqContext,MessageFormat.format("User Group table update started for the group id {0}", memberList.get(0).getGroupId()));

    memberList
        .stream()
        .forEach(
            data -> {
              Map<String, Object> userGroupMap = new HashMap<>();
              if (CollectionUtils.isNotEmpty(dbResGroupIds)) {
                dbResGroupIds
                    .stream()
                    .forEach(
                        map -> {
                          if ((data.getUserId()).equals((String) map.get(JsonKey.USER_ID))) {
                            Set<String> groupIdsSet = (Set<String>) map.get(JsonKey.GROUP_ID);
                            groupIdsSet.remove(data.getGroupId());
                            if (groupIdsSet.size() == 0) {
                              memberDao.deleteFromUserGroup(data.getUserId(), reqContext);
                            } else {
                              userGroupMap.put(JsonKey.GROUP_ID, groupIdsSet);
                            }
                          }
                        });
              }
              if (MapUtils.isNotEmpty(userGroupMap) && userGroupMap.size() > 0) {
                memberDao.updateGroupInUserGroup(userGroupMap, data.getUserId(), reqContext);
              }
            });
  }

  @Override
  public void deleteGroupMembers(String groupId, List<String> members, Map<String, Object> reqContext) throws BaseException {
    memberDao.deleteMemberFromGroup(groupId, members, reqContext);
  }

  public void handleMemberOperations(Map memberOperationMap, String groupId, String contextUserId, Map<String, Object> reqContext)
      throws BaseException {
    List<Map<String, Object>> memberAddList =
        (List<Map<String, Object>>) memberOperationMap.get(JsonKey.ADD);
    if (CollectionUtils.isNotEmpty(memberAddList)) {
      Response addMemberRes = handleMemberAddition(memberAddList, groupId, contextUserId, null, reqContext);
    }
    List<Map<String, Object>> memberEditList =
        (List<Map<String, Object>>) memberOperationMap.get(JsonKey.EDIT);
    if (CollectionUtils.isNotEmpty(memberEditList)) {
      List<Member> editMembers =
          memberEditList
              .stream()
              .map(data -> getMemberModelForEdit(data, groupId, contextUserId))
              .collect(Collectors.toList());
      if (!editMembers.isEmpty()) {
        logger.info(reqContext,MessageFormat.format(
            "Number of members to be modified in the group {0} are {1}", groupId, editMembers.size()));
        Response editMemberRes = editMembers(editMembers, reqContext);
      }
    }
    List<String> memberRemoveList = (List<String>) memberOperationMap.get(JsonKey.REMOVE);
    if (CollectionUtils.isNotEmpty(memberRemoveList)) {
      List<Member> removeMembers =
          memberRemoveList
              .stream()
              .map(data -> getMemberModelForRemove(data, groupId, contextUserId))
              .collect(Collectors.toList());
      if (!removeMembers.isEmpty()) {
        logger.info(reqContext,MessageFormat.format(
            "Number of members needs to be removed from the group {0} are {1}",
            groupId,
            removeMembers.size()));
        Response removeMemberRes = removeMembers(removeMembers, reqContext);
      }
    }
  }

  @Override
  public Response handleMemberAddition(
      List<Map<String, Object>> memberList,
      String groupId,
      String contextUserId,
      List<Map<String, Object>> userGroupsList,
      Map<String, Object> reqContext)
      throws BaseException {
    logger.info(reqContext,MessageFormat.format("Number of members to be added to the group {0} are {1}", groupId, memberList.size()));
    Response addMemberRes = new Response();
    List<Member> members =
        memberList
            .stream()
            .map(data -> getMemberModelForAdd(data, groupId, contextUserId))
            .collect(Collectors.toList());
    if (!members.isEmpty()) {
      addMemberRes = addMembers(members, userGroupsList, reqContext);
    }
    return addMemberRes;
  }

  /**
   * Fetch Member Details based on Group
   *
   * @param groupId
   * @return
   * @throws BaseException
   */
  @Override
  public List<MemberResponse> readGroupMembers(String groupId, Map<String, Object> reqContext)
      throws BaseException {

    List<MemberResponse> members = fetchMembersByGroupIds(Lists.newArrayList(groupId), reqContext);

    if (!members.isEmpty()) {
      fetchMemberDetails(members, reqContext);
    }
    return members;
  }

  /**
   * Fetch Members based on Group
   *
   * @param groupId
   * @return
   * @throws BaseException
   */
  @Override
  public List<MemberResponse> fetchMembersByGroupId(String groupId, Map<String, Object> reqContext) throws BaseException {
    List<MemberResponse> members = fetchMembersByGroupIds(Lists.newArrayList(groupId), reqContext);
    return members;
  }

  /**
   * Fetch Members based on Group
   *
   * @param groupIds
   * @return
   * @throws BaseException
   */
  @Override
  public List<MemberResponse> fetchMembersByGroupIds(List<String> groupIds, Map<String, Object> reqContext) throws BaseException {
    Response response = memberDao.fetchMembersByGroupIds(groupIds, reqContext);
    List<MemberResponse> members = new ArrayList<>();
    if (null != response && null != response.getResult()) {
      List<Map<String, Object>> dbResMembers =
          (List<Map<String, Object>>) response.getResult().get(JsonKey.RESPONSE);
      if (null != dbResMembers) {
        logger.info(reqContext,MessageFormat.format("Group members fetched count : {0}", dbResMembers.size()));
        dbResMembers.forEach(
            map -> {
              Member member = objectMapper.convertValue(map, Member.class);
              if (JsonKey.ACTIVE.equals(member.getStatus())) {
                MemberResponse memberResponse = createMemberResponseObj(member);
                members.add(memberResponse);
              }
            });
      }
    }
    return members;
  }

  private void fetchMemberDetails(List<MemberResponse> members, Map<String, Object> reqContext)
      throws BaseException {
    List<String> memberIds = new ArrayList<>();
    members.forEach(
        member -> {
          memberIds.add(member.getUserId());
        });
    logger.info(reqContext,"Fetching member names");
    Response response = userService.searchUserByIds(memberIds, reqContext);
    if (null != response && null != response.getResult()) {
      Map<String, Object> memberRes =
          (Map<String, Object>) response.getResult().get(JsonKey.RESPONSE);
      if (null != memberRes) {
        List<Map<String, Object>> userDetails =
            (List<Map<String, Object>>) memberRes.get(JsonKey.CONTENT);
        members.forEach(
            member -> {
              Map<String, Object> userInfo =
                  userDetails
                      .stream()
                      .filter(x -> member.getUserId().equals((String) x.get(JsonKey.ID)))
                      .findFirst()
                      .orElse(null);
              if (userInfo != null) {
                String firstName =
                    StringUtils.isNotEmpty((String) userInfo.get(JsonKey.FIRSTNAME))
                        ? (String) userInfo.get(JsonKey.FIRSTNAME)
                        : "";

                String lastName =
                    StringUtils.isNotEmpty((String) userInfo.get(JsonKey.LASTNAME))
                        ? " " + (String) userInfo.get(JsonKey.LASTNAME)
                        : "";
                member.setName(firstName + lastName);
              }
            });
      }
    }
  }

  @Override
  public List<Map<String, Object>> fetchGroupByUser(List<String> groupIds, String userId, Map<String, Object> reqContext)
      throws BaseException {
    List<Map<String, Object>> dbResMembers = new ArrayList<>();
    Response response = memberDao.fetchGroupByUser(groupIds, userId, reqContext);
    if (null != response && null != response.getResult()) {
      dbResMembers = (List<Map<String, Object>>) response.getResult().get(JsonKey.RESPONSE);
    }
    return dbResMembers;
  }

  private Member getMemberModelForAdd(
      Map<String, Object> data, String groupId, String contextUserId) {
    Member member = new Member();
    member.setGroupId(groupId);
    String role = (String) data.get(JsonKey.ROLE);
    if (StringUtils.isNotEmpty(role)) {
      member.setRole(role);
    } else {
      member.setRole(JsonKey.MEMBER);
    }
    member.setUserId((String) data.get(JsonKey.USER_ID));
    member.setStatus(JsonKey.ACTIVE);
    member.setCreatedBy(contextUserId);
    member.setCreatedOn(new Timestamp(System.currentTimeMillis()));
    if (null != data.get(JsonKey.VISITED)) {
      member.setVisited((Boolean) data.get(JsonKey.VISITED));
    } else {
      member.setVisited(member.getUserId().equals(member.getCreatedBy()) ? true : false);
    }
    return member;
  }

  private Member getMemberModelForEdit(
      Map<String, Object> data, String groupId, String contextUserId) {
    Member member = new Member();
    member.setGroupId(groupId);
    String role = (String) data.get(JsonKey.ROLE);
    if (StringUtils.isNotEmpty(role)) {
      member.setRole(role);
    } else {
      member.setRole(JsonKey.MEMBER);
    }
    if (null != data.get(JsonKey.VISITED)) {
      member.setVisited((Boolean) data.get(JsonKey.VISITED));
    }
    member.setUserId((String) data.get(JsonKey.USER_ID));
    member.setUpdatedBy(contextUserId);
    member.setUpdatedOn(new Timestamp(System.currentTimeMillis()));

    return member;
  }

  private Member getMemberModelForRemove(String userId, String groupId, String contextUserId) {
    Member member = new Member();
    member.setGroupId(groupId);
    member.setUserId(userId);
    member.setStatus(JsonKey.INACTIVE);
    member.setRemovedBy(contextUserId);
    member.setRemovedOn(new Timestamp(System.currentTimeMillis()));

    return member;
  }

  private MemberResponse createMemberResponseObj(Member member) {
    MemberResponse memberResponse = new MemberResponse();
    memberResponse.setGroupId(member.getGroupId());
    memberResponse.setUserId(member.getUserId());
    memberResponse.setRole(member.getRole());
    memberResponse.setStatus(member.getStatus());
    memberResponse.setCreatedBy(member.getCreatedBy());
    memberResponse.setRemovedBy(member.getRemovedBy());
    memberResponse.setUpdatedBy(member.getUpdatedBy());
    memberResponse.setCreatedOn(
        member.getCreatedOn() != null
            ? GroupUtil.convertTimestampToUTC(member.getCreatedOn().getTime())
            : null);
    memberResponse.setUpdatedOn(
        member.getUpdatedOn() != null
            ? GroupUtil.convertTimestampToUTC(member.getUpdatedOn().getTime())
            : null);
    memberResponse.setRemovedOn(
        member.getRemovedOn() != null
            ? GroupUtil.convertTimestampToUTC(member.getRemovedOn().getTime())
            : null);
    return memberResponse;
  }
}
