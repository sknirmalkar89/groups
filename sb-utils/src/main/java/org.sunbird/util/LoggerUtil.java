package org.sunbird.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.logstash.logback.marker.Markers;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.message.ResponseCode;
import org.sunbird.common.request.Request;
import java.util.Map;
import org.sunbird.telemetry.JsonKey;
import org.sunbird.telemetry.util.TelemetryEvents;
import  org.sunbird.telemetry.util.TelemetryWriter;

public class LoggerUtil {

    private Logger logger;

    public LoggerUtil(Class c) {
        logger = LoggerFactory.getLogger(c);
    }

    public void info(Map<String,Object> requestContext, String message) {
        if (null != requestContext) {
            logger.info(Markers.appendEntries(requestContext), message);
        } else {
            logger.info(message);
        }
    }

    public void info(String message) {
        logger.info(message);
    }

    public void error(Map<String,Object> requestContext, String message, Throwable e) {
        if (null != requestContext) {
            logger.error(Markers.appendEntries(requestContext), message, e);
        } else {
            logger.error(message, e);
        }
    }


    public void error(String message, Throwable e) {
        logger.error(message, e);
    }

    public void error(
            Map<String,Object> requestContext,
            String message,
            Throwable e,
            Map<String, Object> telemetryInfo) {
        if (null != requestContext) {
            logger.error(Markers.appendEntries(requestContext), message, e);
        } else {
            logger.error(message, e);
        }
        telemetryProcess(requestContext, telemetryInfo, e);
    }

    public void warn(Map<String,Object> requestContext, String message, Throwable e) {
        if (null != requestContext) {
            logger.warn(Markers.appendEntries(requestContext), message, e);
        } else {
            logger.warn(message, e);
        }
    }

    public void debug(Map<String,Object> requestContext, String message) {
        if (isDebugEnabled(requestContext)) {
            logger.info(Markers.appendEntries(requestContext), message);
        } else {
            logger.debug(message);
        }
    }

    public void debug(String message) {
        logger.debug(message);
    }

    private void telemetryProcess(
            Map<String,Object> requestContext, Map<String, Object> telemetryInfo, Throwable e) {
        BaseException baseException = null;
        if (e instanceof BaseException) {
            baseException = (BaseException) e;
        } else {
            baseException =
                    new BaseException(
                            ResponseCode.internalError.getErrorCode(),
                            ResponseCode.internalError.getErrorMessage(),
                            ResponseCode.SERVER_ERROR.getResponseCode());
        }
        Request request = new Request();
        request.setContext(requestContext);
        telemetryInfo.put(JsonKey.TELEMETRY_EVENT_TYPE, TelemetryEvents.ERROR.getName());

        Map<String, Object> params = (Map<String, Object>) telemetryInfo.get(JsonKey.PARAMS);
        params.put(JsonKey.ERROR, baseException.getCode());
        params.put(JsonKey.STACKTRACE, generateStackTrace(e.getStackTrace()));
        request.setRequest(telemetryInfo);
        TelemetryWriter.write(request);
    }

    private String generateStackTrace(StackTraceElement[] elements) {
        StringBuilder builder = new StringBuilder("");
        for (StackTraceElement element : elements) {
            builder.append(element.toString());
        }
        return builder.toString();
    }
}