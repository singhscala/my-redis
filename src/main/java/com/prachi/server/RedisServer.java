package com.prachi.server;

import com.prachi.command.CommandHandler;
import com.prachi.protocol.RespEncoder;
import com.prachi.protocol.RespParser;
import com.prachi.store.RedisStore;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisServer {

    private static final int port = 6379;

    public static void main(String[] args) {
        RedisStore store = new RedisStore();
        CommandHandler commandHandler = new CommandHandler(store);

        ExecutorService executorService =
                Executors.newFixedThreadPool(10);

        Thread expirationThread =
                new Thread(new ExpirationWorker(store));

        expirationThread.setDaemon(true);
        expirationThread.start();

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    System.out.println("Shutting down server...");
                    expirationThread.interrupt();
                    executorService.shutdown();
                    System.out.println("Server shutdown complete.");
                })
        );

        try(ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("My Redis server started on port " + port);

            while(true){
                Socket socket = serverSocket.accept();

                System.out.println("Server is connected "+socket.getInetAddress());

                executorService.submit(()->handleClient(socket, commandHandler));
            }
        }catch (IOException ioException){
            ioException.printStackTrace();
        }finally {
            executorService.shutdown();
        }
    }

    public static void handleClient(Socket socket, CommandHandler commandHandler) {
        try {

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            RespParser respParser = new RespParser();
            RespEncoder respEncoder = new RespEncoder();

            while (true) {

                List<String> args = respParser.readArray(inputStream);
                String response = commandHandler.handle(args);

                String encodedResponse;

                if (args.getFirst().equalsIgnoreCase("GET")) {
                    encodedResponse = respEncoder.encodeBulkString(response);
                } else {
                    encodedResponse = respEncoder.encodeSimpleString(response);
                }

                outputStream.write(encodedResponse.getBytes(StandardCharsets.UTF_8));

                outputStream.flush();
            }
        } catch (IOException e) {
            System.out.println("Client disconnected");

        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
