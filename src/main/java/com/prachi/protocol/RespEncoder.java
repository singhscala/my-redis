package com.prachi.protocol;

import java.util.List;

public class RespEncoder {

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
}
