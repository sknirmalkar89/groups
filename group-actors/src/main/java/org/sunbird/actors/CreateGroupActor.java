package org.sunbird.actors;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.common.exception.AuthorizationException;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.models.Group;
import org.sunbird.common.request.Request;
import org.sunbird.common.response.Response;
import org.sunbird.service.GroupService;
import org.sunbird.service.GroupServiceImpl;
import org.sunbird.service.MemberService;
import org.sunbird.service.MemberServiceImpl;
import org.sunbird.util.*;
import org.sunbird.common.util.JsonKey;
import org.sunbird.util.helper.PropertiesCache;

@ActorConfig(
  tasks = {"createGroup"},
  asyncTasks = {},
  dispatcher = "group-dispatcher"
)
public class CreateGroupActor extends BaseActor {
  private CacheUtil cacheUtil = new CacheUtil();
  private static LoggerUtil logger = new LoggerUtil(CreateGroupActor.class);

  @Override
  public void onReceive(Request request) throws Throwable {
    String operation = request.getOperation();
    switch (operation) {
      case "createGroup":
        createGroup(request);
        break;
      default:
        onReceiveUnsupportedMessage("CreateGroupActor");
    }
  }
  /**
   * This method will create group in cassandra.
   *
   * @param actorMessage
   */
  private void createGroup(Request actorMessage) throws BaseException {
    logger.info(actorMessage.getContext(),"In createGroup() actor");
    GroupService groupService = new GroupServiceImpl();
    MemberService memberService = new MemberServiceImpl();

    GroupRequestHandler requestHandler = new GroupRequestHandler();
    Group group = requestHandler.handleCreateGroupRequest(actorMessage);

    String userId = group.getCreatedBy();
    String groupId=null;
    try {
      if (StringUtils.isEmpty(userId)) {
        throw new AuthorizationException.NotAuthorized(ResponseCode.GS_CRT01);
      }

      // add creator of group to memberList as admin
      List<Map<String, Object>> memberList = new ArrayList<>();
      Map<String, Object> createdUser = new HashMap<>();
      createdUser.put(JsonKey.USER_ID, userId);
      createdUser.put(JsonKey.ROLE, JsonKey.ADMIN);
      memberList.add(createdUser);

      // adding members to group, if members are provided in request
      List<Map<String, Object>> reqMemberList =
          (List<Map<String, Object>>) actorMessage.getRequest().get(JsonKey.MEMBERS);
      if (CollectionUtils.isNotEmpty(reqMemberList)) {
        memberList.addAll(reqMemberList);
      }

      logger.info(actorMessage.getContext(), MessageFormat.format("Fetching groups from user-group for userId {0}", userId));
      List<Map<String, Object>> userGroupsList =
              memberService.getGroupIdsforUserIds(GroupUtil.getMemberIdListFromMap(memberList), actorMessage.getContext());
      validateMaxGroupLimitation(userId, userGroupsList);
      Map<String, List<Map<String, String>>> validationErrors = new HashMap<>();
      boolean  memberLimitExceeded = validateMaxMemberLimitation(memberList, validationErrors,actorMessage.getContext());
      boolean  activityLimitExceeded = validateMaxActivitiesLimitation(group, validationErrors,actorMessage.getContext());
      if (activityLimitExceeded) {
         // if activity limit exceeded, we should not add into the db
        group.setActivities(null);
      }
      groupId = groupService.createGroup(group, actorMessage.getContext());
      if (CollectionUtils.isNotEmpty(memberList)) {
          logger.info(actorMessage.getContext(), MessageFormat.format("Adding members to the group: {0} started", groupId));
          boolean isUseridRedisEnabled =
                  Boolean.parseBoolean(
                          PropertiesCache.getInstance().getConfigValue(JsonKey.ENABLE_USERID_REDIS_CACHE));
          if (isUseridRedisEnabled) {
            // Remove group list user cache from redis
            cacheUtil.deleteCacheSync(userId,actorMessage.getContext());
            deleteUserCache(memberList,actorMessage.getContext());
          }
          // if memberLimitExceeded is true, then members are not added in to the group,but group will
          // be created, and groupId is returned in response ,with a errorMsg EXCEEDED_MEMBER_MAX_LIMIT
          // this is not used/expected call flow for creating group. Doing this for direct api hits.
          if (!memberLimitExceeded) {
            Response addMembersRes =
                    memberService.handleMemberAddition(memberList, groupId, userId, userGroupsList, actorMessage.getContext());
            logger.info(actorMessage.getContext(), MessageFormat.format(
                    "Adding members to the group : {0} ended , response {1}",
                    groupId,
                    addMembersRes.getResult()));
          }
        }

        Response response = new Response();
        response.put(JsonKey.GROUP_ID, groupId);
        if (MapUtils.isNotEmpty(validationErrors)
                && (CollectionUtils.isNotEmpty(validationErrors.get(JsonKey.MEMBERS))
                || CollectionUtils.isNotEmpty(validationErrors.get(JsonKey.ACTIVITIES)))) {
          response.put(JsonKey.ERROR, validationErrors);
        }
        logger.info(actorMessage.getContext(), MessageFormat.format("group created successfully with groupId {0}", groupId));
        sender().tell(response, self());

    }catch (Exception ex){
      logger.debug(actorMessage.getContext(),MessageFormat.format("CreateGroupActor: Request: {0}",actorMessage.getRequest()));
      try {
        ExceptionHandler.handleExceptions(actorMessage, ex, ResponseCode.GS_CRT03);
      }catch (BaseException e){
        logger.error(actorMessage.getContext(),
                MessageFormat.format("CreateGroupActor:Error Msg: {0} ",e.getMessage()),
                e);
        throw e;
      }
    } finally {
      TelemetryHandler.logGroupCreateTelemetry(actorMessage, groupId);
    }
  }


