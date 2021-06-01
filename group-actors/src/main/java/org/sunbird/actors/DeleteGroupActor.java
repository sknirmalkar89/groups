package org.sunbird.actors;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.common.exception.AuthorizationException;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.exception.DBException;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.models.MemberResponse;
import org.sunbird.common.request.Request;
import org.sunbird.common.response.Response;
import org.sunbird.service.GroupService;
import org.sunbird.service.GroupServiceImpl;
import org.sunbird.service.MemberService;
import org.sunbird.service.MemberServiceImpl;
import org.sunbird.telemetry.TelemetryEnvKey;
import org.sunbird.telemetry.util.TelemetryUtil;
import org.sunbird.util.CacheUtil;
import org.sunbird.util.GroupRequestHandler;
import org.sunbird.common.util.JsonKey;
import org.sunbird.util.helper.PropertiesCache;

@ActorConfig(
  tasks = {"deleteGroup"},
  asyncTasks = {},
  dispatcher = "group-dispatcher"
)
public class DeleteGroupActor extends BaseActor {
  private CacheUtil cacheUtil = new CacheUtil();
  private Logger logger = LoggerFactory.getLogger(DeleteGroupActor.class);

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
      logger.error(MessageFormat.format("DeleteGroupActor: Error Code: {0}, Error Msg: {1} ",ResponseCode.GS_DLT01.getErrorCode(),ResponseCode.GS_DLT01.getErrorMessage()));
      throw new AuthorizationException.NotAuthorized(ResponseCode.GS_DLT01);
    }
    GroupService groupService = new GroupServiceImpl();
    Map<String, Object> dbResGroup = null;
   try {
     dbResGroup = readGroup(groupId, groupService);
     // Only Group Creator should be able to delete the group
     if (!userId.equals((String) dbResGroup.get(JsonKey.CREATED_BY))) {
       logger.error(MessageFormat.format("DeleteGroupActor: Error Code: {0}, Error Msg: {1} ",ResponseCode.GS_DLT10.getErrorCode(),ResponseCode.GS_DLT10.getErrorMessage()));
       throw new AuthorizationException.NotAuthorized(ResponseCode.GS_DLT10);
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
     logTelemetry(actorMessage, groupId, dbResGroup,true);
   }catch (DBException ex){
     logger.error(MessageFormat.format("DeleteGroupActor: Error Code: {0}, Error Msg: {1} ",ResponseCode.GS_DLT03.getErrorCode(),ex.getMessage()));
     logTelemetry(actorMessage, groupId, dbResGroup,false);
     throw new BaseException(ResponseCode.GS_DLT03.getErrorCode(), ResponseCode.GS_DLT03.getErrorMessage(),ex.getResponseCode());
   }catch (BaseException ex){
     logger.error(MessageFormat.format("DeleteGroupActor: Error Code: {0}, Error Msg: {1} ",ResponseCode.GS_DLT03.getErrorCode(),ex.getMessage()));
     logTelemetry(actorMessage, groupId, dbResGroup,false);
     throw  new BaseException(ex);
   }catch (Exception ex){
     logger.error(MessageFormat.format("DeleteGroupActor: Error Code: {0}, Error Msg: {1} ",ResponseCode.GS_DLT03.getErrorCode(),ex.getMessage()));
     logTelemetry(actorMessage, groupId, dbResGroup,false);
     throw new BaseException(ResponseCode.GS_DLT03.getErrorCode(), ResponseCode.GS_DLT03.getErrorMessage(),ResponseCode.SERVER_ERROR.getCode());
   }

  }

  private void logTelemetry(Request actorMessage, String groupId, Map<String, Object> dbResGroup, boolean isDeleted) {
    Map<String, Object> targetObject = null;
    List<Map<String, Object>> correlatedObject = new ArrayList<>();
    if(isDeleted) {
      targetObject =
              TelemetryUtil.generateTargetObject(
                      groupId,
                      TelemetryEnvKey.DELETE_GROUP,
                      null,
                      null,
                      (String) dbResGroup.get(JsonKey.STATUS), TelemetryEnvKey.GROUP_DETAIL);
    }else{
      targetObject =
              TelemetryUtil.generateTargetObject(
                      groupId,
                      TelemetryEnvKey.GROUP_ERROR,
                      JsonKey.DELETE,
                      null,
                      null != dbResGroup ? (String) dbResGroup.get(JsonKey.STATUS):null, TelemetryEnvKey.GROUP_DETAIL);
    }
    TelemetryUtil.generateCorrelatedObject(
            (String) actorMessage.getContext().get(JsonKey.USER_ID),
            TelemetryEnvKey.USER,
            null,
            correlatedObject);
    // Add group info information to Cdata
    TelemetryUtil.generateCorrelatedObject(
            groupId,
            TelemetryEnvKey.GROUPID,
            null,
            correlatedObject);
    TelemetryUtil.telemetryProcessingCall(
            actorMessage.getRequest(), targetObject, correlatedObject, actorMessage.getContext());
  }

  private Map<String, Object> readGroup(String groupId, GroupService groupService) throws BaseException {
    try {
      return groupService.readGroup(groupId);
    }catch (BaseException ex){
      logger.error(MessageFormat.format("DeleteGroupActor: Error Code: {0}, Error Msg: {1} ",ResponseCode.GS_DLT07.getErrorCode(),ex.getMessage()));
      throw new BaseException(ResponseCode.GS_DLT07.getErrorCode(),ResponseCode.GS_DLT07.getErrorMessage(),ex.getResponseCode());
    }
  }
}
