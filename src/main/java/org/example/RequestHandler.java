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

            URI uri = new URI(requestPath);
            URL url;
            if (!uri.isAbsolute()) {
                uri = new URI("http://" + requestPath);
            }
            url = uri.toURL();

            String pathWithoutQuery;
            if (uri.getQuery() != null) {
                String[] queryParts = uri.getPath().split("\\?");
                if (queryParts.length > 0) {
                    pathWithoutQuery = queryParts[0];
                } else {
                    pathWithoutQuery = uri.getPath();
                }
            } else {
                pathWithoutQuery = uri.getPath();
            }

            Map<String, String> queryParams = getQueryParams(url);

            if (!isValidPath(pathWithoutQuery)) {
                sendErrorResponse(out, 404, "Not Found");
                return;
            }

            // Чтение тела запроса
            StringBuilder requestBody = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                requestBody.append(line);
            }

            if (pathWithoutQuery.equals("/classic.html")) {
                handleSpecialCase(out, requestBody.toString(), queryParams);
            } else {
                handleRegularCase(out, pathWithoutQuery);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSpecialCase(BufferedOutputStream out, String requestBody, Map<String, String> queryParams) throws IOException {
        // Обработка тела запроса
        // Парсинг тела запроса в JSON объект
        JSONObject jsonBody = new JSONObject(requestBody);

        // Получение значения конкретного параметра запроса
        String name = jsonBody.getString("name");

        // Пример обработки в зависимости от значения параметра запроса
        String response;
        if (name.equals("John")) {
            response = "Hello, John!";
        } else {
            response = "Hello, " + name + "!";
        }

        // Отправка ответа
        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + response.getBytes().length + "\r\n" +
                "Content-Type: text/plain\r\n" +
                "Connection: close\r\n\r\n" +
                response;
        out.write(httpResponse.getBytes());
        out.flush();
    }

    private void handleRegularCase(BufferedOutputStream out, String requestPath) throws IOException {
        // Обработка запроса без тела
        String response;
        if (requestPath.equals("/index.html")) {
            response = "<html><body><h1>Welcome to the homepage!</h1></body></html>";
        } else {
            response = "Page not found";
        }

        // Отправка ответа
        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + response.getBytes().length + "\r\n" +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n\r\n" +
                response;
        out.write(httpResponse.getBytes());
        out.flush();
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