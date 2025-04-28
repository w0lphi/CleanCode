package org.aau.crawler;

import org.aau.crawler.result.Link;

import java.util.Set;

public interface WebCrawler {
    void start();
    Set<Link> getCrawledLinks();
}
