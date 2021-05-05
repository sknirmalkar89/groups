package org.sunbird.actors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.exception.AuthorizationException;
import org.sunbird.exception.BaseException;
import org.sunbird.exception.ValidationException;
import org.sunbird.message.ResponseCode;
import org.sunbird.models.Group;
import org.sunbird.models.MemberResponse;
import org.sunbird.request.Request;
import org.sunbird.response.Response;
import org.sunbird.service.GroupService;
import org.sunbird.service.GroupServiceImpl;
import org.sunbird.service.MemberService;
import org.sunbird.service.MemberServiceImpl;
import org.sunbird.telemetry.TelemetryEnvKey;
import org.sunbird.telemetry.util.TelemetryUtil;
import org.sunbird.util.CacheUtil;
import org.sunbird.util.GroupRequestHandler;
import org.sunbird.util.GroupUtil;
import org.sunbird.util.JsonKey;
import org.sunbird.util.helper.PropertiesCache;

@ActorConfig(
  tasks = {"updateGroup"},
  asyncTasks = {},
  dispatcher = "group-dispatcher"
)
public class UpdateGroupActor extends BaseActor {
  private CacheUtil cacheUtil = new CacheUtil();

  @Override
  public void onReceive(Request request) throws Throwable {
    String operation = request.getOperation();
    switch (operation) {
      case "updateGroup":
        updateGroup(request);
        break;
      default:
        onReceiveUnsupportedMessage("UpdateGroupActor");
    }
  }
  /**
   * This method will update group in cassandra based on group id.
   *
   * @param actorMessage
   */
  private void updateGroup(Request actorMessage) throws BaseException {
    logger.info("UpdateGroup method call");

    GroupRequestHandler requestHandler = new GroupRequestHandler();
    Group group = requestHandler.handleUpdateGroupRequest(actorMessage);
    GroupService groupService = new GroupServiceImpl();

    logger.info("Update group for the groupId {}", group.getId());

    String userId = group.getUpdatedBy();
    if (StringUtils.isEmpty(userId)) {
      throw new AuthorizationException.NotAuthorized();
    }

    Map<String, Object> dbResGroup = groupService.readGroup(group.getId());

    // Check if it is an exit group request
    boolean isExitGroupRequest =
        isExitGroupRequest(
            group, userId, (Map<String, Object>) actorMessage.getRequest().get(JsonKey.MEMBERS));
    // Only exit group and activate group request is allowed in suspended group state
    if (!isExitGroupRequest
        && JsonKey.SUSPENDED.equals(dbResGroup.get(JsonKey.STATUS))
        && (StringUtils.isBlank(group.getStatus())
            || JsonKey.SUSPENDED.equals(group.getStatus()))) {
      throw new ValidationException.GroupNotActive(group.getId());
    }

    Map<String, List<Map<String, String>>> responseMap = new HashMap<>();
    // member validation and updates to group
    MemberService memberService = new MemberServiceImpl();
    List<MemberResponse> membersInDB = memberService.fetchMembersByGroupId(group.getId());

    // Check if user is authorized to delete ,suspend and re-activate operation
    // Allow all member to exit the group
    if (!isExitGroupRequest) {
      checkUserAuthorization(
          dbResGroup, membersInDB, group.getStatus(), userId, actorMessage.getRequest());
    }

    if (MapUtils.isNotEmpty((Map) actorMessage.getRequest().get(JsonKey.MEMBERS))) {
      responseMap.put(
          JsonKey.MEMBERS,
          validateMembersAndSave(
              group.getId(),
              (Map) actorMessage.getRequest().get(JsonKey.MEMBERS),
              userId,
              membersInDB));
    }
    // Activity validation
    if (MapUtils.isNotEmpty(
        (Map<String, Object>) actorMessage.getRequest().get(JsonKey.ACTIVITIES))) {
      responseMap.put(
          JsonKey.ACTIVITIES,
          validateActivityList(
              group, (Map<String, Object>) actorMessage.getRequest().get(JsonKey.ACTIVITIES)));
    }
    boolean deleteFromUserCache = false;
    // Group and activity updates
    if (group != null
        && (StringUtils.isNotEmpty(group.getDescription())
            || StringUtils.isNotEmpty(group.getName())
            || StringUtils.isNotEmpty(group.getMembershipType())
            || StringUtils.isNotEmpty(group.getStatus())
            || MapUtils.isNotEmpty(
                (Map<String, Object>) actorMessage.getRequest().get(JsonKey.ACTIVITIES)))) {
      cacheUtil.deleteCacheSync(group.getId());
      // if name, description and status update happens in group , delete cache for all the members
      // belongs to that group
      deleteFromUserCache = true;
      // if inactive status then delete group included to support backward compatability for old
      // mobile apps
      if (JsonKey.INACTIVE.equals(group.getStatus())) {
        Response response = groupService.deleteGroup(group.getId(), membersInDB);
      } else {
        Response response = groupService.updateGroup(group);
      }
    }

    boolean isUseridRedisEnabled =
        Boolean.parseBoolean(
            PropertiesCache.getInstance().getConfigValue(JsonKey.ENABLE_USERID_REDIS_CACHE));
    if (isUseridRedisEnabled) {
      cacheUtil.deleteCacheSync(userId);
      // Remove group list user cache from redis
      deleteUserCache(
          (Map) actorMessage.getRequest().get(JsonKey.MEMBERS), membersInDB, deleteFromUserCache);
    }

    Response response = new Response(ResponseCode.OK.getCode());
    response.put(JsonKey.RESPONSE, JsonKey.SUCCESS);
    if (MapUtils.isNotEmpty(responseMap)
        && (CollectionUtils.isNotEmpty(responseMap.get(JsonKey.MEMBERS))
            || CollectionUtils.isNotEmpty(responseMap.get(JsonKey.ACTIVITIES)))) {
      response.put(JsonKey.ERROR, responseMap);
    }
    sender().tell(response, self());

    logTelemetry(actorMessage, group, dbResGroup);
  }

