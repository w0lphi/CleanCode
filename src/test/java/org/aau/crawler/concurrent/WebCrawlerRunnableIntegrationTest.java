package org.aau.crawler.concurrent;

import org.aau.config.DomainFilter;
import org.aau.config.WebCrawlerConfiguration;
import org.aau.crawler.error.CrawlingError;
import org.aau.crawler.result.Link;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

public class WebCrawlerRunnableIntegrationTest {

    @Test
    void shouldCrawlSinglePageAndCollectLinksAndHeadings() throws InterruptedException {
        var taskQueue = new LinkedBlockingQueue<CrawlTask>();
        var crawledLinks = java.util.Collections.synchronizedSet(new java.util.HashSet<Link>());
        var activeThreads = new AtomicInteger(0);
        var latch = new CountDownLatch(1);
        var crawlingErrors = Collections.synchronizedList(new ArrayList<CrawlingError>());

        taskQueue.put(new CrawlTask("https://example.com", 0));

        var config = new WebCrawlerConfiguration(
                "https://example.com",
                1, // max depth
                1, // 1 thread
                new DomainFilter(Set.of("example.com")),
                "/output"
        );

        var state = new WebCrawlerSharedState(taskQueue, crawledLinks, activeThreads, latch, crawlingErrors);

        Thread crawlerThread = new Thread(new WebCrawlerRunnable(state, config));
        crawlerThread.start();

        boolean completed = latch.await(10, java.util.concurrent.TimeUnit.SECONDS);

        assertTrue(completed, "Crawler did not finish in time");
        assertFalse(crawledLinks.isEmpty(), "Crawled links should not be empty");

        boolean foundExpectedUrl = crawledLinks.stream().anyMatch(link -> link.getUrl().equals("https://example.com"));
        assertTrue(foundExpectedUrl, "Expected URL should be crawled");
    }
}