package ru.netology;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {

        String path = "/example?param1=value1&param1=value2&param2=value3";
        Map<String, String> headers = new HashMap<>();
        InputStream body = new ByteArrayInputStream(new byte[0]);
        Request request = new Request("GET", path, headers, body);

        // Проверка метода getQueryParam
        String param1Value = request.getQueryParam("param1");
        String param3Value = request.getQueryParam("param3");

        System.out.println("Значение param1: " + param1Value); // Ожидается "value1", так как это первое значение
        System.out.println("Значение param3 (не существует): " + param3Value); // Ожидается null

        // Проверка метода getQueryParams
        Map<String, List<String>> queryParams = request.getQueryParams();
        System.out.println("Все параметры запроса:");
        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
            System.out.println("Параметр: " + entry.getKey() + ", Значения: " + entry.getValue());
        }

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