package org.aau.crawler.client;

import org.aau.http.HttpClient;
import org.aau.http.HttpClientImpl;
import org.aau.web.WebDriver;
import org.aau.web.WebDriverImpl;

public class WebCrawlerClientImpl implements WebCrawlerClient {

    final WebDriver webDriver;
    final HttpClient httpClient;

    public WebCrawlerClientImpl(WebDriver webDriver, HttpClient httpClient) {
        this.webDriver = webDriver;
        this.httpClient = httpClient;
    }

    public WebCrawlerClientImpl() {
        this(createDefaultWebDriver(), createDefaultHttpClient());
    }

    public WebCrawlerClientImpl(HttpClient httpClient) {
        this(createDefaultWebDriver(), httpClient);
    }

    private static WebDriver createDefaultWebDriver() {
        return new WebDriverImpl();
    }

    private static HttpClient createDefaultHttpClient() {
        return new HttpClientImpl();
    }

    @Override
    public boolean isPageAvailable(String url) {
        return httpClient.isPageAvailable(url);
    }

    @Override
    public String getPageContent(String url) throws RuntimeException {
        System.out.printf("Loading page content: url=%s \n", url);
        return webDriver.getPageContent(url);
    }

    @Override
    public void close() {
        webDriver.close();
        httpClient.close();
    }
}
