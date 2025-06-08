package org.aau.crawler;

import org.aau.config.WebCrawlerConfiguration;
import org.aau.crawler.analyzer.PageAnalyzer;
import org.aau.crawler.client.WebCrawlerClient;
import org.aau.crawler.concurrent.CrawlTask;
import org.aau.crawler.concurrent.WebCrawlerRunnable;
import org.aau.crawler.concurrent.WebCrawlerSharedState;
import org.aau.crawler.result.Link;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawlerImpl implements WebCrawler {

    private final WebCrawlerConfiguration configuration;
    private final Set<Link> crawledLinks;
    private final WebCrawlerClient webCrawlerClient;
    private final PageAnalyzer analyzer;

    private final ExecutorService crawlExecutor;
    private final BlockingQueue<CrawlTask> urlQueue;
    private final AtomicInteger activeThreads = new AtomicInteger(0);
    private final CountDownLatch completionLatch;

    public WebCrawlerImpl(WebCrawlerConfiguration configuration, WebCrawlerClient client, PageAnalyzer analyzer) {
        this.configuration = configuration;
        this.webCrawlerClient = client;
        this.analyzer = analyzer;
        this.crawlExecutor = createExecutorService(configuration.threadCount());
        this.crawledLinks = createSynchronizedLinkSet();
        this.urlQueue = createUrlQueue();
        this.completionLatch = createCompletionLatch();
    }

    private ExecutorService createExecutorService(int threadCount) {
        return Executors.newFixedThreadPool(threadCount);
    }

    private Set<Link> createSynchronizedLinkSet() {
        return Collections.synchronizedSet(new HashSet<>());
    }

    private BlockingQueue<CrawlTask> createUrlQueue() {
        return new LinkedBlockingQueue<>();
    }

    private CountDownLatch createCompletionLatch() {
        return new CountDownLatch(1);
    }

    @Override
    public void start() {
        try (webCrawlerClient) {
            this.urlQueue.put(new CrawlTask(configuration.startUrl(), 0));
            System.out.printf("Starting Crawler with %d threads.%n", configuration.threadCount());
            WebCrawlerSharedState sharedState = new WebCrawlerSharedState(urlQueue, crawledLinks, activeThreads, completionLatch);
            for (int i = 0; i < configuration.threadCount(); i++) {
                crawlExecutor.submit(new WebCrawlerRunnable(sharedState, webCrawlerClient, analyzer, configuration));
            }
            awaitCompletion();
        } catch (InterruptedException e) {
            System.err.printf("Crawler interrupted during start: %s%n", e.getMessage());
            Thread.currentThread().interrupt();
            throw new RuntimeException("Crawler interrupted", e);
        } catch (Exception e) {
            System.err.printf("An unexpected error occurred during crawling: %s%n", e.getMessage());
            throw new RuntimeException("Error during web crawling", e);
        } finally {
            shutdownExecutor();
        }
    }

    @Override
    public Set<Link> getCrawledLinks() {
        return crawledLinks;
    }

    @Override
    public void awaitCompletion() throws InterruptedException {
        completionLatch.await();
    }

    protected void shutdownExecutor() {
        System.out.println("Shutting down crawl executor");
        crawlExecutor.shutdown();
        try {
            if (!crawlExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                crawlExecutor.shutdownNow();
            }
        } catch (InterruptedException ie) {
            System.err.printf("Error while shutting down crawl executor: %s %n", ie.getMessage());
            crawlExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
