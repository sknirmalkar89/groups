package org.sunbird.message;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.util.JsonKey;
public enum ResponseCode {
  unAuthorized(IResponseMessage.Key.UNAUTHORIZED, IResponseMessage.Message.UNAUTHORIZED, JsonKey.FAILED),
  keyCloakDefaultError(
      IResponseMessage.Key.KEY_CLOAK_DEFAULT_ERROR,
      IResponseMessage.Message.KEY_CLOAK_DEFAULT_ERROR,JsonKey.FAILED),
  unableToCommunicateWithActor(
      IResponseMessage.Key.UNABLE_TO_COMMUNICATE_WITH_ACTOR,
      IResponseMessage.Message.UNABLE_TO_COMMUNICATE_WITH_ACTOR,JsonKey.FAILED),
  exceededGroupMaxLimit(
      IResponseMessage.Key.EXCEEDED_GROUP_MAX_LIMIT,
      IResponseMessage.Message.EXCEEDED_GROUP_MAX_LIMIT,JsonKey.FAILED),
  invalidRequestData(
      IResponseMessage.INVALID_REQUESTED_DATA, IResponseMessage.Message.INVALID_REQUESTED_DATA,JsonKey.FAILED),
  dataTypeError(IResponseMessage.DATA_TYPE_ERROR, IResponseMessage.Message.DATA_TYPE_ERROR,JsonKey.FAILED),
  groupNotFound(IResponseMessage.GROUP_NOT_FOUND, IResponseMessage.Message.GROUP_NOT_FOUND,JsonKey.FAILED),
  groupNotActive(IResponseMessage.GROUP_NOT_ACTIVE, IResponseMessage.Message.GROUP_NOT_ACTIVE,JsonKey.FAILED),
  invalidParameterValue(
      IResponseMessage.INVALID_PARAMETER_VALUE, IResponseMessage.Message.INVALID_PARAMETER_VALUE,JsonKey.FAILED),
  serverError(IResponseMessage.SERVER_ERROR, IResponseMessage.SERVER_ERROR,JsonKey.FAILED),
  invalidPropertyError(
      IResponseMessage.INVALID_PROPERTY_ERROR, IResponseMessage.Message.INVALID_PROPERTY_ERROR,JsonKey.FAILED),
  dbInsertionError(IResponseMessage.DB_INSERTION_FAIL, IResponseMessage.Message.DB_INSERTION_FAIL,JsonKey.FAILED),
  dbUpdateError(IResponseMessage.DB_UPDATE_FAIL, IResponseMessage.Message.DB_UPDATE_FAIL,JsonKey.FAILED),
  internalError(IResponseMessage.INTERNAL_ERROR, IResponseMessage.Message.INTERNAL_ERROR,JsonKey.FAILED),
  serviceUnAvailable(
      IResponseMessage.Key.SERVICE_UNAVAILABLE, IResponseMessage.Message.SERVICE_UNAVAILABLE,JsonKey.FAILED),
  GS_CRT01(IResponseMessage.Key.GS_CRT01, IResponseMessage.Message.GS_CRT01,JsonKey.FAILED),
  GS_CRT02(IResponseMessage.Key.GS_CRT02, IResponseMessage.Message.GS_CRT02,JsonKey.FAILED),
  GS_CRT03(IResponseMessage.Key.GS_CRT03, IResponseMessage.Message.GS_CRT03,JsonKey.FAILED),
  GS_CRT04(IResponseMessage.Key.GS_CRT04, IResponseMessage.Message.GS_CRT04,JsonKey.FAILED),
  GS_CRT05(IResponseMessage.Key.GS_CRT05, IResponseMessage.Message.GS_CRT05,JsonKey.FAILED),
  GS_CRT06(IResponseMessage.Key.GS_CRT06, IResponseMessage.Message.GS_CRT06,JsonKey.FAILED),

  GS_UDT01(IResponseMessage.Key.GS_UDT01, IResponseMessage.Message.GS_UDT01,JsonKey.FAILED),
  GS_UDT02(IResponseMessage.Key.GS_UDT02, IResponseMessage.Message.GS_UDT02,JsonKey.FAILED),
  GS_UDT03(IResponseMessage.Key.GS_UDT03, IResponseMessage.Message.GS_UDT03,JsonKey.FAILED),
  GS_UDT04(IResponseMessage.Key.GS_UDT04, IResponseMessage.Message.GS_UDT04,JsonKey.FAILED),
  GS_UDT05(IResponseMessage.Key.GS_UDT05, IResponseMessage.Message.GS_UDT05,JsonKey.FAILED),
  GS_UDT06(IResponseMessage.Key.GS_UDT06, IResponseMessage.Message.GS_UDT06,JsonKey.FAILED),
  GS_UDT07(IResponseMessage.Key.GS_UDT07, IResponseMessage.Message.GS_UDT07,JsonKey.FAILED),
  GS_UDT08(IResponseMessage.Key.GS_UDT08, IResponseMessage.Message.GS_UDT08,JsonKey.FAILED),

  GS_RED01(IResponseMessage.Key.GS_RED01, IResponseMessage.Message.GS_RED01,JsonKey.FAILED),
  GS_RED02(IResponseMessage.Key.GS_RED02, IResponseMessage.Message.GS_RED02,JsonKey.FAILED),
  GS_RED03(IResponseMessage.Key.GS_RED03, IResponseMessage.Message.GS_RED03,JsonKey.FAILED),
  GS_RED04(IResponseMessage.Key.GS_RED04, IResponseMessage.Message.GS_RED04,JsonKey.FAILED),

