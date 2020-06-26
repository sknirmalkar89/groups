package org.sunbird.dao;

import org.sunbird.exception.BaseException;
import org.sunbird.models.Group;

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
}
