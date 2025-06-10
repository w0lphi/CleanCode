package org.aau.crawler.client;

import java.util.Map;

public class MockWebCrawlerClient implements WebCrawlerClient {
    private final Map<String, String> pageContents;
    private final Map<String, Boolean> pageAvailability;

    public MockWebCrawlerClient(Map<String, String> pageContents, Map<String, Boolean> pageAvailability) {
        this.pageContents = pageContents;
        this.pageAvailability = pageAvailability;
    }

    @Override
    public boolean isPageAvailable(String url) {
        return pageAvailability.getOrDefault(url, false);
    }

    @Override
    public String getPageContent(String url) throws RuntimeException {
        if (url.contains("broken")) {
            throw new RuntimeException("Mocked page not available: " + url);
        }
        return pageContents.getOrDefault(url, "<html><body>Default mocked content for " + url + "</body></html>");
    }

    @Override
    public void close() {}
}