
package com.prachi.exception;

public class RedisException extends RuntimeException {

    private final RedisErrorCode error;

    public RedisException(RedisErrorCode error) {
        super(error.getErrorMessage());
        this.error = error;
    }

    public RedisErrorCode getError() {
        return error;
    }

    public String getFormattedMessage() {
        return error.getErrorCode() + " " + getMessage();
    }
}