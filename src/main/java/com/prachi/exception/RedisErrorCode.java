package com.prachi.exception;

public enum RedisErrorCode {

    EMPTY_COMMAND("ERR-1001", "Command cannot be empty"),
    UNKNOWN_COMMAND("ERR-1002", "Unknown command"),
    WRONG_NUMBER_OF_ARGUMENTS("ERR-1003", "Wrong number of arguments"),
    INVALID_ARGUMENT("ERR-1004", "Invalid argument"),
    INVALID_TTL("ERR-1005", "TTL must be a valid positive integer"),
    KEY_TYPE_MISMATCH("ERR-1006", "Operation against a key holding the wrong kind of value"),
    INVALID_RESP_REQUEST("ERR-1007", "Invalid RESP request"),
    CONNECTION_ERROR("ERR-1008", "Connection error"),
    INTERNAL_ERROR("ERR-1009", "Internal server error"),
    CLIENT_DISCONNECTED("ERR-1010", "Client disconnected");

    private final String errorCode;
    private final String errorMessage;

    RedisErrorCode(String code, String message) {
        this.errorCode = code;
        this.errorMessage = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
