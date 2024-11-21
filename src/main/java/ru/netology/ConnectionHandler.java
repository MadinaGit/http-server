package ru.netology;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionHandler implements Runnable {
    private final Socket socket;
    private final List<String> validPaths;
    private static final Logger logger = Logger.getLogger(ConnectionHandler.class.getName());

    public ConnectionHandler(Socket socket, List<String> validPaths) {
        this.socket = socket;
        this.validPaths = validPaths;
    }

    @Override
    public void run() {
        try (
                var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                var out = new BufferedOutputStream(socket.getOutputStream())
        ) {

            final var requestLine = in.readLine();
            logger.info("Получен запрос:  " + requestLine);
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                sendErrorResponse(out, "400 Bad Request", "Invalid request format");
                return;
            }

            final var path = parts[1];
            if (!validPaths.contains(path)) {
                sendErrorResponse(out, "404 Not Found", "Requested resource not found.");
                return;
            }

            final var filePath = Path.of("01_web\\http-server\\", "public", path);
            System.out.println("Looking for file at: " + filePath.toAbsolutePath());

            if (!Files.exists(filePath)) {
                sendErrorResponse(out, "404 Not Found", "File does not exist: " + filePath);
                return;
            }
            final var mimeType = Files.probeContentType(filePath);
            byte[] content;


            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
            } else {
                content = Files.readAllBytes(filePath);
            }
            sendResponse(out, content, mimeType);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Ошибка соединения: ", e);
            try {
                sendErrorResponse(socket.getOutputStream(), "500 Internal Server Error", "An error occurred while processing the request");
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Не удалось отправить ответ об ошибке: ", ex);
            }
        }
    }

    private void sendErrorResponse(OutputStream out, String status, String message) throws IOException {
        String responseBody = "<html><body><h1>" + status + "</h1><p>" + message + "</p></body></html>";
        out.write((
                "HTTP/1.1 " + status + "\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: " + responseBody.length() + "\r\n" +
                        "Connection: close\r\n\r\n" +
                        responseBody
        ).getBytes());
    }

    private void sendResponse(BufferedOutputStream out, byte[] content, String mimeType) throws IOException {

        out.write((
                "HTTP/1.1 " + "200 OK" + "\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n\r\n").getBytes());
        out.write(content);
        out.flush();
    }
}
