package controllers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.exception.BaseException;
import org.sunbird.message.ResponseCode;
import org.sunbird.models.ActorOperations;
import org.sunbird.request.Request;
import play.mvc.Http;
import play.mvc.Result;
import utils.module.PrintEntryExitLog;
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