  private boolean isExitGroupRequest(Group group, String userId, Map<String, Object> members) {
    boolean isExitRequest = false;
    if (group != null
        && (StringUtils.isNotEmpty(group.getDescription())
            || StringUtils.isNotEmpty(group.getName())
            || StringUtils.isNotEmpty(group.getMembershipType())
            || StringUtils.isNotEmpty(group.getStatus())
            || CollectionUtils.isNotEmpty(group.getActivities()))) {
      isExitRequest = false;
    } else if (group != null
        && MapUtils.isNotEmpty(members)
        && !members.containsKey(JsonKey.ADD)
        && !members.containsKey(JsonKey.EDIT)) {
      List<String> removeMemberList = (List<String>) members.get(JsonKey.REMOVE);
      isExitRequest = removeMemberList.size() == 1 && userId.equals(removeMemberList.get(0));
    }
    return isExitRequest;
  }

  private void checkUserAuthorization(
      Map<String, Object> dbResGroup,
      List<MemberResponse> membersInDB,
      String status,
      String userId,
      Map<String, Object> groupRequest) {
    MemberResponse member =
        membersInDB.stream().filter(x -> x.getUserId().equals(userId)).findAny().orElse(null);
    // Check User is authorized Suspend , Re-activate or delete the group .
    if ((JsonKey.ACTIVE.equals(status) || JsonKey.SUSPENDED.equals(status))
        && (member == null || !JsonKey.ADMIN.equals(member.getRole()))) {
      throw new AuthorizationException.NotAuthorized();
    }

    if (JsonKey.INACTIVE.equals(status)
        && !userId.equals((String) dbResGroup.get(JsonKey.CREATED_BY))) {
      throw new AuthorizationException.NotAuthorized();
    }

    // check only admin should be able to update name, description, status ,add,edit or remove
    // members
    if (StringUtils.isNotEmpty((String) groupRequest.get(JsonKey.GROUP_DESC))
        || StringUtils.isNotEmpty((String) groupRequest.get(JsonKey.GROUP_NAME))
        || StringUtils.isNotEmpty((String) groupRequest.get(JsonKey.GROUP_MEMBERSHIP_TYPE))
        || StringUtils.isNotEmpty((String) groupRequest.get(JsonKey.GROUP_STATUS))
        || MapUtils.isNotEmpty((Map) groupRequest.get(JsonKey.MEMBERS))) {
      if (member == null || !JsonKey.ADMIN.equals(member.getRole())) {
        throw new AuthorizationException.NotAuthorized();
      }
    }
  }

  private List<Map<String, String>> validateActivityList(
      Group group, Map<String, Object> activityOperationMap) {
    List<Map<String, Object>> updateActivityList =
        new GroupServiceImpl().handleActivityOperations(group.getId(), activityOperationMap);
    List<Map<String, String>> activityErrorList = new ArrayList<>();
    boolean isActivityLimitExceeded =
        GroupUtil.checkMaxActivityLimit(updateActivityList.size(), activityErrorList);
    if (!isActivityLimitExceeded) {
      group.setActivities(updateActivityList);
    }
    return activityErrorList;
  }

