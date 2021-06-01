package controllers;

import java.util.concurrent.CompletionStage;
import org.sunbird.common.exception.BaseException;
import org.sunbird.models.ActorOperations;
import org.sunbird.common.request.Request;
import play.mvc.Http;
import play.mvc.Result;
import validators.GroupMembershipUpdateRequestValidator;
import validators.IRequestValidator;

public class UpdateGroupMembershipController extends BaseController {

  @Override
  protected boolean validate(Request request) throws BaseException {
    IRequestValidator requestValidator = new GroupMembershipUpdateRequestValidator();
    return requestValidator.validate(request);
  }

  public CompletionStage<Result> updateGroupMembership(Http.Request req) {
    Request request = createSBRequest(req, ActorOperations.UPDATE_GROUP_MEMBERSHIP.getValue());
    return handleRequest(request);
  }
}
