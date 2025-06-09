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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
        sharedState = spy(new WebCrawlerSharedState(queue, crawledLinks, new AtomicInteger(), new CountDownLatch(1), errors));

        config = spy(new WebCrawlerConfiguration(
                "http://example.com",
                2,
                1,
                new DomainFilter(Set.of("example.com")),
                "/output"
        ));

        mockClient = mock(WebCrawlerClient.class);
        mockAnalyzer = mock(PageAnalyzer.class);

        runnable = spy(new WebCrawlerRunnable(sharedState, config) {
            @Override
            protected WebCrawlerClient createWebCrawlerClient() {
                return mockClient;
            }

            @Override
            protected PageAnalyzer createPageAnalyzer() {
                return mockAnalyzer;
            }
        });
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

    @Test
    void testRunCompletesWhenQueueEmptyAndNoActiveThreads() throws InterruptedException {
        sharedState.urlQueue().put(new CrawlTask("http://example.com", 0));

        when(mockClient.isPageAvailable("http://example.com")).thenReturn(false);

        Thread thread = new Thread(runnable);
        thread.start();

        boolean completed = sharedState.completionLatch().await(10, TimeUnit.SECONDS);

        assertTrue(completed, "Crawler thread did not complete in time");
        assertTrue(sharedState.urlQueue().isEmpty(), "Queue should be empty");
        assertEquals(0, sharedState.activeThreads().get(), "No active threads should remain");
    }


    @Test
    void runShouldReportException() throws Exception {
        var exception = new Exception("Test exception");
        doThrow(exception).when(mockClient).close();
        doNothing().when(runnable).processCrawlTasks();
        assertDoesNotThrow(() -> runnable.run());

        assertEquals(1, sharedState.crawlingErrors().size());
        assertEquals(exception, sharedState.crawlingErrors().getFirst().cause());
        verify(runnable).reportError(anyString(), eq(exception));
    }

    @Test
    void processCrawlTasksShouldReportInterruptedException() throws InterruptedException {
        var ie = new InterruptedException("Test interruption");
        doThrow(ie).when(sharedState).getNextTask();

        assertDoesNotThrow(() -> runnable.processCrawlTasks());

        assertTrue(Thread.currentThread().isInterrupted());
        assertEquals(1, sharedState.crawlingErrors().size());
        assertEquals(ie, sharedState.crawlingErrors().getFirst().cause());
        verify(runnable).reportError(anyString(), eq(ie));
        verify(sharedState).getNextTask();
    }

    @Test
    void crawlLinkShouldDoNothingIfShouldCrawlReturnsFalse() {
        String url = "http://example.com";
        int depth = 1;
        doReturn(false).when(runnable).shouldCrawl(url, depth);
        runnable.crawlLink(url, depth);

        verify(runnable).shouldCrawl(url, depth);
        verify(mockClient, never()).isPageAvailable(url);
    }

    @Test
    void crawlLinkShouldReportRuntimeExceptionAndAddBrokenLink() {
        String url = "http://example.com";
        var exception = new RuntimeException("Test exception");
        doReturn(true).when(runnable).shouldCrawl(url, 0);
        doReturn(true).when(mockClient).isPageAvailable(url);
        doThrow(exception).when(mockClient).getPageContent(url);

        assertDoesNotThrow(() -> runnable.crawlLink(url, 0));

        assertEquals(1, sharedState.crawlingErrors().size());
        assertEquals(exception, sharedState.crawlingErrors().getFirst().cause());

        assertEquals(1, sharedState.crawledLinks().size());
        Link crawledLink = sharedState.crawledLinks().iterator().next();
        assertEquals(BrokenLink.class, crawledLink.getClass());
        assertEquals(url, crawledLink.getUrl());

        verify(mockClient).getPageContent(url);
        verify(runnable).reportError(anyString(), eq(exception));
    }

    @Test
    void reportSublinksShouldReportInterruptedException() throws InterruptedException {
        String url = "http://example.com";
        var ie = new InterruptedException("Test interruption");
        doThrow(ie).when(sharedState).addTask(any(CrawlTask.class));
        doReturn(false).when(runnable).isAlreadyCrawledUrl(url);

        assertDoesNotThrow(() -> runnable.reportSublinks(Set.of(url), 0));

        assertTrue(Thread.currentThread().isInterrupted());
        assertEquals(1, sharedState.crawlingErrors().size());
        assertEquals(ie, sharedState.crawlingErrors().getFirst().cause());
        verify(runnable).reportError(anyString(), eq(ie));
        verify(sharedState).addTask(any(CrawlTask.class));
    }

    @Test
    void processCrawlTasksShouldContinueAsLongAsThereAreTasksOrActiveThreads() throws InterruptedException {
        doReturn(null).when(sharedState).getNextTask();
        doReturn(true).when(sharedState).hasNoFurtherTasks();
        when(sharedState.hasActiveThreads()).thenReturn(true).thenReturn(false);

        runnable.processCrawlTasks();

        verify(sharedState, times(2)).getNextTask();
        verify(sharedState, times(2)).hasActiveThreads();
        verify(sharedState).hasNoFurtherTasks();
        verify(sharedState).countDownCompletionLatch();
        verifyNoMoreInteractions(sharedState);
    }

    @ParameterizedTest
    @MethodSource("shouldCrawlArguments")
    void testShouldCrawlLink(int depth, boolean isAllowedDomain, boolean isAlreadyCrawledUrl, boolean expectedValue) {
        String url = "http://example.com";
        doReturn(isAllowedDomain).when(config).isAllowedDomain(url);
        doReturn(isAlreadyCrawledUrl).when(runnable).isAlreadyCrawledUrl(url);

        assertEquals(expectedValue, runnable.shouldCrawl(url, depth));
    }

    static Stream<Arguments> shouldCrawlArguments() {
        return Stream.of(
                Arguments.of(3, false, true, false),
                Arguments.of(0, false, true, false),
                Arguments.of(0, true, true, false),
                Arguments.of(0, true, false, true)
        );
    }

}
