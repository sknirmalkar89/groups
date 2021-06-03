package org.sunbird.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.sunbird.common.util.JsonKey;
import org.sunbird.models.GroupResponse;
import org.sunbird.models.Member;
import org.sunbird.models.MemberResponse;
import org.sunbird.util.helper.PropertiesCache;

public class GroupUtil {

  private static LoggerUtil logger = new LoggerUtil(GroupUtil.class);
  /**
   * Update Group details in the group of a user
   *
   * @param groups
   * @param groupDetailsMap
   */
  public static void updateGroupDetails(
      List<GroupResponse> groups, Map<String, Map<String, Object>> groupDetailsMap) {
    if (!groups.isEmpty()) {
      for (GroupResponse group : groups) {
        Map<String, Object> groupDetails = groupDetailsMap.get(group.getId());
        group.setMemberRole((String) groupDetails.get(JsonKey.ROLE));
        group.setVisited((Boolean) groupDetails.get(JsonKey.VISITED));
      }
    }
  }

  public static String convertTimestampToUTC(long timeInMs) {
    Date date = new Date(timeInMs);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSSZ");
    simpleDateFormat.setLenient(false);
    return simpleDateFormat.format(date);
  }

  public static String convertDateToUTC(Date date) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSSZ");
    simpleDateFormat.setLenient(false);
    return simpleDateFormat.format(date);
  }

  public static Map<SearchServiceUtil, Map<String, String>> groupActivityIdsBySearchUtilClass(
      List<Map<String, Object>> activities, Map<String,Object> reqContext) {
    logger.info(reqContext,"groupActivityIdsBySearchUtilClass");
    Map<SearchServiceUtil, Map<String, String>> idClassTypeMap = new HashMap<>();
    for (Map<String, Object> activity : activities) {
      SearchServiceUtil searchUtil =
          ActivityConfigReader.getServiceUtilClassName((String) activity.get(JsonKey.TYPE));
      if (null != searchUtil) {
        if (idClassTypeMap.containsKey(searchUtil)) {
          Map<String, String> idActivityMap = idClassTypeMap.get(searchUtil);
          idActivityMap.put((String) activity.get(JsonKey.ID), (String) activity.get(JsonKey.TYPE));
        } else {
          Map<String, String> idActivityMap = new HashMap<>();
          idActivityMap.put((String) activity.get(JsonKey.ID), (String) activity.get(JsonKey.TYPE));
          idClassTypeMap.put(searchUtil, idActivityMap);
        }
      }
    }
    return idClassTypeMap;
  }

  public static boolean checkMaxGroupLimit(List<Map<String, Object>> userGroupsList, String userId) {
    int groupCount = 0;
    boolean maxGroupLimitExceed =false;
    if (CollectionUtils.isNotEmpty(userGroupsList)) {
      Map<String, Object> userInfo =
          userGroupsList
              .stream()
              .filter(userMap -> userId.equals((String) userMap.get(JsonKey.USER_ID)))
              .findFirst()
              .orElse(null);
      if (MapUtils.isNotEmpty(userInfo)) {
        groupCount = ((Set<String>) userInfo.get(JsonKey.GROUP_ID)).size();
      }
    }
    int maxGroupLimit =
        Integer.parseInt(PropertiesCache.getInstance().getProperty(JsonKey.MAX_GROUP_LIMIT));
    if (groupCount >= maxGroupLimit) {
      maxGroupLimitExceed = true;
    }
    return maxGroupLimitExceed;
  }

  public static boolean checkMaxActivityLimit(
      Integer totalActivityCount) {
    boolean isExceeded = false;
    int activityLimit =
        Integer.parseInt(PropertiesCache.getInstance().getProperty(JsonKey.MAX_ACTIVITY_LIMIT));
    if (totalActivityCount > activityLimit) {
      isExceeded = true;
    }
    return isExceeded;
  }

  public static boolean checkMaxMemberLimit(
      int totalMemberCount) {
    boolean isExceeded = false;
    int memberLimit =
        Integer.parseInt(
            PropertiesCache.getInstance().getProperty(JsonKey.MAX_GROUP_MEMBERS_LIMIT));
    if (totalMemberCount > memberLimit) {
      isExceeded = true;
    }
    return isExceeded;
  }

  public static int totalMemberCount(Map memberOperationMap, List<MemberResponse> membersInDB) {
    int totalMemberCount = (null != membersInDB ? membersInDB.size() : 0);

    List<Map<String, Object>> memberAddList =
        (List<Map<String, Object>>) memberOperationMap.get(JsonKey.ADD);
    if (CollectionUtils.isNotEmpty(memberAddList)) {
      totalMemberCount += memberAddList.size();
    }

    List<Map<String, Object>> memberRemoveList =
        (List<Map<String, Object>>) memberOperationMap.get(JsonKey.REMOVE);
    if (CollectionUtils.isNotEmpty(memberRemoveList)) {
      totalMemberCount -= memberRemoveList.size();
    }
    return totalMemberCount;
  }

  public static List<String> getMemberIdList(List<Member> member) {
    List<String> members =
        member.stream().map(data -> data.getUserId()).collect(Collectors.toList());
    return members;
  }

  public static List<String> getMemberIdListFromMap(List<Map<String, Object>> member) {
    List<String> members =
        member
            .stream()
            .map(data -> (String) data.get(JsonKey.USER_ID))
            .collect(Collectors.toList());
    return members;
  }
}
