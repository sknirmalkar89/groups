package org.sunbird.actors;

import com.fasterxml.jackson.core.type.TypeReference;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.models.GroupResponse;
import org.sunbird.models.MemberResponse;
import org.sunbird.common.request.Request;
import org.sunbird.common.response.Response;
import org.sunbird.service.GroupService;
import org.sunbird.service.GroupServiceImpl;
import org.sunbird.service.MemberService;
import org.sunbird.service.MemberServiceImpl;
import org.sunbird.util.CacheUtil;
import org.sunbird.common.util.JsonKey;
import org.sunbird.util.ExceptionHandler;
import org.sunbird.util.JsonUtils;
import org.sunbird.util.LoggerUtil;

@ActorConfig(
  tasks = {"readGroup"},
  asyncTasks = {},
  dispatcher = "group-dispatcher"
)
public class ReadGroupActor extends BaseActor {

  private LoggerUtil logger = new LoggerUtil(ReadGroupActor.class);

  @Override
  public void onReceive(Request request) throws Throwable {
    String operation = request.getOperation();
    switch (operation) {
      case "readGroup":
        readGroup(request);
        break;
      default:
        onReceiveUnsupportedMessage("ReadGroupActor");
    }
  }
  /**
   * This method will read group in cassandra based on group id.
   *
   * @param actorMessage
   */
  private void readGroup(Request actorMessage) throws BaseException {
    CacheUtil cacheUtil = new CacheUtil();
    GroupService groupService = new GroupServiceImpl();
    MemberService memberService = new MemberServiceImpl();
    String groupId = (String) actorMessage.getRequest().get(JsonKey.GROUP_ID);
    List<String> requestFields = (List<String>) actorMessage.getRequest().get(JsonKey.FIELDS);
    logger.info(actorMessage.getContext(),MessageFormat.format("Reading group with groupId {0} and required fields {1}", groupId, requestFields));
    GroupResponse groupResponse;
    try {
      String groupInfo = cacheUtil.getCache(groupId,actorMessage.getContext());
      if (StringUtils.isNotEmpty(groupInfo)) {
        groupResponse = JsonUtils.deserialize(groupInfo, GroupResponse.class);
      } else {
        groupResponse = readGroupWithActivities(actorMessage, cacheUtil, groupService, groupId);
      }
      if (CollectionUtils.isNotEmpty(requestFields) && requestFields.contains(JsonKey.MEMBERS)) {
        String groupMember = cacheUtil.getCache(constructRedisIdentifier(groupId),actorMessage.getContext());
        List<MemberResponse> memberResponses = new ArrayList<>();
        if (StringUtils.isNotEmpty(groupMember)) {
          memberResponses =
                  JsonUtils.deserialize(groupMember, new TypeReference<List<MemberResponse>>() {
                  });
        } else {
          logger.info(actorMessage.getContext(),MessageFormat.format(
                  "read group member cache is empty. Fetching details from DB for groupId - {0} ",
                  groupId));
          memberResponses = memberService.readGroupMembers(groupId, actorMessage.getContext());
          cacheUtil.setCache(
                  constructRedisIdentifier(groupId),
                  JsonUtils.serialize(memberResponses),
                  CacheUtil.groupTtl);
        }
        groupResponse.setMembers(memberResponses);
      }
      if (CollectionUtils.isNotEmpty(requestFields) && !requestFields.contains(JsonKey.ACTIVITIES)) {
        groupResponse.setActivities(null);
      }
      Response response = new Response(ResponseCode.OK.getCode());
      Map<String, Object> map = JsonUtils.convert(groupResponse, Map.class);
      response.putAll(map);
      sender().tell(response, self());
    } catch (Exception ex){
      logger.info(actorMessage.getContext(),MessageFormat.format("ReadGroupActor: Request: {0}",actorMessage.getRequest()));

      logger.error(actorMessage.getContext(),MessageFormat.format("ReadGroupActor: Error Code: {0}, Error Msg: {1} ",ResponseCode.GS_RED03.getErrorCode(),ex.getMessage()),ex);
      ExceptionHandler.handleExceptions(actorMessage, ex, ResponseCode.GS_RED03);
    }
  }

  private GroupResponse readGroupWithActivities(Request actorMessage, CacheUtil cacheUtil, GroupService groupService, String groupId) throws Exception {
    try {
      GroupResponse groupResponse;
      logger.info(actorMessage.getContext(),MessageFormat.format("read group cache is empty. Fetching details from DB for groupId - {0} ", groupId));
      groupResponse = groupService.readGroupWithActivities(groupId, actorMessage.getContext());
      cacheUtil.setCache(groupId, JsonUtils.serialize(groupResponse), CacheUtil.groupTtl);
      return groupResponse;
    }catch (BaseException ex){
      throw new BaseException(ResponseCode.GS_RED07.getErrorCode(), ResponseCode.GS_RED07.getErrorMessage(),ex.getResponseCode());
    }
  }

  /**
   * constructs redis identifie for group & members info groupId_members
   *
   * @param groupId
   * @return
   */
  private String constructRedisIdentifier(String groupId) {
    return groupId + "_" + JsonKey.MEMBERS;
  }
}
