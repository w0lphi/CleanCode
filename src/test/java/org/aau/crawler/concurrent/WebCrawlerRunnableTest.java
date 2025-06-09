package org.aau.crawler.concurrent;

import org.aau.config.DomainFilter;
import org.aau.config.WebCrawlerConfiguration;
import org.aau.crawler.analyzer.PageAnalyzer;
import org.aau.crawler.client.WebCrawlerClient;
import org.aau.crawler.error.CrawlingError;
import org.aau.crawler.result.BrokenLink;
import org.aau.crawler.result.Link;
import org.aau.crawler.result.WorkingLink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebCrawlerRunnableTest {

    private WebCrawlerSharedState sharedState;
    private WebCrawlerConfiguration config;
    private WebCrawlerClient mockClient;
    private PageAnalyzer mockAnalyzer;
    private WebCrawlerRunnable runnable;

    @BeforeEach
    void setup() {
        BlockingQueue<CrawlTask> queue = new LinkedBlockingQueue<>();
        Set<Link> crawledLinks = new HashSet<>();
        List<CrawlingError> errors = Collections.synchronizedList(new ArrayList<>());
        sharedState = new WebCrawlerSharedState(queue, crawledLinks, new AtomicInteger(), new CountDownLatch(1), errors);

        config = new WebCrawlerConfiguration(
                "http://example.com",
                2,
                1,
                new DomainFilter(Set.of("example.com")),
                "/output"
        );

        mockClient = mock(WebCrawlerClient.class);
        mockAnalyzer = mock(PageAnalyzer.class);

        runnable = new WebCrawlerRunnable(sharedState, config) {
            @Override
            protected WebCrawlerClient createWebCrawlerClient() {
                return mockClient;
            }

            @Override
            protected PageAnalyzer createPageAnalyzer() {
                return mockAnalyzer;
            }
        };
    }

    @Test
    void testShouldCrawlTrue() {
        assertTrue(runnable.shouldCrawl("http://example.com", 1));
    }

    @Test
    void testShouldCrawlFalseDueToDepth() {
        assertFalse(runnable.shouldCrawl("http://example.com", 5));
    }

    @Test
    void testCrawlLinkWorking() throws Exception {
        when(mockClient.isPageAvailable("http://example.com")).thenReturn(true);
        when(mockClient.getPageContent("http://example.com")).thenReturn("<html></html>");

        WorkingLink testLink = new WorkingLink("http://example.com", 0, Set.of("Heading1"), Set.of("http://example.com/sub"));
        when(mockAnalyzer.analyze(any(), anyInt(), any())).thenReturn(testLink);

        runnable.crawlLink("http://example.com", 0);

        assertTrue(sharedState.crawledLinks().contains(testLink));
    }

    @Test
    void testCrawlLinkBroken() {
        when(mockClient.isPageAvailable("http://example.com")).thenReturn(false);

        runnable.crawlLink("http://example.com", 0);

        assertTrue(sharedState.crawledLinks().stream()
                .anyMatch(link -> link instanceof BrokenLink && link.getUrl().equals("http://example.com")));
    }

    @Test
    void testReportSublinksAddsToQueue() throws InterruptedException {
        runnable.reportSublinks(Set.of("http://example.com/page1"), 0);

        CrawlTask task = sharedState.urlQueue().take();
        assertEquals("http://example.com/page1", task.url());
        assertEquals(1, task.depth());
    }

}