  GS_LST01(IResponseMessage.Key.GS_LST01, IResponseMessage.Message.GS_LST01,JsonKey.FAILED),
  GS_LST02(IResponseMessage.Key.GS_LST02, IResponseMessage.Message.GS_LST02,JsonKey.FAILED),
  GS_LST03(IResponseMessage.Key.GS_LST03, IResponseMessage.Message.GS_LST03,JsonKey.FAILED),


  GS_DLT01(IResponseMessage.Key.GS_DLT01, IResponseMessage.Message.GS_DLT01,JsonKey.FAILED),
  GS_DLT02(IResponseMessage.Key.GS_DLT02, IResponseMessage.Message.GS_DLT02,JsonKey.FAILED),
  GS_DLT03(IResponseMessage.Key.GS_DLT03, IResponseMessage.Message.GS_DLT03,JsonKey.FAILED),
  GS_DLT04(IResponseMessage.Key.GS_DLT04, IResponseMessage.Message.GS_DLT04,JsonKey.FAILED),
  GS_DLT05(IResponseMessage.Key.GS_DLT05, IResponseMessage.Message.GS_DLT05,JsonKey.FAILED),
  
  GS_MBRSHP_UDT01(IResponseMessage.Key.GS_MBRSHP_UDT01, IResponseMessage.Message.GS_MBRSHP_UDT01,JsonKey.FAILED),
  GS_MBRSHP_UDT02(IResponseMessage.Key.GS_MBRSHP_UDT02, IResponseMessage.Message.GS_MBRSHP_UDT02,JsonKey.FAILED),
  GS_MBRSHP_UDT03(IResponseMessage.Key.GS_MBRSHP_UDT03, IResponseMessage.Message.GS_MBRSHP_UDT03,JsonKey.FAILED),

  OK(200),
  CLIENT_ERROR(400),
  SERVER_ERROR(500),
  RESOURCE_NOT_FOUND(404),
  UNAUTHORIZED(401),
  FORBIDDEN(403),
  REDIRECTION_REQUIRED(302),
  TOO_MANY_REQUESTS(429),
  SERVICE_UNAVAILABLE(503),
  BAD_REQUEST(400);

  private int code;
  /** error code contains String value */
  private String errorCode;
  /** errorMessage contains proper error message. */
  private String errorMessage;

  private String status;

  ResponseCode(int code) {
    this.code = code;
  }

  /**
   * @param errorCode String
   * @param errorMessage String
   */
  ResponseCode(String errorCode, String errorMessage, String status) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.status = status;
  }
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public int getCode() {
    return this.code;
  }

  /**
   * This method will take header response code as int value and it provide matched enum value, if
   * code is not matched or exception occurs then it will provide SERVER_ERROR
   *
   * @param code int
   * @return HeaderResponseCode
   */
  public static ResponseCode getHeaderResponseCode(int code) {
    if (code > 0) {
      try {
        ResponseCode[] arr = ResponseCode.values();
        if (null != arr) {
          for (ResponseCode rc : arr) {
            if (rc.getResponseCode() == code) return rc;
          }
        }
      } catch (Exception e) {
        return ResponseCode.SERVER_ERROR;
      }
    }
    return ResponseCode.SERVER_ERROR;
  }

  /**
   * This method will take header response error code as string value and it provide matched enum value, if
   * error code is not matched or exception occurs then it will provide SERVER_ERROR
   *
   * @param errorCode String
   * @return HeaderResponseCode
   */
  public static ResponseCode getHeaderResponseStatus(String errorCode) {
    if (errorCode!=  null) {
      try {
        ResponseCode[] arr = ResponseCode.values();
        if (null != arr) {
          for (ResponseCode rc : arr) {
            if (rc.getErrorCode() == errorCode) return rc;
          }
        }
      } catch (Exception e) {
        return ResponseCode.SERVER_ERROR;
      }
    }
    return ResponseCode.SERVER_ERROR;
  }

  /**
   * This method will provide ResponseCode enum based on error code
   *
   * @param errorCode
   * @return String
   */
  public static ResponseCode getResponse(String errorCode) {
    if (StringUtils.isBlank(errorCode)) {
      return null;
    } else if (JsonKey.UNAUTHORIZED.equals(errorCode)) {
      return ResponseCode.unAuthorized;
    } else {
      ResponseCode value = null;
      ResponseCode responseCodes[] = ResponseCode.values();
      for (ResponseCode response : responseCodes) {
        if (response.getErrorCode().equals(errorCode)) {
          return response;
        }
      }
      return value;
    }
  }

  public int getResponseCode() {
    return code;
  }

  /** @return */
  public String getErrorCode() {
    return errorCode;
  }

  /** @return */
  public String getErrorMessage() {
    return errorMessage;
  }

  public static ResponseCode getResponseCode(int code) {
    ResponseCode[] codes = ResponseCode.values();
    for (ResponseCode res : codes) {
      if (res.code == code) {
        return res;
      }
    }
    return ResponseCode.RESOURCE_NOT_FOUND;
  }
}
