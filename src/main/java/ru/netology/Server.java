package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private final int port;
    private final List<String> validPaths = List.of(
            "/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html",
            "/classic.html", "/events.html", "/events.js"
    );

    private final ExecutorService executor;
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public Server(int port) {
        this.port = port;
        this.executor = Executors.newFixedThreadPool(64);
    }

    public void start() {
        try (var serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port: " + port);
            while (true) {
                var socket = serverSocket.accept();
                executor.submit(new ConnectionHandler(socket, validPaths));
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server exception: ", e);
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }
}
