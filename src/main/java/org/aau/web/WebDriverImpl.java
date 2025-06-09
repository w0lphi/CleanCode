package org.aau.web;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;


public class WebDriverImpl implements WebDriver {

    private static final String READY_STATE_COMPLETE = "complete";
    private static final Duration MAX_PAGE_LOAD_TIME = Duration.ofSeconds(10);

    private final org.openqa.selenium.WebDriver webDriver;

    public WebDriverImpl() {
        this.webDriver = createDefaultWebDriver();
    }

    static org.openqa.selenium.WebDriver createDefaultWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--blink-settings=imagesEnabled=false");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-notifications");
        org.openqa.selenium.WebDriver webDriver = new ChromeDriver(options);
        webDriver.manage().timeouts().pageLoadTimeout(MAX_PAGE_LOAD_TIME);
        WebDriverWait waitForDocumentReady = new WebDriverWait(webDriver, MAX_PAGE_LOAD_TIME);
        waitForDocumentReady.until((driver) -> {
            String readyState = (String) ((JavascriptExecutor) driver).executeScript("return document.readyState");
            return READY_STATE_COMPLETE.equals(readyState);
        });
        return webDriver;
    }

    @Override
    public String getPageContent(String url) {
        webDriver.get(url);
        return webDriver.getPageSource();
    }

    @Override
    public void close() {
        webDriver.close();
        webDriver.quit();
    }
}
