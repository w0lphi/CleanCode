package org.aau.crawler.client;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class WebCrawlerClientImpl implements WebCrawlerClient {

    final WebDriver webDriver;
    final HttpClient httpClient;
    private static final String READY_STATE_COMPLETE = "complete";
    private static final Duration MAX_PAGE_LOAD_TIME = Duration.ofSeconds(10);

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
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--blink-settings=imagesEnabled=false");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-notifications");
        WebDriver webDriver = new ChromeDriver(options);
        webDriver.manage().timeouts().pageLoadTimeout(MAX_PAGE_LOAD_TIME);
        WebDriverWait waitForDocumentReady = new WebDriverWait(webDriver, MAX_PAGE_LOAD_TIME);
        waitForDocumentReady.until((driver) -> {
            String readyState = (String) ((JavascriptExecutor) driver).executeScript("return document.readyState");
            return READY_STATE_COMPLETE.equals(readyState);
        });
        return webDriver;
    }

    private static HttpClient createDefaultHttpClient() {
        return HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
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
    public String getPageContent(String url) throws TimeoutException, UnhandledAlertException {
        System.out.printf("Loading page content: url=%s \n", url);
        webDriver.get(url);
        return webDriver.getPageSource();
    }

    @Override
    public void close() {
        webDriver.close();
        httpClient.close();
    }
}
