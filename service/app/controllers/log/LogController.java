package controllers.log;

import controllers.BaseController;
import java.util.concurrent.CompletionStage;
import play.mvc.Result;

/** This controller is responsible to manage the dynamic configuration of Logs */
public class LogController extends BaseController {

  /**
   * This action method is responsible to set the Log Level dynamically using Api.
   *
   * @return
   */
  public CompletionStage<Result> setLogLevel() {
    return handleLogRequest();
  }
}
