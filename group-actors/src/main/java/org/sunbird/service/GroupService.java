package org.sunbird.service;

import org.sunbird.exception.BaseException;
import org.sunbird.models.Group;
import org.sunbird.response.Response;

public interface GroupService {

  String createGroup(Group groupObj) throws BaseException;
  Response readGroup(String groupId) throws BaseException;
}
