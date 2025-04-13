package org.aau.driver;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class CrawlerWebDriver implements AutoCloseable {

    private final WebDriver webDriver;
    public static final String READY_STATE_COMPLETE = "complete";

    public CrawlerWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        WebDriver chromeDriver = new ChromeDriver(options);
        chromeDriver.manage().timeouts()
                .pageLoadTimeout(Duration.ofSeconds(10))
                .implicitlyWait(Duration.ofSeconds(5))
                .scriptTimeout(Duration.ofSeconds(5));

        WebDriverWait waitForDocumentReady = new WebDriverWait(chromeDriver, Duration.ofSeconds(10));
        waitForDocumentReady.until((driver) -> {
            String readyState = (String) ((JavascriptExecutor) driver).executeScript("return document.readyState");
            return READY_STATE_COMPLETE.equals(readyState);
        });

        this.webDriver = chromeDriver;
    }

    public String getPageContent(String url) throws TimeoutException {
        System.out.printf("Loading page content: url=%s \n", url);
        webDriver.get(url);
        return webDriver.getPageSource();
    }

    @Override
    public void close() {
        webDriver.close();
    }
}
