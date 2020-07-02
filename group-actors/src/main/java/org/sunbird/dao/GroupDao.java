package org.sunbird.dao;

import org.sunbird.exception.BaseException;
import org.sunbird.models.Group;
import org.sunbird.response.Response;

/** This interface will have all methods required for group service api. */
public interface GroupDao {

  /**
   * This method will create group and return groupId as success response or throw
   * ProjectCommonException.
   *
   * @param groupObj Group Details.
   * @return group ID.
   */
  String createGroup(Group groupObj) throws BaseException;

  /**
   * This method will read group based on Id and return response Object as success response or throw
   * ProjectCommonException.
   *
   * @param groupId
   * @return responseObj with Group Details.
   */
  Response readGroup(String groupId) throws BaseException;

}
