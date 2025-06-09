package org.aau.http;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpClientImpl implements HttpClient {

    private final java.net.http.HttpClient httpClient;

    public HttpClientImpl() {
        this.httpClient = createHttpClient();
    }

    protected java.net.http.HttpClient createHttpClient() {
        return java.net.http.HttpClient.newBuilder()
                .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public boolean isPageAvailable(String url) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            int statusCode = response.statusCode();
            return statusCode < 400;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void close() {
        httpClient.close();
    }
}
