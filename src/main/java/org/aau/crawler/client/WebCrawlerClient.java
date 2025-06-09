package org.aau.crawler.client;

public interface WebCrawlerClient extends AutoCloseable {
    boolean isPageAvailable(String url);

    String getPageContent(String url) throws RuntimeException;
}
