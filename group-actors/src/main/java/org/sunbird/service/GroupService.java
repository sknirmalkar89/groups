package org.sunbird.service;

import java.util.List;
import java.util.Map;
import org.sunbird.common.exception.BaseException;
import org.sunbird.models.Group;
import org.sunbird.models.GroupResponse;
import org.sunbird.models.MemberResponse;
import org.sunbird.common.response.Response;

public interface GroupService {

  String createGroup(Group groupObj, Map<String, Object> reqContext) throws BaseException;

  Map<String, Object> readGroup(String groupId, Map<String, Object> reqContext) throws BaseException;

  GroupResponse readGroupWithActivities(String groupId, Map<String, Object> reqContext)
      throws Exception;

  void readGroupActivities(Map<String, Object> dbResGroup, Map<String, Object> reqContext);

  List<GroupResponse> searchGroup(Map<String, Object> searchFilter, Map<String, Object> reqContext) throws BaseException;

  Response updateGroup(Group groupObj, Map<String, Object> reqContext) throws BaseException;

  List<Map<String, Object>> handleActivityOperations(
      String groupId, Map<String, Object> activityOperationMap, Map<String, Object> reqContext) throws BaseException;

  Response deleteGroup(String groupId, List<MemberResponse> members, Map<String, Object> reqContext) throws BaseException;
}
