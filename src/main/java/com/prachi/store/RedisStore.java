package com.prachi.store;

import com.prachi.model.RedisModel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedisStore {

    private final Map<String, RedisModel> data = new ConcurrentHashMap<>();

    public void set(String key, String value) {

        data.put(
                key,
                new RedisModel(value, -1)
        );
    }

    public void set(String key, String value, long expiryTime){
        data.put(key, new RedisModel(value, expiryTime));
    }

    public String get(String key){

        RedisModel redisModel = data.get(key);

        if (redisModel == null) {
            return null;
        }

        if (redisModel.isExpire()) {
            data.remove(key);
            return null;
        }

        return redisModel.getValue();
    }

    public void delete(String key){
        data.remove(key);
    }

    public boolean exists(String key){

        RedisModel redisModel = data.get(key);
        if (redisModel == null) {
            return false;
        }

        if (redisModel.isExpire()) {

            data.remove(key);

            return false;
        }
        return true;
    }

    public void removeExpiredKeys() {

        data.entrySet().removeIf(entry ->
                entry.getValue().isExpire()
        );
    }
}
