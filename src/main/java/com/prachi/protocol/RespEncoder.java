package com.prachi.protocol;

import com.prachi.model.RedisResponse;

import java.util.List;

public class RespEncoder {

    public String encode(RedisResponse response) {
        if (response == null) {
            return "$-1\r\n";
        }

        Object value = response.getValue();

        return switch (response.getType()) {
            case SIMPLE_STRING ->
                    encodeSimpleString((String) value);

            case ERROR ->
                    encodeError((String) value);

            case INTEGER ->
                    encodeInteger((Long) value);

            case BULK_STRING ->
                    encodeBulkString((String) value);

            case ARRAY ->
                    encodeArray((List<String>) value);
        };
    }

    public String encodeSimpleString(String value) {
        return "+" + value + "\r\n";
    }

    public String encodeError(String message) {
        return "-" + message + "\r\n";
    }

    public String encodeBulkString(String value) {

        if (value == null) {
            return "$-1\r\n";
        }

        return "$" + value.length() + "\r\n"
                + value + "\r\n";
    }

    public String encodeArray(List<String> values) {

        StringBuilder response = new StringBuilder();

        response.append("*")
                .append(values.size())
                .append("\r\n");

        for (String value : values) {
            response.append(encodeBulkString(value));
        }

        return response.toString();
    }

    public String encodeInteger(long value) {
        return ":" + value + "\r\n";
    }
}
