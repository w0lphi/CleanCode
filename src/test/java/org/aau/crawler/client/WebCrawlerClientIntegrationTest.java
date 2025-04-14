package org.aau.crawler.client;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(MockServerExtension.class)
public class WebCrawlerClientIntegrationTest {

    MockServerClient client;
    WebCrawlerClient webCrawlerClient;
    String mockServerUrl;

    @BeforeEach
    public void setup(MockServerClient client) {
        this.client = client;
        this.webCrawlerClient = new WebCrawlerClient();
        this.mockServerUrl = "http://localhost:%d".formatted(client.getPort());
    }

    @ParameterizedTest
    @ValueSource(ints = {
            400, 401, 402, 403, 404, 405, 406, 407, 408, 409,
            410, 411, 412, 413, 414, 415, 416, 417, 418, 421,
            422, 423, 424, 425, 426, 428, 429, 431, 451,
            500, 501, 502, 503, 504, 505, 506, 507, 508, 510, 511
    })
    void isPageAvailableShouldReturnFalseForErrorCode(int statusCode){
        String path = "/error-code";
        client.when(
                request()
                        .withPath(path)
                        .withMethod("HEAD")
        ).respond(
                response().withStatusCode(statusCode)
        );
        String url = "%s%s".formatted(mockServerUrl, path);
        assertFalse(webCrawlerClient.isPageAvailable(url));
    }

    @ParameterizedTest
    @ValueSource(ints = {
            200, 201, 202, 203, 204, 205, 206, 207, 208, 226,
            300, 301, 302, 303, 304, 305, 307, 308
    })
    void isPageAvailableShouldReturnTrueForSuccessCode(int statusCode){
        String path = "/success-code";
        client.when(
                request()
                        .withPath(path)
                        .withMethod("HEAD")
        ).respond(
                response().withStatusCode(statusCode)
        );
        String url = "%s%s".formatted(mockServerUrl, path);
        assertTrue(webCrawlerClient.isPageAvailable(url));
    }

    @Test
    void isPageAvailableShouldReturnFalseOnException() throws IOException, InterruptedException {
        HttpClient mockHttpClient = mock(HttpClient.class);
        WebDriver mockWebDriver = mock(WebDriver.class);
        WebCrawlerClient client = new WebCrawlerClient(mockWebDriver, mockHttpClient);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Simulated network error"));
        boolean result = client.isPageAvailable("http://example.com");
        assertFalse(result);
        verify(mockHttpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void getPageContentShouldReturnPageContent(){
        String path = "/test-html";
        String url = "%s%s".formatted(mockServerUrl, path);
        String expectedHtml = "<html><head><title>Test Title</title></head><body>Test</body></html>";
        client.when(
                request()
                        .withPath(path)
                        .withMethod("GET")
        ).respond(
                response()
                        .withStatusCode(200)
                        .withBody(expectedHtml)
        );
        HttpClient mockHttpClient = mock(HttpClient.class);
        WebCrawlerClient client = new WebCrawlerClient(mockHttpClient);
        String result = client.getPageContent(url);
        assertEquals(expectedHtml, result);
    }

    @Test
    void closeShouldCloseWebDriverAndHTTPClient(){
        HttpClient mockHttpClient = mock(HttpClient.class);
        WebDriver mockWebDriver = mock(WebDriver.class);
        WebCrawlerClient client = new WebCrawlerClient(mockWebDriver, mockHttpClient);
        client.close();
        verify(mockHttpClient).close();
        verify(mockWebDriver).close();
    }




}
