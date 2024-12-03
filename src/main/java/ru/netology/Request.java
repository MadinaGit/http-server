package ru.netology;

import com.sun.jdi.Value;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final InputStream body;
    private final Map<String, List<String>> queryParams;

    public Request(String method, String path, Map<String, String> headers, InputStream body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
        this.queryParams = parseQueryParams(path);
    }

    public String getQueryParam(String name) {
        List<String> values = queryParams.get(name);
        if (values != null && !values.isEmpty()) {
            return values.get(0);
        }
        return null;
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    private Map<String, List<String>> parseQueryParams(String path) {
        Map<String, List<String>> params = new HashMap<>();
        if (path.contains("?")) {
            String query = path.substring(path.indexOf("?") + 1);
            List<NameValuePair> nameValuePairs = URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
            for (NameValuePair pair : nameValuePairs) {
                params.computeIfAbsent(pair.getName(), k -> new ArrayList<>()).add(pair.getValue());
            }
        }
        return params;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public InputStream getBody() {
        return body;
    }
}
