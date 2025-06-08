package org.aau.crawler;

import org.aau.config.WebCrawlerConfiguration;
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

    private final ExecutorService crawlExecutor;
    private final BlockingQueue<CrawlTask> urlQueue;
    private final AtomicInteger activeThreads = new AtomicInteger(0);
    private final CountDownLatch completionLatch;

    public WebCrawlerImpl(WebCrawlerConfiguration configuration) {
        this.configuration = configuration;
        this.crawlExecutor = createExecutorService(configuration.threadCount());
        this.crawledLinks = createSynchronizedLinkSet();
        this.urlQueue = createUrlQueue();
        this.completionLatch = createCompletionLatch();
    }

    protected ExecutorService createExecutorService(int threadCount) {
        return Executors.newFixedThreadPool(threadCount);
    }

    protected Set<Link> createSynchronizedLinkSet() {
        return Collections.synchronizedSet(new HashSet<>());
    }

    protected BlockingQueue<CrawlTask> createUrlQueue() {
        return new LinkedBlockingQueue<>();
    }

    protected CountDownLatch createCompletionLatch() {
        return new CountDownLatch(1);
    }

    @Override
    public void start() {
        try {
            this.urlQueue.put(new CrawlTask(configuration.startUrl(), 0));
            System.out.printf("Starting Crawler with %d threads.%n", configuration.threadCount());
            WebCrawlerSharedState sharedState = new WebCrawlerSharedState(urlQueue, crawledLinks, activeThreads, completionLatch);
            for (int i = 0; i < configuration.threadCount(); i++) {
                crawlExecutor.submit(new WebCrawlerRunnable(sharedState, configuration));
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
