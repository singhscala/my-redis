package com.prachi;

import java.util.HashMap;
import java.util.Map;

public class RedisStore {

    private final Map<String, String> data = new HashMap<>();

    public void set(String key, String value){
        data.put(key, value);
    }

    public void get(String key){
        data.get(key);
    }

    public void delete(String key){
        data.remove(key);
    }

    public boolean exist(String key){
        return data.containsKey(key);
    }
}
