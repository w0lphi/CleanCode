package org.aau.crawler.concurrent;

import org.aau.crawler.result.BrokenLink;
import org.aau.crawler.result.WorkingLink;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class WebCrawlerSharedStateIntegrationTest {

    @Test
    void sharedStateShouldTrackCrawledLinksAndQueue() throws InterruptedException {
        var queue = new LinkedBlockingQueue<CrawlTask>();
        var crawled = new HashSet<org.aau.crawler.result.Link>();
        crawled.add(new WorkingLink("https://a.com", 0, Set.of(), Set.of()));
        crawled.add(new BrokenLink("https://b.com", 1));

        var state = new WebCrawlerSharedState(queue, Collections.synchronizedSet(crawled), new AtomicInteger(1), new CountDownLatch(1), Collections.synchronizedList(new ArrayList<>()));

        assertTrue(state.containsCrawledUrl("https://a.com"));
        assertTrue(state.containsCrawledUrl("https://b.com"));

        queue.put(new CrawlTask("https://c.com", 1));
        assertEquals("https://c.com", queue.take().url());
    }
}