package controllers;

import java.util.concurrent.CompletionStage;
import org.sunbird.models.ActorOperations;
import org.sunbird.request.Request;
import play.mvc.Http;
import play.mvc.Result;

public class GroupController extends BaseController {

  @Override
  protected boolean validate(Request request) {
    return true;
  }

  public CompletionStage<Result> createGroup(Http.Request req) {
    Request request = createSBRequest(req);
    request.setOperation(ActorOperations.CREATE_GROUP.getValue());
    return handleRequest(request);
  }
}
