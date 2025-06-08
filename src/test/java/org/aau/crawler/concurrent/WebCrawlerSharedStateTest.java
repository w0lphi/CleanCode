package org.aau.crawler.concurrent;

import org.aau.crawler.result.BrokenLink;
import org.aau.crawler.result.Link;
import org.aau.crawler.result.WorkingLink;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class WebCrawlerSharedStateTest {

    @Test
    void testFieldAccessors() {
        BlockingQueue<CrawlTask> queue = new LinkedBlockingQueue<>();
        Set<Link> crawled = new HashSet<>();
        AtomicInteger active = new AtomicInteger(2);
        CountDownLatch latch = new CountDownLatch(1);

        WebCrawlerSharedState state = new WebCrawlerSharedState(queue, crawled, active, latch);

        assertSame(queue, state.urlQueue());
        assertSame(crawled, state.crawledLinks());
        assertSame(active, state.activeThreads());
        assertSame(latch, state.completionLatch());
    }

    @Test
    void testContainsCrawledUrlTrue() {
        Set<Link> crawled = Set.of(
                new WorkingLink("http://example.com", 0, Set.of(), Set.of()),
                new BrokenLink("http://broken.com", 1)
        );
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                new LinkedBlockingQueue<>(),
                crawled,
                new AtomicInteger(),
                new CountDownLatch(1)
        );

        assertTrue(state.containsCrawledUrl("http://example.com"));
        assertTrue(state.containsCrawledUrl("http://broken.com"));
    }

    @Test
    void testContainsCrawledUrlFalse() {
        Set<Link> crawled = Set.of(
                new WorkingLink("http://example.com", 0, Set.of(), Set.of())
        );
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                new LinkedBlockingQueue<>(),
                crawled,
                new AtomicInteger(),
                new CountDownLatch(1)
        );

        assertFalse(state.containsCrawledUrl("http://unknown.com"));
    }

    @Test
    void testContainsCrawledUrlEmptySet() {
        WebCrawlerSharedState state = new WebCrawlerSharedState(
                new LinkedBlockingQueue<>(),
                Collections.emptySet(),
                new AtomicInteger(),
                new CountDownLatch(1)
        );

        assertFalse(state.containsCrawledUrl("http://anything.com"));
    }
}
