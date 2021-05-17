package org.sunbird.exception;

import org.sunbird.message.ResponseCode;

public class DBException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private String code;
    private String message;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    private int responseCode;

    /**
     * This code is for client to identify the error and based on that do the message localization.
     *
     * @return String
     */
    public String getCode() {
        return code;
    }

    /**
     * To set the client code.
     *
     * @param code String
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * message for client in english.
     *
     * @return String
     */
    @Override
    public String getMessage() {
        return message;
    }

    /** @param message String */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * three argument constructor.
     *
     * @param code String
     * @param message String
     *
     */
    public DBException(String code, String message) {
        super();
        this.code = code;
        this.message = message;
        this.responseCode = ResponseCode.SERVER_ERROR.getCode();
    }

    public DBException(String code, String message,int responseCode) {
        super();
        this.code = code;
        this.message = message;
        this.responseCode = responseCode;
    }
}
