package org.sunbird.service;

import org.sunbird.exception.BaseException;
import org.sunbird.models.Group;

public interface GroupService {

  String createGroup(Group groupObj) throws BaseException;
}
