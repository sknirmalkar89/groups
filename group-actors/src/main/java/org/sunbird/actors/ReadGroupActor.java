package org.sunbird.actors;

import com.fasterxml.jackson.core.type.TypeReference;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.exception.DBException;
import org.sunbird.exception.BaseException;
import org.sunbird.message.ResponseCode;
import org.sunbird.models.GroupResponse;
import org.sunbird.models.MemberResponse;
import org.sunbird.request.Request;
import org.sunbird.response.Response;
import org.sunbird.service.GroupService;
import org.sunbird.service.GroupServiceImpl;
import org.sunbird.service.MemberService;
import org.sunbird.service.MemberServiceImpl;
import org.sunbird.util.CacheUtil;
import org.sunbird.util.JsonKey;
import org.sunbird.util.JsonUtils;

@ActorConfig(
  tasks = {"readGroup"},
  asyncTasks = {},
  dispatcher = "group-dispatcher"
)
public class ReadGroupActor extends BaseActor {

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
    logger.info("Reading group with groupId {} and required fields {}", groupId, requestFields);
    GroupResponse groupResponse;
    try {
      String groupInfo = cacheUtil.getCache(groupId);
      if (StringUtils.isNotEmpty(groupInfo)) {
        groupResponse = JsonUtils.deserialize(groupInfo, GroupResponse.class);
      } else {
        logger.info("read group cache is empty. Fetching details from DB for groupId - {} ", groupId);
        groupResponse = groupService.readGroupWithActivities(groupId, actorMessage.getContext());
        cacheUtil.setCache(groupId, JsonUtils.serialize(groupResponse), CacheUtil.groupTtl);
      }
      if (CollectionUtils.isNotEmpty(requestFields) && requestFields.contains(JsonKey.MEMBERS)) {
        String groupMember = cacheUtil.getCache(constructRedisIdentifier(groupId));
        List<MemberResponse> memberResponses = new ArrayList<>();
        if (StringUtils.isNotEmpty(groupMember)) {
          memberResponses =
                  JsonUtils.deserialize(groupMember, new TypeReference<List<MemberResponse>>() {
                  });
        } else {
          logger.info(
                  "read group member cache is empty. Fetching details from DB for groupId - {} ",
                  groupId);
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
    } catch (BaseException ex){
      logger.error(MessageFormat.format("ReadGroupActor: Error Code: {0}, Error Msg: {1} ",ResponseCode.GS_RED03.getErrorCode(),ex.getMessage()));
      throw  new BaseException(ResponseCode.GS_RED03.getErrorCode(),ResponseCode.GS_RED03.getErrorMessage(),ex.getResponseCode());
    } catch (DBException ex){
      logger.error(MessageFormat.format("ReadGroupActor: Error Code: {0}, Error Msg: {1} ",ResponseCode.GS_RED04.getErrorCode(),ex.getMessage()));
      throw new BaseException(ResponseCode.GS_RED04.getErrorCode(),ResponseCode.GS_RED04.getErrorMessage(),ex.getResponseCode());
    }catch (Exception ex){
      logger.error(MessageFormat.format("ReadGroupActor: Error Code: {0}, Error Msg: {1} ",ResponseCode.GS_RED04.getErrorCode(),ex.getMessage()));
      throw new BaseException(ResponseCode.GS_RED04.getErrorCode(),ResponseCode.GS_RED04.getErrorMessage(),ResponseCode.SERVER_ERROR.getCode());
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
