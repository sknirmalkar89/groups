package org.sunbird.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.sunbird.dao.GroupDao;
import org.sunbird.dao.impl.GroupDaoImpl;
import org.sunbird.exception.BaseException;
import org.sunbird.models.Group;
import org.sunbird.response.Response;
import org.sunbird.service.GroupService;
import org.sunbird.util.JsonKey;

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

  @Override
  public Response readGroup(String groupId) throws BaseException {
    Response responseObj = groupDao.readGroup(groupId);
    return responseObj;
  }

  /**
   * This method will read group details based on userId and return all group details as success
   * response or throw ProjectCommonException.
   *
   * @param searchFilter .
   * @return dbGroupDetails.
   */
  @Override
  public List<Map<String, Object>> readGroupDetails(Map<String, Object> searchFilter)
      throws BaseException {
    String userId = (String) searchFilter.get(JsonKey.USER_ID);
    List<Map<String, Object>> dbGroupDetails = new ArrayList<>();
    if (null != userId) {

    } else {
      // Todo will be removed later
      dbGroupDetails = getAllGroups();
    }
    return dbGroupDetails;
  }

  // Todo remove later as we will not need
  // list all groups

  private List<Map<String, Object>> getAllGroups() throws BaseException {

    Response response = groupDao.readAllGroups();
    if (null != response && null != response.getResult()) {
      List<Map<String, Object>> dbGroupDetails =
          (List<Map<String, Object>>) response.getResult().get(JsonKey.RESPONSE);
      if (null != dbGroupDetails) {
        return dbGroupDetails;
      }
    }
    return new ArrayList<>();
  }
}
