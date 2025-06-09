package org.aau.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HttpClientUnitTest {

    java.net.http.HttpClient mockHttpClient;

    @BeforeEach
    void setup() {
        mockHttpClient = mock(java.net.http.HttpClient.class);
    }


    @Test
    void isPageAvailableShouldReturnFalseOnException() throws IOException, InterruptedException {

        HttpClientImpl client = new HttpClientImpl() {
            @Override
            protected HttpClient createHttpClient() {
                return mockHttpClient;
            }
        };

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Simulated network error"));
        boolean result = client.isPageAvailable("http://example.com");
        assertFalse(result);
        verify(mockHttpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

}