    private boolean validateMaxActivitiesLimitation(Group group, Map<String, List<Map<String, String>>> validationErrors, Map<String,Object> reqContext) throws BaseException {

      List<Map<String, String>> activityErrorList = new ArrayList<>();
      validationErrors.put(JsonKey.ACTIVITIES, activityErrorList);
      boolean maxActivityLimit = GroupUtil.checkMaxActivityLimit(group.getActivities() != null ? group.getActivities().size() : 0);
      if(maxActivityLimit){
        String errMsg = MessageFormat.format(ResponseCode.GS_CRT06.getErrorMessage(),
                  PropertiesCache.getInstance().getProperty(JsonKey.MAX_ACTIVITY_LIMIT));
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put(JsonKey.ERROR_MESSAGE, errMsg);
        errorMap.put(JsonKey.ERROR_CODE, ResponseCode.GS_CRT06.getErrorCode());
        activityErrorList.add(errorMap);
      }
      return maxActivityLimit;

  }

  private boolean validateMaxMemberLimitation(List<Map<String, Object>> memberList, Map<String, List<Map<String, String>>> validationErrors,Map<String,Object> reqContext) {
      List<Map<String, String>> memberErrorList = new ArrayList<>();
      validationErrors.put(JsonKey.MEMBERS, memberErrorList);
      boolean maxMemberLimit = GroupUtil.checkMaxMemberLimit(memberList.size());
      if(maxMemberLimit){
          String errMsg = MessageFormat.format(ResponseCode.GS_CRT05.getErrorMessage(),
                  PropertiesCache.getInstance().getProperty(JsonKey.MAX_GROUP_MEMBERS_LIMIT));
          Map<String, String> errorMap = new HashMap<>();
          errorMap.put(JsonKey.ERROR_MESSAGE, errMsg);
          errorMap.put(JsonKey.ERROR_CODE, ResponseCode.GS_CRT05.getErrorCode());
          memberErrorList.add(errorMap);
      }
      return maxMemberLimit;

  }

  private void validateMaxGroupLimitation(String userId, List<Map<String, Object>> userGroupsList) {
    boolean maxGroupLimitExceed = GroupUtil.checkMaxGroupLimit(userGroupsList, userId);
    if(maxGroupLimitExceed){
      throw new BaseException(ResponseCode.GS_CRT04.getErrorCode(),
              MessageFormat.format(ResponseCode.GS_CRT04.getErrorMessage(),
                      PropertiesCache.getInstance().getProperty(JsonKey.MAX_GROUP_LIMIT)),ResponseCode.CLIENT_ERROR.getResponseCode());
    }
  }

  private void deleteUserCache(List<Map<String, Object>> memberList,Map<String,Object> reqContext) {
    CacheUtil cacheUtil = new CacheUtil();
    logger.info(reqContext,"Delete user cache from redis");
    memberList.forEach(member -> cacheUtil.delCache((String) (member.get(JsonKey.USER_ID))));
  }

}
