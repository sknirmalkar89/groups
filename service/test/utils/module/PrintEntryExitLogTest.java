package utils.module;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.request.Request;
import org.sunbird.common.response.ResponseFactory;
import org.sunbird.common.response.ResponseParams;
import org.sunbird.common.util.JsonKey;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ResponseFactory.class})
@PowerMockIgnore({
  "javax.management.*",
  "javax.net.ssl.*",
  "javax.security.*",
  "jdk.internal.reflect.*"
})
public class PrintEntryExitLogTest {
  @Test
  public void testPrintExitLogOnFailure() {
    try {
      ResponseParams params = new ResponseParams();
      BaseException exception =
          new BaseException(
              ResponseCode.internalError.getErrorCode(),
              ResponseCode.internalError.getErrorMessage(),
              ResponseCode.SERVER_ERROR.getResponseCode());
      ResponseCode code = ResponseCode.getResponse(exception.getCode());
      params.setErr(code.getErrorCode());
      params.setErrmsg(code.getErrorMessage());
      params.setStatus(JsonKey.FAILED);
      params.setMsgid("123-456-789");
      PowerMockito.mockStatic(ResponseFactory.class);
      PowerMockito.when(
              ResponseFactory.createResponseParamObj(
                  Mockito.any(), Mockito.any(), Mockito.anyString()))
          .thenReturn(params);
      Request request = new Request();
      request.getContext().put(JsonKey.METHOD, "POST");
      request.getContext().put(JsonKey.URL, "v1/group/create");
      request.setOperation("createGroup");
      Map<String, Object> requestContext = new HashMap<>();
      requestContext.put(JsonKey.X_REQUEST_ID, "123-456-789");
      request.setContext(requestContext);
      PrintEntryExitLog.printExitLogOnFailure(request, null);
      Assert.assertNotNull(request);
    } catch (Exception e) {
      Assert.assertNull(e);
    }
  }
}
