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
import validators.GroupCreateRequestValidator;
import validators.IRequestValidator;

public class CreateGroupController extends BaseController {

  @Override
  protected boolean validate(Request request) throws BaseException {
    IRequestValidator requestValidator = new GroupCreateRequestValidator();
    return requestValidator.validate(request);
  }

  public CompletionStage<Result> createGroup(Http.Request req) {
    Request request = createSBRequest(req, ActorOperations.CREATE_GROUP.getValue());
    try {
      return handleRequest(request);
     }catch (Exception ex) {
      PrintEntryExitLog.printExitLogOnFailure(
              request,
              new BaseException(
                      ResponseCode.GS_CRT_06.getErrorCode(),
                      ex.getMessage(),
                      ResponseCode.CLIENT_ERROR.getResponseCode()));
      return CompletableFuture.supplyAsync(() -> StringUtils.EMPTY)
              .thenApply(result -> ResponseHandler.handleFailureResponse(ex, request));
    }
  }
}
