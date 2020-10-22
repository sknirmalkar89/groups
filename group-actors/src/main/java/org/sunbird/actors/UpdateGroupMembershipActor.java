package org.sunbird.actors;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.exception.AuthorizationException;
import org.sunbird.exception.ValidationException;
import org.sunbird.models.Member;
import org.sunbird.request.Request;
import org.sunbird.response.Response;
import org.sunbird.service.MemberService;
import org.sunbird.service.MemberServiceImpl;
import org.sunbird.telemetry.TelemetryEnvKey;
import org.sunbird.telemetry.util.TelemetryUtil;
import org.sunbird.util.CacheUtil;
import org.sunbird.util.GroupRequestHandler;
import org.sunbird.util.JsonKey;
import org.sunbird.util.helper.PropertiesCache;

@ActorConfig(
  tasks = {"updateGroupMembership"},
  asyncTasks = {},
  dispatcher = "group-dispatcher"
)
public class UpdateGroupMembershipActor extends BaseActor {
  private CacheUtil cacheUtil = new CacheUtil();

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
    logger.info("updateGroupMembership method call");
    GroupRequestHandler requestHandler = new GroupRequestHandler();
    String requestedBy = requestHandler.getRequestedBy(actorMessage);
    String userId = (String) actorMessage.getRequest().get(JsonKey.USER_ID);
    if (StringUtils.isEmpty(requestedBy) || !requestedBy.equals(userId)) {
      throw new AuthorizationException.NotAuthorized();
    }
    logger.info("Update groups details for the userId {}", userId);
    List<Map<String, Object>> groups =
        (List<Map<String, Object>>) actorMessage.getRequest().get(JsonKey.GROUPS);
    List<Member> members = createMembersUpdateRequest(groups, userId);
    Response response = new Response();
    if (CollectionUtils.isNotEmpty(members)) {
      MemberService memberService = new MemberServiceImpl();
      response = memberService.editMembers(members);
    }
    boolean isUseridRedisEnabled =
        Boolean.parseBoolean(
            PropertiesCache.getInstance().getConfigValue(JsonKey.ENABLE_USERID_REDIS_CACHE));
    if (isUseridRedisEnabled) {
      // Remove updated groups from cache
      groups.forEach(
          group -> {
            String groupId = (String) group.get(JsonKey.GROUP_ID);
            cacheUtil.deleteCacheSync(groupId);
            cacheUtil.delCache(groupId + "_" + JsonKey.MEMBERS);
          });

      // Remove user cache list
      cacheUtil.delCache(userId);
    }
    sender().tell(response, self());
    logTelemetry(actorMessage, userId);
  }

  private List<Member> createMembersUpdateRequest(List<Map<String, Object>> groups, String userId) {
    List<Member> members = new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper();
    for (Map<String, Object> groupMembership : groups) {
      Member member = mapper.convertValue(groupMembership, Member.class);
      member.setUserId(userId);
      if (StringUtils.isBlank(member.getGroupId())) {
        throw new ValidationException.MandatoryParamMissing(JsonKey.GROUP_ID, JsonKey.GROUPS);
      }
      members.add(member);
    }
    return members;
  }

  private void logTelemetry(Request actorMessage, String userId) {
    String source =
        actorMessage.getContext().get(JsonKey.REQUEST_SOURCE) != null
            ? (String) actorMessage.getContext().get(JsonKey.REQUEST_SOURCE)
            : "";

    List<Map<String, Object>> correlatedObject = new ArrayList<>();
    if (StringUtils.isNotBlank(source)) {
      TelemetryUtil.generateCorrelatedObject(
          source, StringUtils.capitalize(JsonKey.REQUEST_SOURCE), null, correlatedObject);
    }
    Map<String, Object> targetObject = null;
    targetObject =
        TelemetryUtil.generateTargetObject(userId, TelemetryEnvKey.USER, JsonKey.UPDATE, null);

    TelemetryUtil.generateCorrelatedObject(
        (String) actorMessage.getContext().get(JsonKey.USER_ID),
        TelemetryEnvKey.USER,
        null,
        correlatedObject);
    TelemetryUtil.telemetryProcessingCall(
        actorMessage.getRequest(), targetObject, correlatedObject, actorMessage.getContext());
  }
}
