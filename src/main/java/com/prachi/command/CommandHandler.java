package com.prachi.command;

import com.prachi.store.RedisStore;

import java.util.List;

public class CommandHandler {

    private final RedisStore redisStore;

    public CommandHandler(RedisStore redisStore){
        this.redisStore = redisStore;
    }

    public String handle(List<String> args) {

        if (args.isEmpty()) {
            return "ERR empty command";
        }

        String[] parts = args.toArray(new String[0]);
        String operation = parts[0].toUpperCase();

        return switch (operation) {
            case "SET" -> handleSet(parts);
            case "GET" -> handleGet(parts);
            case "DEL" -> handleDelete(parts);
            case "EXISTS" -> handleExists(parts);
            default -> "ERR unknown command";
        };
    }

    private String handleSet(String[] parts) {

        if (parts.length == 3) {

            String key = parts[1];
            String value = parts[2];

            redisStore.set(key, value);

            return "OK";
        }

        if (parts.length == 5
                && parts[3].equalsIgnoreCase("EX")) {

            String key = parts[1];
            String value = parts[2];

            try {

                long seconds = Long.parseLong(parts[4]);

                if (seconds <= 0) {
                    return "ERR invalid expiration";
                }

                long expiryTime =
                        System.currentTimeMillis()
                                + seconds * 1000;

                redisStore.set(
                        key,
                        value,
                        expiryTime
                );

                return "OK";

            } catch (NumberFormatException e) {

                return "ERR invalid expiration";
            }
        }

        return "ERR wrong number of arguments";
    }

    public String handleGet(String[] parts){
        if(parts.length!=2){
            return "ERR wrong number of arguments";
        }

        String key = parts[1];

        String value = redisStore.get(key);

        if (value == null) {
            return null;
        }

        return value;
    }

    public String handleDelete(String[] parts){
        if(parts.length!=2){
            return "ERR wrong number of arguments";
        }

        String key = parts[1];

        if (!redisStore.exists(key)) {
            return "0";
        }

        redisStore.delete(key);
        return "1";
    }

    public String handleExists(String[] parts){
        if(parts.length!=2){
            return "ERR wrong number of arguments";
        }

        String key = parts[1];

        return redisStore.exists(key) ? "1" : "0";
    }
}
