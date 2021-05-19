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
import validators.GroupSearchRequestValidator;
import validators.IRequestValidator;

public class SearchGroupController extends BaseController {
  @Override
  protected boolean validate(Request request) throws BaseException {
    IRequestValidator requestValidator = new GroupSearchRequestValidator();
    return requestValidator.validate(request);
  }

  public CompletionStage<Result> searchGroup(Http.Request req) {
    Request request = createSBRequest(req, ActorOperations.SEARCH_GROUP.getValue());
    try{
      return handleRequest(request);
    } catch (Exception ex) {
     return CompletableFuture.supplyAsync(() -> StringUtils.EMPTY)
            .thenApply(result -> ResponseHandler.handleFailureResponse(ex, request));
    }

  }
}
