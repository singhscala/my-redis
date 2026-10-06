package com.prachi.model;

public class RedisModel {

    private final String value;
    private final long expiryTime;

    public RedisModel(String value, long expiryTime){
        this.value = value;
        this.expiryTime = expiryTime;
    }

    public String getValue(){
        return value;
    }

    public boolean isExpire(){
        return expiryTime != -1
                && System.currentTimeMillis() >= expiryTime;
    }
}
