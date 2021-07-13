package org.sunbird.actors;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.common.exception.AuthorizationException;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.exception.ValidationException;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.models.Member;
import org.sunbird.common.request.Request;
import org.sunbird.common.response.Response;
import org.sunbird.service.MemberService;
import org.sunbird.service.MemberServiceImpl;
import org.sunbird.util.*;
import org.sunbird.common.util.JsonKey;
import org.sunbird.util.helper.PropertiesCache;

@ActorConfig(
  tasks = {"updateGroupMembership"},
  asyncTasks = {},
  dispatcher = "group-dispatcher"
)
public class UpdateGroupMembershipActor extends BaseActor {
  private CacheUtil cacheUtil = new CacheUtil();
  private LoggerUtil logger = new LoggerUtil(UpdateGroupMembershipActor.class);

  @Override
  public void onReceive(Request request) throws Throwable {
    String operation = request.getOperation();

    switch (operation) {
      case "updateGroupMembership":
        updateGroupMembership(request);
        break;
      default:
        onReceiveUnsupportedMessage("UpdateGroupMembershipActor");
    }
  }

  private void updateGroupMembership(Request actorMessage) {
    logger.info(actorMessage.getContext(),"updateGroupMembership method call");
    GroupRequestHandler requestHandler = new GroupRequestHandler();
    String requestedBy = requestHandler.getRequestedBy(actorMessage);
    String userId = (String) actorMessage.getRequest().get(JsonKey.USER_ID);
    try {
      if (StringUtils.isEmpty(requestedBy) || !requestedBy.equals(userId)) {
        logger.error(actorMessage.getContext(),MessageFormat.format("UpdateGroupMembershipActor: Error Code: {0}, Error Msg: {1}",
                ResponseCode.GS_MBRSHP_UDT01.getErrorCode(),ResponseCode.GS_MBRSHP_UDT01.getErrorMessage()));
        throw new AuthorizationException.NotAuthorized(ResponseCode.GS_MBRSHP_UDT01);
      }

    logger.info(actorMessage.getContext(),MessageFormat.format("Update groups details for the userId {0}", userId));
    List<Map<String, Object>> groups =
        (List<Map<String, Object>>) actorMessage.getRequest().get(JsonKey.GROUPS);

      List<Member> members = createMembersUpdateRequest(groups, userId);
    Response response = new Response();

      if (CollectionUtils.isNotEmpty(members)) {
        MemberService memberService = new MemberServiceImpl();
        response = memberService.editMembers(members,actorMessage.getContext());
      }
      boolean isUseridRedisEnabled =
              Boolean.parseBoolean(
                      PropertiesCache.getInstance().getConfigValue(JsonKey.ENABLE_USERID_REDIS_CACHE));
      if (isUseridRedisEnabled) {
        // Remove updated groups from cache
        groups.forEach(
                group -> {
                  String groupId = (String) group.get(JsonKey.GROUP_ID);
                  cacheUtil.delCache(groupId + "_" + JsonKey.MEMBERS);
                });

        // Remove user cache list
        cacheUtil.delCache(userId);
      }
      sender().tell(response, self());
      TelemetryHandler.logGroupMembershipUpdateTelemetry(actorMessage, userId,true);
    }catch (Exception ex){
      logger.debug(actorMessage.getContext(),MessageFormat.format("UpdateGroupMembershipActor: Request: {0}",actorMessage.getRequest()));
      TelemetryHandler.logGroupMembershipUpdateTelemetry(actorMessage, userId,false);
      try{
        ExceptionHandler.handleExceptions(actorMessage, ex, ResponseCode.GS_MBRSHP_UDT03);
      }catch (BaseException e){
        logger.error(actorMessage.getContext(),MessageFormat.format("UpdateGroupMembershipActor:  Error Msg: {0} ",e.getMessage()),e);
        throw e;
      }
    }
  }

  private List<Member> createMembersUpdateRequest(List<Map<String, Object>> groups, String userId) {
    List<Member> members = new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper();
    for (Map<String, Object> groupMembership : groups) {
      Member member = mapper.convertValue(groupMembership, Member.class);
      // TODO: Needs to be removed in future with role based access, Now allowing only visited flag
      // to be updated
      if (member != null
          && (StringUtils.isNotEmpty(member.getRole())
              || StringUtils.isNotEmpty(member.getStatus()))) {
        throw new AuthorizationException.NotAuthorized(ResponseCode.GS_MBRSHP_UDT01);
      }
      member.setUserId(userId);
      if (StringUtils.isBlank(member.getGroupId())) {
        throw new ValidationException.MandatoryParamMissing(JsonKey.GROUP_ID, JsonKey.GROUPS, ResponseCode.GS_MBRSHP_UDT02);
      }
      members.add(member);
    }
    return members;
  }

}
