package org.aau.http;

public interface HttpClient {

    boolean isPageAvailable(String url);

    void close();
}
