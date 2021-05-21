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
import validators.GroupDeleteRequestValidator;
import validators.IRequestValidator;

public class DeleteGroupController extends BaseController {
  @Override
  protected boolean validate(Request request) throws BaseException {
    IRequestValidator requestValidator = new GroupDeleteRequestValidator();
    return requestValidator.validate(request);
  }

  public CompletionStage<Result> deleteGroup(Http.Request req) {
    Request request = createSBRequest(req, ActorOperations.DELETE_GROUP.getValue());
    return handleRequest(request);
  }
}
