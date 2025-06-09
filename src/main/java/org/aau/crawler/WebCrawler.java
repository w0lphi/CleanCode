package org.aau.crawler;

import org.aau.crawler.error.CrawlingError;
import org.aau.crawler.result.Link;

import java.util.List;
import java.util.Set;

public interface WebCrawler {
    void start();

    Set<Link> getCrawledLinks();

    List<CrawlingError> getErrors();

    void awaitCompletion() throws InterruptedException;
}
