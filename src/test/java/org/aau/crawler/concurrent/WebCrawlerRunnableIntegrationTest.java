package org.aau.crawler.concurrent;

import org.aau.config.DomainFilter;
import org.aau.config.WebCrawlerConfiguration;
import org.aau.crawler.analyzer.MockPageAnalyzer;
import org.aau.crawler.analyzer.PageAnalyzer;
import org.aau.crawler.client.MockWebCrawlerClient;
import org.aau.crawler.client.WebCrawlerClient;
import org.aau.crawler.error.CrawlingError;
import org.aau.crawler.result.BrokenLink;
import org.aau.crawler.result.Link;
import org.aau.crawler.result.WorkingLink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class WebCrawlerRunnableIntegrationTest {

    private static final Map<String, String> MOCK_PAGE_CONTENTS = Map.of(
            "https://example.com", "<html><body><h1>Mock Heading 1</h1><a href=\"https://example.com/subpage\">Sub Page</a><a href=\"https://another.com/external\">External Link</a></body></html>",
            "https://example.com/subpage", "<html><body><h2>Sub Page Heading</h2><a href=\"https://example.com/another-subpage\">Another Sub Page</a></body></html>",
            "https://example.com/another-subpage", "<html><body><h3>Deep Page Heading</h3></body></html>",
            "https://example.com/page1", "<html><body><h1>Page1 Heading</h1><a href=\"https://example.com/page1sub1\">Page1 Sublink 1</a></body></html>",
            "https://example.com/page1sub1", "<html><body><h2>Page1 Sublink 1 Heading</h2></body></html>",
            "https://broken.com", "<html><body>This content doesn't matter for broken links</body></html>"
    );

    private static final Map<String, Boolean> MOCK_PAGE_AVAILABILITY = Map.of(
            "https://example.com", true,
            "https://example.com/subpage", true,
            "https://example.com/another-subpage", true,
            "https://another.com/external", true,
            "https://broken.com", false,
            "https://example.com/page1", true,
            "https://example.com/page1sub1", true
    );

    private static final Map<String, Set<String>> MOCK_EXTRACTED_LINKS = Map.of(
            "https://example.com", Set.of("https://example.com/subpage", "https://another.com/external"),
            "https://example.com/subpage", Set.of("https://example.com/another-subpage"),
            "https://example.com/another-subpage", Collections.emptySet(),
            "https://example.com/page1", Set.of("https://example.com/page1sub1"),
            "https://example.com/page1sub1", Collections.emptySet(),
            "https://broken.com", Collections.emptySet()
    );

    private static final Map<String, Set<String>> MOCK_EXTRACTED_HEADINGS = Map.of(
            "https://example.com", Set.of("h1 Mock Heading 1"),
            "https://example.com/subpage", Set.of("h2 Sub Page Heading"),
            "https://example.com/another-subpage", Set.of("h3 Deep Page Heading"),
            "https://example.com/page1", Set.of("h1 Page1 Heading"),
            "https://example.com/page1sub1", Set.of("h2 Page1 Sublink 1 Heading"),
            "https://broken.com", Collections.emptySet()
    );

    private WebCrawlerSharedState sharedState;
    private WebCrawlerConfiguration configuration;
    private WebCrawlerClient mockWebCrawlerClient;
    private PageAnalyzer mockPageAnalyzer;

    @BeforeEach
    void setUp() {
        LinkedBlockingQueue<CrawlTask> taskQueue = new LinkedBlockingQueue<>();
        Set<Link> crawledLinks = Collections.synchronizedSet(new HashSet<>());
        AtomicInteger activeThreads = new AtomicInteger(0);
        CountDownLatch completionLatch = new CountDownLatch(1);
        List<CrawlingError> crawlingErrors = Collections.synchronizedList(new ArrayList<>());

        sharedState = new WebCrawlerSharedState(taskQueue, crawledLinks, activeThreads, completionLatch, crawlingErrors);

        configuration = new WebCrawlerConfiguration(
                "https://example.com",
                2,
                1,
                new DomainFilter(Set.of("example.com")),
                "/output"
        );

        mockWebCrawlerClient = new MockWebCrawlerClient(MOCK_PAGE_CONTENTS, MOCK_PAGE_AVAILABILITY);
        mockPageAnalyzer = new MockPageAnalyzer(MOCK_EXTRACTED_LINKS, MOCK_EXTRACTED_HEADINGS);
    }

    private WebCrawlerRunnable createWebCrawlerRunnableWithMocks() {
        return new WebCrawlerRunnable(sharedState, configuration) {
            @Override
            protected WebCrawlerClient createWebCrawlerClient() {
                return mockWebCrawlerClient;
            }

            @Override
            protected PageAnalyzer createPageAnalyzer() {
                return mockPageAnalyzer;
            }
        };
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void shouldCrawlSinglePageAndCollectLinksAndHeadings() throws InterruptedException {
        sharedState.addTask(new CrawlTask("https://example.com", 0));

        Thread crawlerThread = new Thread(createWebCrawlerRunnableWithMocks());
        crawlerThread.start();

        //boolean completed = sharedState.completionLatch().await();
        //assertTrue(completed);

        assertFalse(sharedState.crawledLinks().isEmpty());

        assertTrue(sharedState.crawledLinks().stream().anyMatch(link -> link.getUrl().equals("https://example.com")));
        assertTrue(sharedState.crawledLinks().stream().anyMatch(link -> link.getUrl().equals("https://example.com/subpage")));
        assertTrue(sharedState.crawledLinks().stream().anyMatch(link -> link.getUrl().equals("https://example.com/another-subpage")));

        Optional<Link> exampleLink = sharedState.crawledLinks().stream()
                .filter(link -> link.getUrl().equals("https://example.com"))
                .findFirst();

        assertTrue(exampleLink.isPresent());
        assertTrue(exampleLink.get() instanceof WorkingLink);
        WorkingLink workingExampleLink = (WorkingLink) exampleLink.get();
        assertTrue(workingExampleLink.getHeadings().contains("h1 Mock Heading 1"));

        assertFalse(sharedState.crawledLinks().stream().anyMatch(link -> link.getUrl().equals("https://another.com/external")));

        assertTrue(sharedState.urlQueue().isEmpty());
        assertFalse(sharedState.hasActiveThreads());
        assertEquals(0, sharedState.completionLatch().getCount());
        assertTrue(sharedState.crawlingErrors().isEmpty());
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void shouldCrawlMultiplePagesConcurrently() throws InterruptedException {
        int numberOfThreads = 2;

        sharedState.addTask(new CrawlTask("https://example.com", 0));
        sharedState.addTask(new CrawlTask("https://example.com/page1", 0));

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(createWebCrawlerRunnableWithMocks());
        }

        //boolean completed = sharedState.completionLatch().await();
        //assertTrue(completed);

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertFalse(sharedState.crawledLinks().isEmpty());
        assertTrue(sharedState.containsCrawledUrl("https://example.com"));
        assertTrue(sharedState.containsCrawledUrl("https://example.com/subpage"));
        assertTrue(sharedState.containsCrawledUrl("https://example.com/another-subpage"));
        assertTrue(sharedState.containsCrawledUrl("https://example.com/page1"));
        assertTrue(sharedState.containsCrawledUrl("https://example.com/page1sub1"));

        assertEquals(5, sharedState.crawledLinks().size());
        assertTrue(sharedState.urlQueue().isEmpty());
        assertFalse(sharedState.hasActiveThreads());
        assertEquals(0, sharedState.completionLatch().getCount());
        assertTrue(sharedState.crawlingErrors().isEmpty());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void shouldReportBrokenLinksConcurrently() throws InterruptedException {
        int numberOfThreads = 2;

        sharedState.addTask(new CrawlTask("https://example.com", 0));
        sharedState.addTask(new CrawlTask("https://broken.com", 0));

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(createWebCrawlerRunnableWithMocks());
        }

        //boolean completed = sharedState.completionLatch().await();
        //assertTrue(completed);

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        assertTrue(sharedState.containsCrawledUrl("https://example.com"));
        assertTrue(sharedState.containsCrawledUrl("https://broken.com"));

        Optional<Link> brokenLinkOptional = sharedState.crawledLinks().stream()
                .filter(link -> link.getUrl().equals("https://broken.com"))
                .findFirst();

        assertTrue(brokenLinkOptional.isPresent());
        assertTrue(brokenLinkOptional.get() instanceof BrokenLink);

        assertFalse(sharedState.crawlingErrors().isEmpty());
        boolean foundBrokenLinkError = sharedState.crawlingErrors().stream()
                .anyMatch(error -> error.message().contains("Unexpected error while crawling https://broken.com") || error.message().contains("Mocked page not available: https://broken.com"));
        assertTrue(foundBrokenLinkError);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void shouldHandleMaximumDepthCorrectly() throws InterruptedException {
        configuration = new WebCrawlerConfiguration(
                "https://example.com",
                0,
                1,
                new DomainFilter(Set.of("example.com")),
                "/output"
        );

        sharedState = new WebCrawlerSharedState(
                new LinkedBlockingQueue<>(),
                Collections.synchronizedSet(new HashSet<>()),
                new AtomicInteger(0),
                new CountDownLatch(1),
                Collections.synchronizedList(new ArrayList<>() )
        );

        sharedState.addTask(new CrawlTask("https://example.com", 0));

        Thread crawlerThread = new Thread(createWebCrawlerRunnableWithMocks());
        crawlerThread.start();

        //boolean completed = sharedState.completionLatch().await();
        //assertTrue(completed);

        assertEquals(1, sharedState.crawledLinks().size());
        assertTrue(sharedState.containsCrawledUrl("https://example.com"));
        assertFalse(sharedState.containsCrawledUrl("https://example.com/subpage"));
        assertTrue(sharedState.urlQueue().isEmpty());
    }
}