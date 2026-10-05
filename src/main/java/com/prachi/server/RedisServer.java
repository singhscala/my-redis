package com.prachi.server;

import com.prachi.command.CommandHandler;
import com.prachi.store.RedisStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
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

    public static void handleClient(Socket socket, CommandHandler commandHandler){
        try{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

            String command;
            while((command = bufferedReader.readLine())!=null){
                String response = commandHandler.handle(command);
                printWriter.println(response);
            }
        }catch (IOException ioException){
            ioException.printStackTrace();
        }finally {
            try {
                socket.close();
            }catch (IOException ignored){}
        }
    }
}
