package org.sunbird.actors;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.exception.BaseException;
import org.sunbird.models.Group;
import org.sunbird.models.Member;
import org.sunbird.request.Request;
import org.sunbird.response.Response;
import org.sunbird.service.GroupService;
import org.sunbird.service.MemberService;
import org.sunbird.service.impl.GroupServiceImpl;
import org.sunbird.service.impl.MemberServiceImpl;
import org.sunbird.util.JsonKey;

@ActorConfig(
  tasks = {"createGroup", "updateGroup"},
  asyncTasks = {}
)
public class CreateGroupActor extends BaseActor {

  private GroupService groupService = GroupServiceImpl.getInstance();
  private MemberService memberService = MemberServiceImpl.getInstance();

  @Override
  public void onReceive(Request request) throws Throwable {
    String operation = request.getOperation();
    switch (operation) {
      case "createGroup":
        createGroup(request);
        break;
      case "updateGroup":
        // updateGroup(request);
        break;
      default:
        onReceiveUnsupportedMessage("CreateGroupActor");
    }
  }
  /**
   * This method will create group in cassandra.
   *
   * @param actorMessage
   */
  private void createGroup(Request actorMessage) throws BaseException {
    logger.info("CreateGroup method call");

    Group group = new Group();
    group.setName((String) actorMessage.getRequest().get(JsonKey.GROUP_NAME));
    group.setDescription((String) actorMessage.getRequest().get(JsonKey.GROUP_DESC));
    String groupId = groupService.createGroup(group);

    // adding members to group
    List<Map<String, Object>> memberList =
        (List<Map<String, Object>>) actorMessage.getRequest().get(JsonKey.MEMBERS);
    if (CollectionUtils.isNotEmpty(memberList)) {
      List<Member> members =
          memberList
              .stream()
              .map(data -> getMemberModel(data, groupId))
              .collect(Collectors.toList());
      if (!members.isEmpty()) {
        logger.info("adding members to the group: {} stated", group.getName());
        Response addMemberRes = memberService.addMembers(members);
        logger.info("Adding members to the group ended : {}", addMemberRes.getResult());
      }
    }

    Response response = new Response();
    response.put(JsonKey.GROUP_ID, groupId);
    sender().tell(response, self());
  }

  private Member getMemberModel(Map<String, Object> data, String groupId) {
    Member member = new Member();
    member.setGroupId(groupId);
    member.setRole((String) data.get(JsonKey.ROLE));
    member.setStatus((String) data.get(JsonKey.STATUS));
    member.setUserId((String) data.get(JsonKey.USER_ID));
    member.setCreatedOn(new Timestamp(System.currentTimeMillis()));
    return member;
  }
}
