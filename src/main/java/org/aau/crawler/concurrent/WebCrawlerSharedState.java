package org.aau.crawler.concurrent;

import org.aau.crawler.result.Link;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public record WebCrawlerSharedState(
        BlockingQueue<CrawlTask> urlQueue,
        Set<Link> crawledLinks,
        AtomicInteger activeThreads,
        CountDownLatch completionLatch) {

    public boolean containsCrawledUrl(String url) {
        return crawledLinks.stream().anyMatch(crawlResult -> crawlResult.getUrl().equals(url));
    }

}
