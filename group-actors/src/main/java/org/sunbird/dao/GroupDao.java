package org.sunbird.dao;

import java.util.List;
import java.util.Map;

import org.sunbird.common.exception.BaseException;
import org.sunbird.models.Group;
import org.sunbird.common.response.Response;

/** This interface will have all methods required for group service api. */
public interface GroupDao {

  /**
   * This method will create group and return groupId as success response or throw BaseException.
   *
   * @param groupObj Group Details.
   * @return group ID.
   */
  String createGroup(Group groupObj, Map<String,Object> reqContext) throws BaseException;

  /**
   * This method will read group based on Id and return response Object as success response or throw
   * BaseException.
   *
   * @param groupId
   * @return responseObj with Group Details.
   */
  Response readGroup(String groupId, Map<String,Object> reqContext) throws BaseException;

  /**
   * This method will read group based on Id and return response Object as success response or throw
   * ProjectCommonException.
   *
   * @param groupIds
   * @return responseObj with Group Details.
   */
  Response readGroups(List<String> groupIds, Map<String,Object> reqContext) throws BaseException;

  /**
   * This method will update group and return success response or throw BaseException.
   *
   * @param groupObj Group Details.
   * @return Response.
   */
  Response updateGroup(Group groupObj, Map<String,Object> reqContext) throws BaseException;

  /**
   * This method will delete group and return success response or throw BaseException.
   *
   * @param groupId Group Id.
   * @return Response.
   */
  Response deleteGroup(String groupId, Map<String,Object> reqContext) throws BaseException;
}
