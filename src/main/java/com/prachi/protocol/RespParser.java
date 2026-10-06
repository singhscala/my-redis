package com.prachi.protocol;

import java.io.IOException;
import java.io.InputStream;
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

        int elementCount =
                Integer.parseInt(input.substring(1, firstLineEnd));

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

        int currentByte;

        while ((currentByte = inputStream.read()) != -1) {

            if (currentByte == '\r') {

                int nextByte = inputStream.read();

                if (nextByte == '\n') {
                    break;
                }
            }

            line.append((char) currentByte);
        }

        return line.toString();
    }

    private String readBytes(
            InputStream inputStream,
            int length
    ) throws IOException {

        byte[] buffer = new byte[length];

        int totalRead = 0;

        while (totalRead < length) {

            int bytesRead =
                    inputStream.read(
                            buffer,
                            totalRead,
                            length - totalRead
                    );

            if (bytesRead == -1) {
                throw new IOException("Unexpected end of stream");
            }

            totalRead += bytesRead;
        }

        return new String(buffer);
    }

    public String readBulkString(InputStream inputStream)
            throws IOException {

        String header = readLine(inputStream);

        if (!header.startsWith("$")) {
            throw new IOException("Expected bulk string");
        }

        int length =
                Integer.parseInt(header.substring(1));

        if (length == -1) {
            return null;
        }

        String value =
                readBytes(inputStream, length);

        readLine(inputStream);

        return value;
    }

    public List<String> readArray(InputStream inputStream)
            throws IOException {

        String header = readLine(inputStream);

        if (!header.startsWith("*")) {
            throw new IOException("Expected RESP array");
        }

        int elementCount =
                Integer.parseInt(header.substring(1));

        List<String> result = new ArrayList<>();

        for (int i = 0; i < elementCount; i++) {

            String value = readBulkString(inputStream);

            result.add(value);
        }

        return result;
    }
}
