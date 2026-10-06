package com.prachi.client;

import com.prachi.protocol.RespEncoder;
import com.prachi.protocol.RespParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class RedisClient {

    private static final String HOST = "localhost";
    private static final int PORT = 6379;

    public static void main(String[] args) {

        try (
                Socket socket = new Socket(HOST, PORT);

                BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

                InputStream inputStream = socket.getInputStream();

                OutputStream outputStream = socket.getOutputStream()
        ) {

            RespEncoder respEncoder = new RespEncoder();

            System.out.println("Connected to My Redis Server");

            while (true) {

                System.out.print("redis> ");

                String command = console.readLine();

                if (command == null || command.equalsIgnoreCase("exit")) {
                    break;
                }

                List<String> argsList = Arrays.asList(command.split("\\s+"));

                String request = respEncoder.encodeArray(argsList);

                outputStream.write(request.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                RespParser respParser = new RespParser();

                String response = respParser.readResponse(inputStream);

                System.out.println(response == null ? "(nil)" : response);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}