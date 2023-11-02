package org.example;

import java.util.Map;

class Request {
    private Map<String, String> queryParams;
    private String requestPath;
    private String httpMethod;
    private String httpVersion;

    public Request(Map<String, String> queryParams, String requestPath, String httpMethod, String httpVersion) {
        this.queryParams = queryParams;
        this.requestPath = requestPath;
        this.httpMethod = httpMethod;
        this.httpVersion = httpVersion;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getQueryParam(String key) {
        return queryParams.get(key);
    }

    public String getRequestPath() {
        return requestPath;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getHttpVersion() {
        return httpVersion;
    }
}
