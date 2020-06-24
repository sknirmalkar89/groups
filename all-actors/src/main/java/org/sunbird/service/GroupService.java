package org.sunbird.service;

import org.sunbird.BaseException;
import org.sunbird.models.Group;

public interface GroupService {

  String createGroup(Group groupObj) throws BaseException;
}
