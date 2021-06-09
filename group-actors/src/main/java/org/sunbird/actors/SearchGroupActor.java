package org.sunbird.actors;

import com.fasterxml.jackson.core.type.TypeReference;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.models.GroupResponse;
import org.sunbird.common.request.Request;
import org.sunbird.common.response.Response;
import org.sunbird.service.GroupService;
import org.sunbird.service.GroupServiceImpl;
import org.sunbird.util.CacheUtil;
import org.sunbird.common.util.JsonKey;
import org.sunbird.util.ExceptionHandler;
import org.sunbird.util.JsonUtils;
import org.sunbird.util.LoggerUtil;
import org.sunbird.util.helper.PropertiesCache;

@ActorConfig(
  tasks = {"searchGroup"},
  asyncTasks = {},
  dispatcher = "group-dispatcher"
)
public class SearchGroupActor extends BaseActor {
  private LoggerUtil logger = new LoggerUtil(SearchGroupActor.class);

  @Override
  public void onReceive(Request request) throws Throwable {
    String operation = request.getOperation();
    switch (operation) {
      case "searchGroup":
        searchGroup(request);
        break;
        
      default:
        onReceiveUnsupportedMessage("SearchGroupActor");
        
    }
  }

  /**
   * This method will search group related information.
   *
   * @param request
   */
  private void searchGroup(Request request) throws BaseException {
    boolean isUseridRedisEnabled =
        Boolean.parseBoolean(
            PropertiesCache.getInstance().getConfigValue(JsonKey.ENABLE_USERID_REDIS_CACHE));
    CacheUtil cacheUtil = new CacheUtil();
    GroupService groupService = new GroupServiceImpl();
    Map<String, Object> searchQueryMap = request.getRequest();
    logger.info(request.getContext(),MessageFormat.format("search group with the request/filters {0}", searchQueryMap));
    Map<String, Object> filterMap = (Map<String, Object>) searchQueryMap.get(JsonKey.FILTERS);

    List<GroupResponse> groupDetails = new ArrayList<>();
    String userId = (String) filterMap.get(JsonKey.USER_ID);
    try {
      if (StringUtils.isNotBlank(userId)) {
        boolean getFromDB = true;
        if (isUseridRedisEnabled) {
          String groupList = cacheUtil.getCache(userId,request.getContext());
          if (StringUtils.isNotEmpty(groupList)) {
            try {
              groupDetails =
                      JsonUtils.deserialize(groupList, new TypeReference<List<GroupResponse>>() {
                      });
              getFromDB = false;
            } catch (Exception e) {
              logger.error(request.getContext(),MessageFormat.format("SearchGroupActor: Error in getting group list from Redis: {0}", e.getMessage()));
            }
          }
        }
        if (getFromDB || CollectionUtils.isEmpty(groupDetails)) {
          logger.info(request.getContext(),"/group/list cache is empty. Fetching details from DB");
          groupDetails = groupService.searchGroup(filterMap,request.getContext());
          if (isUseridRedisEnabled) {
            updateGroupDetailInCache(cacheUtil, groupDetails, userId, request.getContext());
          }
        }
      } else {
        String errorMsg ="Bad Request UserId is Mandatory";
        logger.error(request.getContext(),errorMsg);
        throw new BaseException(
                ResponseCode.GS_LST02.getErrorCode(),
                ResponseCode.GS_LST02.getErrorMessage(),
                ResponseCode.BAD_REQUEST.getCode());
      }

      Map<String, Object> result = new HashMap<>();
      if (CollectionUtils.isNotEmpty(groupDetails) && groupDetails.size() > 0) {
        groupDetails.sort(
                ((o1, o2) ->
                        o1.getMemberRole().compareTo(o2.getMemberRole()))); // sort group result by role
      }
      result.put(JsonKey.GROUP, groupDetails);
      Response response = new Response(result, ResponseCode.OK.getCode());
      sender().tell(response, self());
    }catch (Exception ex){
      logger.error(request.getContext(),MessageFormat.format("SearchGroupActor: Error Code: {0}, Error Msg: {1} ",ResponseCode.GS_LST03.getErrorCode(),ex.getMessage()),ex);
      ExceptionHandler.handleExceptions(request, ex, ResponseCode.GS_LST03);
    }
  }

  private void updateGroupDetailInCache(CacheUtil cacheUtil, List<GroupResponse> groupDetails, String userId, Map<String,Object> reqContext) {
    try {
      cacheUtil.setCache(userId, JsonUtils.serialize(groupDetails), CacheUtil.userTtl);
    } catch (Exception e) {
      logger.error(reqContext,MessageFormat.format("SearchGroupActor: Error in saving group list to Redis: {0}", e.getMessage()));
    }
  }
}
