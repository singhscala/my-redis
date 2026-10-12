package com.prachi.model;

import java.util.List;

public class RedisResponse {

    private final Type type;
    private final Object value;

    private RedisResponse(Type type, Object value){
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public static RedisResponse simpleString(String value) {
        return new RedisResponse(Type.SIMPLE_STRING, value);
    }

    public static RedisResponse error(String message) {
        return new RedisResponse(Type.ERROR, message);
    }

    public static RedisResponse integer(long value) {
        return new RedisResponse(Type.INTEGER, value);
    }

    public static RedisResponse bulkString(String value) {
        return new RedisResponse(Type.BULK_STRING, value);
    }

    public static RedisResponse array(List<String> values) {
        return new RedisResponse(Type.ARRAY, values);
    }
}
