package controllers;

import java.util.concurrent.CompletionStage;
import org.sunbird.BaseException;
import org.sunbird.request.Request;
import play.mvc.Http;
import play.mvc.Result;
import validators.IRequestValidator;

/** This controller class will responsible to check health of the services. */
public class HealthController extends BaseController {
  // Service name must be "service" for the DevOps monitoring.
  private static final String service = "service";

  @Override
  protected void validate(Request request, IRequestValidator validator) throws BaseException {
    if (validator != null) {
      validator.validate(request);
    }
  }

  /**
   * This action method is responsible for checking Health.
   *
   * @return a CompletableFuture of success response
   */
  public CompletionStage<Result> getHealth() throws BaseException {
    Request req = new Request("health"); // Get API
    return handleRequest(req, null);
  }

  /**
   * This action method is responsible to check service health
   *
   * @return a CompletableFuture of success response
   */
  public CompletionStage<Result> getServiceHealth(String serviceName, Http.Request req)
      throws BaseException {
    Request request = createSBRequest(req);
    request.getContext().put("service", serviceName);
    request.setOperation("health");
    return handleRequest(request, null);
  }
}
