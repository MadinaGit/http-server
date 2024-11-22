package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private final Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();

    public void addHandler(String method, String path, Handler handler) {
        handlers.computeIfAbsent(method, k -> new ConcurrentHashMap<>()).put(path, handler);
    }

    public void listen(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Сервер запущен на порту " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                logger.info("Новое соеднение доступно: " + socket.getInetAddress() + ":" + socket.getPort());
                new Thread(() -> handleConnection(socket)).start();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while starting server", e);
            e.printStackTrace();
        }
    }


    private void handleConnection(Socket socket) {
        try (
                InputStream inputStream = socket.getInputStream();
                BufferedOutputStream responseStream = new BufferedOutputStream(socket.getOutputStream())) {
            Request request = parseRequest(inputStream);
            logger.info("Запрос получен: " + request.getMethod() + " " + request.getPath());


            Map<String, Handler> methodHandlers = handlers.get(request.getMethod());
            if (methodHandlers != null) {
                Handler handler = methodHandlers.get(request.getPath());
                if (handler != null) {
                    handler.handle(request, responseStream);
                    logger.info("Ответ отправлен для:  " + request.getMethod() + " " + request.getPath());
                } else {
                    logger.warning("Handler not found for: " + request.getMethod() + " " + request.getPath());
                    responseStream.write("404 Not Found".getBytes());
                }
            } else {
                logger.warning("Method not allowed: " + request.getMethod());
                responseStream.write("405 Method Not Allowed".getBytes());
            }
            responseStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Error while handling connection", e);
            try {
                socket.getOutputStream().write("500 Internal Server Error".getBytes());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } finally {
            try {
                socket.close();
                logger.info("Соеднинение закрыто: " + socket.getInetAddress() + ":" + socket.getPort());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Ошибка закрытия соединения", e);
            }
        }
    }

    protected void sendResponse(OutputStream outputStream, String message, String contentType, int statusCode) throws IOException {
        String statusLine = "HTTP/1.1 " + statusCode + " " + getStatusMessage(statusCode) + "\r\n";
        outputStream.write(statusLine.getBytes());
        outputStream.write(("Content-Type: " + contentType + "\r\n").getBytes());
        outputStream.write(("Content-Length: " + message.length() + "\r\n").getBytes());
        outputStream.write("\r\n".getBytes());
        outputStream.write(message.getBytes());
        outputStream.flush();
    }

    private String getStatusMessage(int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
            case 404:
                return "Not Found";
            case 405:
                return "Method Not Allowed";
            case 500:
                return "Internal Server Error";
            default:
                return "Unknown";
        }
    }

    private Request parseRequest(InputStream inputStream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new Exception("Invalid HTTP request");
        }

        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 2) {
            throw new Exception("Invalid HTTP request line");
        }

        String method = requestParts[0];
        String path = requestParts[1];

        Map<String, String> headers = new ConcurrentHashMap<>();
        String line;
        while (!(line = reader.readLine()).isEmpty()) {
            String[] headerParts = line.split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);
            } else {
                throw new Exception("Invalid header: " + line);
            }
        }

        return new Request(method, path, headers, inputStream);
    }
}