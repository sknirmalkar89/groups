package org.sunbird.util;

import org.sunbird.common.exception.BaseException;
import org.sunbird.common.exception.DBException;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.request.Request;

public class ExceptionHandler {

    public static void handleExceptions(Request request, Exception ex, ResponseCode responseCode){
        if(ex instanceof BaseException){
            throw  new BaseException((BaseException) ex);
        }else{
            throw new BaseException(responseCode.getErrorCode(),responseCode.getErrorMessage(),ResponseCode.SERVER_ERROR.getCode());
        }
    }
}
