package com.prachi.protocol;

import com.prachi.exception.RedisErrorCode;
import com.prachi.exception.RedisException;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RespParser {

    public String parseBulkString(String input) {

        int firstLineEnd = input.indexOf("\r\n");

        String lengthPart = input.substring(1, firstLineEnd);
        int length = Integer.parseInt(lengthPart);

        int dataStart = firstLineEnd + 2;

        return input.substring(dataStart, dataStart + length);
    }

    public List<String> parseArray(String input) {

        int firstLineEnd = input.indexOf("\r\n");

        int elementCount = Integer.parseInt(input.substring(1, firstLineEnd));

        List<String> result = new ArrayList<>();

        int currentPosition = firstLineEnd + 2;

        for (int i = 0; i < elementCount; i++) {

            int lineEnd = input.indexOf("\r\n", currentPosition);

            int length = Integer.parseInt(
                    input.substring(currentPosition + 1, lineEnd)
            );

            int dataStart = lineEnd + 2;

            String value = input.substring(
                    dataStart,
                    dataStart + length
            );

            result.add(value);

            currentPosition = dataStart + length + 2;
        }

        return result;
    }

    public String parseSimpleString(String input) {

        int lineEnd = input.indexOf("\r\n");

        return input.substring(1, lineEnd);
    }

    public String parseError(String input) {

        int lineEnd = input.indexOf("\r\n");

        return input.substring(1, lineEnd);
    }

    public String parseNull(String input) {

        if (input.equals("$-1\r\n")) {
            return null;
        }

        throw new IllegalArgumentException("Invalid RESP null");
    }


    private String readLine(InputStream inputStream) throws IOException {
        StringBuilder line = new StringBuilder();

        while (true) {
            int currentByte = inputStream.read();

            if (currentByte == -1) {
                throw new EOFException("Incomplete RESP line");
            }

            if (currentByte == '\r') {
                int nextByte = inputStream.read();

                if (nextByte == -1) {
                    throw new EOFException("Incomplete RESP line ending");
                }

                if (nextByte != '\n') {
                    throw new RedisException(
                            RedisErrorCode.INVALID_RESP_REQUEST);
                }

                return line.toString();
            }

            line.append((char) currentByte);
        }
    }

    private String readBytes(InputStream inputStream, int length) throws IOException {

        byte[] buffer = new byte[length];

        int totalRead = 0;

        while (totalRead < length) {

            int bytesRead = inputStream.read(buffer, totalRead, length - totalRead);

            if (bytesRead == -1) {
                throw new EOFException("Incomplete RESP bulk string");
            }

            totalRead += bytesRead;
        }

        return new String(buffer, StandardCharsets.UTF_8);
    }


    public String readBulkString(InputStream inputStream) throws IOException {

        String header = readLine(inputStream);

        if (!header.startsWith("$")) {
            throw new RedisException(RedisErrorCode.INVALID_RESP_REQUEST);
        }

        int length;

        try {
            length = Integer.parseInt(header.substring(1));
        } catch (NumberFormatException e) {
            throw new RedisException(RedisErrorCode.INVALID_RESP_REQUEST);
        }

        if (length == -1) {
            return null;
        }

        if (length < -1) {
            throw new RedisException(RedisErrorCode.INVALID_RESP_REQUEST);
        }

        String value = readBytes(inputStream, length);

        int cr = inputStream.read();
        int lf = inputStream.read();

        if (cr != '\r' || lf != '\n') {
            throw new RedisException(RedisErrorCode.INVALID_RESP_REQUEST);
        }

        return value;
    }



    public List<String> readArray(InputStream inputStream)
            throws IOException {

        String header = readLine(inputStream);

        if (!header.startsWith("*")) {
            throw new RedisException(RedisErrorCode.INVALID_RESP_REQUEST);
        }

        int elementCount;

        try {
            elementCount = Integer.parseInt(header.substring(1));
        } catch (NumberFormatException e) {
            throw new RedisException(RedisErrorCode.INVALID_RESP_REQUEST);
        }

        if (elementCount < 0) {
            throw new RedisException(RedisErrorCode.INVALID_RESP_REQUEST);
        }

        List<String> result = new ArrayList<>();

        for (int i = 0; i < elementCount; i++) {
            String value = readBulkString(inputStream);

            if (value == null) {
                throw new RedisException(RedisErrorCode.INVALID_RESP_REQUEST);
            }

            result.add(value);
        }

        return result;
    }


    public long readInteger(InputStream inputStream) throws IOException {

        String value = readLine(inputStream);

        return Long.parseLong(value);
    }

    public String readResponse(InputStream inputStream) throws IOException {

        int firstByte = inputStream.read();

        if (firstByte == -1) {
            throw new IOException("Connection closed");}

        switch (firstByte) {

            case '+', '-':
                return readLine(inputStream);

            case ':':
                return String.valueOf(readInteger(inputStream));

            case '*':
                int count = Integer.parseInt(readLine(inputStream));

                if (count == -1) {
                    return null;
                }

                List<String> values = new ArrayList<>();

                for (int i = 0; i < count; i++) {
                    values.add(readResponse(inputStream));
                }

                return String.join("\n", values);

            case '$':
                int length = Integer.parseInt(readLine(inputStream));

                if (length == -1) {
                    return null;
                }

                String value = readBytes(inputStream, length);
                readLine(inputStream);
                return value;

            default:
                throw new IOException("Unsupported RESP response");
        }
    }
}
