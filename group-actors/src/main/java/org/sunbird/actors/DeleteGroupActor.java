package org.sunbird.actors;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.common.exception.AuthorizationException;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.models.MemberResponse;
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
  tasks = {"deleteGroup"},
  asyncTasks = {},
  dispatcher = "group-dispatcher"
)
public class DeleteGroupActor extends BaseActor {
  private CacheUtil cacheUtil = new CacheUtil();
  private LoggerUtil logger = new LoggerUtil(DeleteGroupActor.class);

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
    logger.info(actorMessage.getContext(),"DeleteGroup method call");
    GroupRequestHandler requestHandler = new GroupRequestHandler();
    String userId = requestHandler.getRequestedBy(actorMessage);
    String groupId = (String) actorMessage.getRequest().get(JsonKey.GROUP_ID);
    logger.info(actorMessage.getContext(),MessageFormat.format("Delete group for the groupId {0}", groupId));

    if (StringUtils.isEmpty(userId)) {
      logger.error(actorMessage.getContext(),MessageFormat.format("DeleteGroupActor: Error Code: {0}, Error Msg: {1} ",ResponseCode.GS_DLT01.getErrorCode(),ResponseCode.GS_DLT01.getErrorMessage()));
      throw new AuthorizationException.NotAuthorized(ResponseCode.GS_DLT01);
    }
    GroupService groupService = new GroupServiceImpl();
    Map<String, Object> dbResGroup = null;
   try {
     dbResGroup = readGroup(groupId, groupService, actorMessage.getContext());
     // Only Group Creator should be able to delete the group
     if (!userId.equals((String) dbResGroup.get(JsonKey.CREATED_BY))) {
       logger.error(actorMessage.getContext(),MessageFormat.format("DeleteGroupActor: Error Code: {0}, Error Msg: {1} ",ResponseCode.GS_DLT10.getErrorCode(),ResponseCode.GS_DLT10.getErrorMessage()));
       throw new AuthorizationException.NotAuthorized(ResponseCode.GS_DLT10);
     }

     // Get all members belong to the group
     MemberService memberService = new MemberServiceImpl();
     List<MemberResponse> membersInDB = memberService.fetchMembersByGroupId(groupId, actorMessage.getContext());
     Response response = groupService.deleteGroup(groupId, membersInDB, actorMessage.getContext());
     // delete cache for the group and all members belong to the group
     boolean isUseridRedisEnabled =
             Boolean.parseBoolean(
                     PropertiesCache.getInstance().getConfigValue(JsonKey.ENABLE_USERID_REDIS_CACHE));
     if (isUseridRedisEnabled) {
       cacheUtil.deleteCacheSync(groupId, actorMessage.getContext());
       cacheUtil.delCache(groupId + "_" + JsonKey.MEMBERS);
       // Remove group list user cache from redis
       membersInDB.forEach(member -> cacheUtil.delCache(member.getUserId()));
     }
     sender().tell(response, self());
     TelemetryHandler.logGroupDeleteTelemetry(actorMessage, groupId, dbResGroup,true);
   }catch (Exception ex){
     logger.debug(actorMessage.getContext(),MessageFormat.format("DeleteGroupActor: Request: {0}",actorMessage.getRequest()));
     TelemetryHandler.logGroupDeleteTelemetry(actorMessage, groupId, dbResGroup,false);
     try{
       ExceptionHandler.handleExceptions(actorMessage, ex, ResponseCode.GS_DLT03);
     }catch (BaseException e){
       logger.error(actorMessage.getContext(),MessageFormat.format("DeleteGroupActor: Error Msg: {0} ",e.getMessage()),e);
       throw e;
     }
   }
  }

  private Map<String, Object> readGroup(String groupId, GroupService groupService, Map<String,Object> reqContext) throws BaseException {
    try {
      return groupService.readGroup(groupId,reqContext);
    }catch (BaseException ex){
      throw new BaseException(ResponseCode.GS_DLT07.getErrorCode(),ResponseCode.GS_DLT07.getErrorMessage(),ex.getResponseCode());
    }
  }
}
