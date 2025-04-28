package org.aau.crawler.client;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;

public interface WebCrawlerClient extends AutoCloseable {
    boolean isPageAvailable(String url);
    String getPageContent(String url) throws TimeoutException, UnhandledAlertException;
}
