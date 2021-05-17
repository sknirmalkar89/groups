package org.sunbird.actors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.exception.AuthorizationException;
import org.sunbird.exception.BaseException;
import org.sunbird.exception.DBException;
import org.sunbird.message.ResponseCode;
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
import org.sunbird.util.JsonKey;
import org.sunbird.util.helper.PropertiesCache;

@ActorConfig(
  tasks = {"deleteGroup"},
  asyncTasks = {},
  dispatcher = "group-dispatcher"
)
public class DeleteGroupActor extends BaseActor {
  private CacheUtil cacheUtil = new CacheUtil();

  @Override
  public void onReceive(Request request) throws Throwable {
    String operation = request.getOperation();
    switch (operation) {
      case "deleteGroup":
        deleteGroup(request);
        break;
      default:
        onReceiveUnsupportedMessage("DeleteGroupActor");
    }
  }

  /**
   * This method will delete the group
   *
   * @param actorMessage
   * @throws BaseException
   */
  private void deleteGroup(Request actorMessage) throws BaseException {
    logger.info("DeleteGroup method call");
    GroupRequestHandler requestHandler = new GroupRequestHandler();
    String userId = requestHandler.getRequestedBy(actorMessage);
    String groupId = (String) actorMessage.getRequest().get(JsonKey.GROUP_ID);
    logger.info("Delete group for the groupId {}", groupId);

    if (StringUtils.isEmpty(userId)) {
      throw new AuthorizationException.NotAuthorized(ResponseCode.GS_DLT_01);
    }
    GroupService groupService = new GroupServiceImpl();
   try {
     Map<String, Object> dbResGroup = readGroup(groupId, groupService);
     // Only Group Creator should be able to delete the group
     if (!userId.equals((String) dbResGroup.get(JsonKey.CREATED_BY))) {
       throw new AuthorizationException.NotAuthorized(ResponseCode.GS_DLT_04);
     }

     // Get all members belong to the group
     MemberService memberService = new MemberServiceImpl();
     List<MemberResponse> membersInDB = memberService.fetchMembersByGroupId(groupId);
     Response response = groupService.deleteGroup(groupId, membersInDB);
     // delete cache for the group and all members belong to the group
     boolean isUseridRedisEnabled =
             Boolean.parseBoolean(
                     PropertiesCache.getInstance().getConfigValue(JsonKey.ENABLE_USERID_REDIS_CACHE));
     if (isUseridRedisEnabled) {
       cacheUtil.deleteCacheSync(groupId);
       cacheUtil.delCache(groupId + "_" + JsonKey.MEMBERS);
       // Remove group list user cache from redis
       membersInDB.forEach(member -> cacheUtil.delCache(member.getUserId()));
     }
     sender().tell(response, self());
     Map<String, Object> targetObject = null;
     List<Map<String, Object>> correlatedObject = new ArrayList<>();
     targetObject =
             TelemetryUtil.generateTargetObject(
                     groupId,
                     TelemetryEnvKey.GROUP,
                     JsonKey.DELETE,
                     (String) dbResGroup.get(JsonKey.STATUS));
     TelemetryUtil.telemetryProcessingCall(
             actorMessage.getRequest(), targetObject, correlatedObject, actorMessage.getContext());
    }catch(DBException ex){
      throw new BaseException(ResponseCode.GS_DLT_05.getErrorCode(), ResponseCode.GS_DLT_05.getErrorMessage(),ex.getResponseCode());
    }

  }

  private Map<String, Object> readGroup(String groupId, GroupService groupService) throws BaseException {
    try {
      Map<String, Object> dbResGroup = groupService.readGroup(groupId);
      return dbResGroup;
    }catch (BaseException ex){
      throw new BaseException(ResponseCode.GS_DLT_03.getErrorCode(),ResponseCode.GS_DLT_03.getErrorMessage(),ex.getResponseCode());
    }
  }
}
