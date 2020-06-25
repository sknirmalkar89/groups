package controllers;

import java.util.concurrent.CompletionStage;
import org.sunbird.BaseException;
import org.sunbird.models.ActorOperations;
import org.sunbird.request.Request;
import play.mvc.Http;
import play.mvc.Result;
import validators.GroupCreateRequestValidator;
import validators.IRequestValidator;

public class GroupController extends BaseController {

  @Override
  protected void validate(Request request, IRequestValidator validator) throws BaseException {
    if (validator != null) {
      validator.validate(request);
    }
  }

  public CompletionStage<Result> createGroup(Http.Request req) {
    IRequestValidator requestValidator = new GroupCreateRequestValidator();
    Request request = createSBRequest(req);
    request.setOperation(ActorOperations.CREATE_GROUP.getValue());
    return handleRequest(request, requestValidator);
  }
}
