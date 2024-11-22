package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        final var server = new Server();

        server.addHandler("GET", "/messages", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream responseStream) {
                try {
                    String responseMessage = "GET /messages handler executed.";
                    server.sendResponse(responseStream, responseMessage, "text/plain", 200);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        server.addHandler("POST", "/messages", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream responseStream) {
                try {
                    String responseMessage = "POST /messages handler executed.";
                    server.sendResponse(responseStream, responseMessage, "text/plain", 200);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        server.listen(9999);
    }
}