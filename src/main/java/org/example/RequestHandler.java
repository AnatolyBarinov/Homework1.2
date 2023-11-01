package org.example;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestHandler {
    private final List<String> VALID_PATHS;

    public RequestHandler() {
        VALID_PATHS = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    }

    public void handleConnection(BufferedReader in, BufferedOutputStream out) {
        try {
            String requestLine = in.readLine();

            if (requestLine == null || requestLine.isEmpty()) {
                return;
            }

            String[] parts = requestLine.split(" ");
            if (parts.length != 3) {
                return;
            }

            String requestPath = parts[1];
            URL url = new URL(requestPath);
            String pathWithoutQuery = url.getPath();
            Map<String, String> queryParams = getQueryParams(url);

            if (!isValidPath(pathWithoutQuery)) {
                sendErrorResponse(out, 404, "Not Found");
                return;
            }

            Path filePath = Path.of(".", "public", pathWithoutQuery);
            String mimeType = Files.probeContentType(filePath);

            if (pathWithoutQuery.equals("/classic.html")) {
                handleSpecialCase(out, filePath, mimeType, queryParams);
            } else {
                handleRegularCase(out, filePath, mimeType);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isValidPath(String path) {
        return VALID_PATHS.contains(path);
    }

    private void sendErrorResponse(BufferedOutputStream out, int statusCode, String statusMessage) throws IOException {
        String response = "HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        out.write(response.getBytes());
        out.flush();
    }

    private void handleSpecialCase(BufferedOutputStream out, Path filePath, String mimeType, Map<String, String> queryParams) throws IOException {
        String template = Files.readString(filePath);
        String content = template.replace(
                "{time}",
                LocalDateTime.now().toString()
        );

        String lastParam = queryParams.get("last");
        if (lastParam != null) {
            int last = Integer.parseInt(lastParam);
            // Дополнительная логика обработки параметра "last"


            // Пример использования HttpClient для выполнения HTTP-запроса
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://example.com/api?last=" + last))
                    .build();
            //getQueryParams Дополнительная настройка запроса, если нужно


            try {
                // Получение ответа от сервера и его обработка
                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                String apiResponse = response.body();



                // Обновление контента с учетом полученных данных
                content = content.replace("{apiResponse}", apiResponse);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        byte[] contentBytes = content.getBytes();
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Content-Length: " + contentBytes.length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        out.write(response.getBytes());
        out.write(contentBytes);
        out.flush();
    }
    private void handleRegularCase(BufferedOutputStream out, Path filePath, String mimeType) throws IOException {
        long length = Files.size(filePath);
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Content-Length: " + length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        out.write(response.getBytes());
        Files.copy(filePath, out);
        out.flush();
    }

    private Map<String, String> getQueryParams(URL url) throws IOException {
        Map<String, String> queryParams = new HashMap<>();
        String query = url.getQuery();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                String key = URLDecoder.decode(keyValue[0], "UTF-8");
                String value = URLDecoder.decode(keyValue[1], "UTF-8");
                queryParams.put(key, value);
            }
        }
        return queryParams;
    }
}