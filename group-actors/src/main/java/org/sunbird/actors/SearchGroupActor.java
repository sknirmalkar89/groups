package org.sunbird.actors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.exception.BaseException;
import org.sunbird.message.ResponseCode;
import org.sunbird.request.Request;
import org.sunbird.response.Response;
import org.sunbird.service.GroupService;
import org.sunbird.service.impl.GroupServiceImpl;
import org.sunbird.util.JsonKey;

@ActorConfig(
  tasks = {"searchGroup"},
  asyncTasks = {}
)
public class SearchGroupActor extends org.sunbird.actors.BaseActor {
  private GroupService groupService = GroupServiceImpl.getInstance();

  @Override
  public void onReceive(Request request) throws Throwable {
    String operation = request.getOperation();
    switch (operation) {
      case "searchGroup": // create Group
        searchGroup(request);
        break;

      default:
        onReceiveUnsupportedMessage("SearchGroupActor");
    }
  }

  /**
   * This method will create group in cassandra.
   *
   * @param actorMessage
   */
  private void searchGroup(Request request) throws BaseException {
    logger.info("SearchGroup method call");
    Response response = new Response();
    Map<String, Object> searchQueryMap = request.getRequest();
    Map<String, Object> filterMap = (Map<String, Object>) searchQueryMap.get(JsonKey.FILTERS);
    List<Map<String, Object>> groupDetails = groupService.readGroupDetails(filterMap);
    Map<String, Object> result = new HashMap<>();
    result.put(JsonKey.GROUP, groupDetails);
    response.putAll(result);
    response.setResponseCode(ResponseCode.OK.getCode());

    sender().tell(response, self());
  }
}
