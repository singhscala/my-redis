package com.prachi.store;

import com.prachi.exception.RedisErrorCode;
import com.prachi.exception.RedisException;
import com.prachi.model.RedisModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedisStore {

    private final Map<String, RedisModel> data = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> map = new ConcurrentHashMap<>();

    private void checkKeyType(String key, boolean expectHash) {
        boolean isString = data.containsKey(key);
        boolean isHash = map.containsKey(key);

        if (expectHash && isString) {
            throw new RedisException(RedisErrorCode.KEY_TYPE_MISMATCH);
        }

        if (!expectHash && isHash) {
            throw new RedisException(RedisErrorCode.KEY_TYPE_MISMATCH);
        }
    }

    public synchronized void set(String key, String value) {
        checkKeyType(key, false);
        data.put(key, new RedisModel(value, -1));
    }

    public synchronized void set(String key, String value, long expiryTime){
        checkKeyType(key, false);
        data.put(key, new RedisModel(value, expiryTime));
    }

    public synchronized int hset(String key, String field, String value) {
        checkKeyType(key, true);
        Map<String, String> hash = map.computeIfAbsent(key, k -> new ConcurrentHashMap<>());

        return hash.put(field, value) == null ? 1 : 0;
    }

    public synchronized String get(String key){
        checkKeyType(key, false);

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

    public synchronized String hget(String key, String field){
        checkKeyType(key, true);
        Map<String, String> hash = map.get(key);

        if(hash==null){
            return null;
        }

        return hash.get(field);
    }

    public synchronized Map<String, String> hgetAll(String key){
        checkKeyType(key, true);
        Map<String, String> hash = map.get(key);

        if (hash==null){
            return Map.of();
        }

        return new ConcurrentHashMap<>(hash);
    }

    public synchronized void delete(String key) {
        data.remove(key);
        map.remove(key);
    }

    public synchronized int hdel(String key, String... fields) {
        checkKeyType(key, true);
        Map<String, String> hash = map.get(key);

        if (hash == null) {
            return 0;
        }

        int deletedCount = 0;

        for (String field : fields) {
            if (hash.remove(field) != null) {
                deletedCount++;
            }
        }

        if (hash.isEmpty()) {
            map.remove(key, hash);
        }

        return deletedCount;
    }

    public synchronized boolean exists(String key) {
        RedisModel redisModel = data.get(key);

        if (redisModel != null) {
            if (redisModel.isExpire()) {
                data.remove(key, redisModel);
            } else {
                return true;
            }
        }

        Map<String, String> hash = map.get(key);

        return hash != null && !hash.isEmpty();
    }

    public synchronized int hexists(String key, String field) {
        checkKeyType(key, true);
        Map<String, String> hash = map.get(key);

        if (hash == null) {
            return 0;
        }

        return hash.containsKey(field) ? 1 : 0;
    }

    public synchronized int hlen(String key) {
        checkKeyType(key, true);
        Map<String, String> hash = map.get(key);

        if (hash == null) {
            return 0;
        }

        return hash.size();
    }

    public synchronized void removeExpiredKeys() {

        data.entrySet().removeIf(entry ->
                entry.getValue().isExpire()
        );
    }

    public synchronized List<String> hkeys(String key) {
        checkKeyType(key, true);
        Map<String, String> hash = map.get(key);

        if (hash == null) {
            return List.of();
        }

        return new ArrayList<>(hash.keySet());
    }

    public synchronized List<String> hvals(String key) {
        checkKeyType(key, true);
        Map<String, String> hash = map.get(key);

        if (hash == null) {
            return List.of();
        }

        return new ArrayList<>(hash.values());
    }

    public synchronized List<String> hmget(String key, String... fields) {
        checkKeyType(key, true);
        Map<String, String> hash = map.get(key);

        List<String> values = new ArrayList<>();

        for (String field : fields) {
            values.add(hash == null ? null : hash.get(field));
        }

        return values;
    }
}
