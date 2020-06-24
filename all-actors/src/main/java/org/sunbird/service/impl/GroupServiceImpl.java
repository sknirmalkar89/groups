package org.sunbird.service.impl;

import java.util.UUID;
import org.sunbird.BaseException;
import org.sunbird.dao.GroupDao;
import org.sunbird.dao.impl.GroupDaoImpl;
import org.sunbird.models.Group;
import org.sunbird.service.GroupService;

public class GroupServiceImpl implements GroupService {

  private static GroupDao groupDao = GroupDaoImpl.getInstance();
  private static GroupService groupService = null;

  public static GroupService getInstance() {
    if (groupService == null) {
      groupService = new GroupServiceImpl();
    }
    return groupService;
  }

  @Override
  public String createGroup(Group groupObj) throws BaseException {
    groupObj.setId(UUID.randomUUID().toString());
    String groupId = groupDao.createGroup(groupObj);
    return groupId;
  }
}
