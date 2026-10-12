package com.prachi.store;

import com.prachi.model.RedisModel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedisStore {

    private final Map<String, RedisModel> data = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> map = new ConcurrentHashMap<>();

    public void set(String key, String value) {
        data.put(key, new RedisModel(value, -1));
    }

    public void set(String key, String value, long expiryTime){
        data.put(key, new RedisModel(value, expiryTime));
    }

    public void hset(String key, String field, String value){
        map.computeIfAbsent(
                key,
                k-> new ConcurrentHashMap<>()
        ).put(field, value);
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

    public String hget(String key, String field){
        Map<String, String> hash = map.get(key);

        if(hash==null){
            return null;
        }

        return hash.get(field);
    }

    public Map<String, String> hgetAll(String key){
        Map<String, String> hash = map.get(key);

        if (hash==null){
            return Map.of();
        }

        return new ConcurrentHashMap<>(hash);
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