  private List<Map<String, String>> validateMembersAndSave(
      String groupId,
      Map memberOperationMap,
      String requestedBy,
      List<MemberResponse> membersInDB) {
    List<Map<String, String>> memberErrorList = new ArrayList<>();

    MemberService memberService = new MemberServiceImpl();

    GroupRequestHandler requestHandler = new GroupRequestHandler();
    // Validate Member Addition
    if (CollectionUtils.isNotEmpty(
        (List<Map<String, Object>>) memberOperationMap.get(JsonKey.ADD))) {
      requestHandler.validateAddMembers(memberOperationMap, membersInDB, memberErrorList);
    }
    // Validate Member Update
    if (CollectionUtils.isNotEmpty(
        (List<Map<String, Object>>) memberOperationMap.get(JsonKey.EDIT))) {
      requestHandler.validateEditMembers(memberOperationMap, membersInDB, memberErrorList);
    }
    // Validate Member Remove
    if (CollectionUtils.isNotEmpty((List<String>) memberOperationMap.get(JsonKey.REMOVE))) {
      requestHandler.validateRemoveMembers(memberOperationMap, membersInDB, memberErrorList);
    }
    int totalMemberCount = GroupUtil.totalMemberCount(memberOperationMap, membersInDB);
    boolean memberLimit = GroupUtil.checkMaxMemberLimit(totalMemberCount, memberErrorList);
    cacheUtil.delCache(groupId + "_" + JsonKey.MEMBERS);
    if (!memberLimit) {
      memberService.handleMemberOperations(memberOperationMap, groupId, requestedBy);
    }
    return memberErrorList;
  }

  private void deleteUserCache(
      Map memberOperationMap, List<MemberResponse> dbMembers, boolean deleteFromUserCache) {
    if (MapUtils.isNotEmpty(memberOperationMap)) {
      List<Map<String, Object>> memberAddList =
          (List<Map<String, Object>>) memberOperationMap.get(JsonKey.ADD);
      if (CollectionUtils.isNotEmpty(memberAddList)) {
        memberAddList.forEach(member -> cacheUtil.delCache((String) (member.get(JsonKey.USER_ID))));
      }
      List<Map<String, Object>> memberEditList =
          (List<Map<String, Object>>) memberOperationMap.get(JsonKey.EDIT);
      if (CollectionUtils.isNotEmpty(memberEditList)) {
        memberEditList.forEach(
            member -> cacheUtil.delCache((String) (member.get(JsonKey.USER_ID))));
      }
      List<String> memberRemoveList = (List<String>) memberOperationMap.get(JsonKey.REMOVE);
      if (CollectionUtils.isNotEmpty(memberRemoveList)) {
        memberRemoveList.forEach(member -> cacheUtil.delCache(member));
      }
    }
    if (CollectionUtils.isNotEmpty(dbMembers) && deleteFromUserCache) {
      dbMembers.forEach(member -> cacheUtil.delCache(member.getUserId()));
    }
  }

  private void logTelemetry(Request actorMessage, Group group, Map<String, Object> dbResGroup) {
    Map<String, Object> targetObject = null;
    List<Map<String, Object>> correlatedObject = new ArrayList<>();
    if (null != group.getStatus()) {
      switch (group.getStatus()) {
        case JsonKey.ACTIVE:
          targetObject =
              TelemetryUtil.generateTargetObject(
                  group.getId(),
                  TelemetryEnvKey.GROUP,
                  JsonKey.ACTIVE,
                  (String) dbResGroup.get(JsonKey.STATUS));
          break;

        case JsonKey.SUSPENDED:
          targetObject =
              TelemetryUtil.generateTargetObject(
                  group.getId(),
                  TelemetryEnvKey.GROUP,
                  JsonKey.SUSPENDED,
                  (String) dbResGroup.get(JsonKey.STATUS));
          break;

        default:
          targetObject =
              TelemetryUtil.generateTargetObject(
                  group.getId(), TelemetryEnvKey.GROUP, JsonKey.UPDATE, null);
      }
      TelemetryUtil.telemetryProcessingCall(
          actorMessage.getRequest(), targetObject, correlatedObject, actorMessage.getContext());
    }
  }
}
