package com.prachi.command;

import com.prachi.exception.RedisErrorCode;
import com.prachi.exception.RedisException;
import com.prachi.model.RedisModel;
import com.prachi.model.RedisResponse;
import com.prachi.store.RedisStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandHandler {

    private final RedisStore redisStore;

    public CommandHandler(RedisStore redisStore){
        this.redisStore = redisStore;
    }

    public RedisResponse handle(List<String> args) {

        try {
            if (args == null || args.isEmpty()) {
                throw new RedisException(RedisErrorCode.EMPTY_COMMAND);
            }

            String[] parts = args.toArray(new String[0]);
            String operation = parts[0].toUpperCase();

            return switch (operation) {
                case "SET" -> handleSet(parts);
                case "GET" -> handleGet(parts);
                case "DEL" -> handleDelete(parts);
                case "EXISTS" -> handleExists(parts);
                case "HSET" -> handleHSet(parts);
                case "HGET" -> handleHGet(parts);
                case "HGETALL" -> handleHGetAll(parts);
                case "HDEL" -> handleHDel(parts);
                case "HEXISTS" -> handleHExists(parts);
                case "HLEN" -> handleHLen(parts);
                case "HKEYS" -> handleHKeys(parts);
                case "HVALS" -> handleHVals(parts);
                case "HMGET" -> handleHMGet(parts);
                case "TYPE" -> handleType(parts);

                default -> throw new RedisException(
                        RedisErrorCode.UNKNOWN_COMMAND);
            };

        } catch (RedisException e) {
            return RedisResponse.error(
                    e.getError().getErrorCode() + " " + e.getMessage()
            );
        }
    }

    private RedisResponse handleSet(String[] parts) {

        if (parts.length == 3) {

            String key = parts[1];
            String value = parts[2];

            redisStore.set(key, value);

            return RedisResponse.simpleString("OK");
        }

        if (parts.length == 5 && parts[3].equalsIgnoreCase("EX")) {

            String key = parts[1];
            String value = parts[2];
            long seconds;

            try {
                seconds = Long.parseLong(parts[4]);
            } catch (NumberFormatException e) {
                throw new RedisException(RedisErrorCode.INVALID_TTL);
            }

            if (seconds <= 0) {
                throw new RedisException(RedisErrorCode.INVALID_TTL);
            }

            long expiryTime = System.currentTimeMillis() + seconds * 1000;

            redisStore.set(key, value, expiryTime);

            return RedisResponse.simpleString("OK");
        }

        throw new RedisException(RedisErrorCode.WRONG_NUMBER_OF_ARGUMENTS);
    }

    public RedisResponse handleGet(String[] parts){
        if (parts.length != 2) {
            throw new RedisException(RedisErrorCode.WRONG_NUMBER_OF_ARGUMENTS);
        }

        String value = redisStore.get(parts[1]);

        return RedisResponse.bulkString(value);
    }

    public RedisResponse handleDelete(String[] parts){
        if (parts.length != 2) {
            throw new RedisException(RedisErrorCode.WRONG_NUMBER_OF_ARGUMENTS);
        }

        String key = parts[1];

        if (!redisStore.exists(key)) {
            return RedisResponse.integer(0);
        }

        redisStore.delete(key);
        return RedisResponse.integer(1);
    }

    public RedisResponse handleExists(String[] parts){
        if (parts.length != 2) {
            throw new RedisException(RedisErrorCode.WRONG_NUMBER_OF_ARGUMENTS);
        }

        String key = parts[1];

        return redisStore.exists(key) ? RedisResponse.integer(1) : RedisResponse.integer(0);
    }

    private RedisResponse handleHSet(String[] parts) {
        if (parts.length != 4) {
            throw new RedisException(RedisErrorCode.WRONG_NUMBER_OF_ARGUMENTS);
        }

        int result = redisStore.hset(parts[1], parts[2], parts[3]);
        return RedisResponse.integer(result);
    }

    private RedisResponse handleHGet(String[] parts) {
        if (parts.length != 3) {
            throw new RedisException(RedisErrorCode.WRONG_NUMBER_OF_ARGUMENTS);
        }

        String key = parts[1];
        String field = parts[2];

        String value = redisStore.hget(key, field);

        return RedisResponse.bulkString(value);
    }

    private RedisResponse handleHGetAll(String[] parts) {
        if (parts.length != 2) {
            throw new RedisException(RedisErrorCode.WRONG_NUMBER_OF_ARGUMENTS);
        }

        List<String> result = new ArrayList<>();

        redisStore.hgetAll(parts[1]).forEach((field, value) -> {
            result.add(field);
            result.add(value);
        });

        return RedisResponse.array(result);
    }

    private RedisResponse handleHDel(String[] parts) {
        if (parts.length < 3) {
            throw new RedisException(RedisErrorCode.WRONG_NUMBER_OF_ARGUMENTS);
        }

        String key = parts[1];

        String[] fields = Arrays.copyOfRange(parts, 2, parts.length);

        int deleted = redisStore.hdel(key, fields);

        return RedisResponse.integer(deleted);
    }

    private RedisResponse handleHExists(String[] parts) {
        if (parts.length != 3) {
            throw new RedisException(RedisErrorCode.WRONG_NUMBER_OF_ARGUMENTS);
        }

        int exists = redisStore.hexists(parts[1], parts[2]);

        return RedisResponse.integer(exists);
    }

    private RedisResponse handleHLen(String[] parts) {
        if (parts.length != 2) {
            throw new RedisException(RedisErrorCode.WRONG_NUMBER_OF_ARGUMENTS);
        }

        int length = redisStore.hlen(parts[1]);

        return RedisResponse.integer(length);
    }

    private RedisResponse handleHKeys(String[] parts) {
        if (parts.length != 2) {
            throw new RedisException(RedisErrorCode.WRONG_NUMBER_OF_ARGUMENTS);
        }

        List<String> fields = redisStore.hkeys(parts[1]);

        return RedisResponse.array(fields);
    }

    private RedisResponse handleHVals(String[] parts) {
        if (parts.length != 2) {
            throw new RedisException(RedisErrorCode.WRONG_NUMBER_OF_ARGUMENTS);
        }

        List<String> values = redisStore.hvals(parts[1]);

        return RedisResponse.array(values);
    }

    private RedisResponse handleHMGet(String[] parts) {
        if (parts.length < 3) {
            throw new RedisException(RedisErrorCode.WRONG_NUMBER_OF_ARGUMENTS);
        }

        String key = parts[1];

        String[] fields = Arrays.copyOfRange(parts, 2, parts.length);

        List<String> values = redisStore.hmget(key, fields);

        return RedisResponse.array(values);
    }

    private RedisResponse handleType(String[] parts){
        if (parts.length != 2) {
            throw new RedisException(RedisErrorCode.WRONG_NUMBER_OF_ARGUMENTS);
        }
        return RedisResponse.bulkString(redisStore.type(parts[1]));
    }
}
