package org.aau.crawler;

import org.aau.crawler.analyzer.PageAnalyzer;
import org.aau.crawler.client.WebCrawlerClient;
import org.aau.crawler.concurrent.CrawlTask;
import org.aau.crawler.concurrent.WebCrawlerRunnable;
import org.aau.crawler.result.Link;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawlerImpl implements WebCrawler {

    private final String startUrl;
    private final int maximumDepth;
    private final int threadCount;
    private final Set<Link> crawledLinks;
    private final WebCrawlerClient webCrawlerClient;
    private final PageAnalyzer analyzer;

    private final ExecutorService executor;
    private final BlockingQueue<CrawlTask> urlQueue;
    private final AtomicInteger activeThreads = new AtomicInteger(0);

    public WebCrawlerImpl(String startUrl, int maximumDepth, int threadCount, WebCrawlerClient webCrawlerClient, PageAnalyzer analyzer) {
        this.startUrl = startUrl;
        this.maximumDepth = maximumDepth;
        this.webCrawlerClient = webCrawlerClient;
        this.analyzer = analyzer;
        this.threadCount = threadCount;
        this.executor = Executors.newFixedThreadPool(threadCount);
        this.crawledLinks = Collections.synchronizedSet(new HashSet<>());
        this.urlQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void start() {
        try (webCrawlerClient) {
            this.urlQueue.put(new CrawlTask(startUrl, 0));

            for (int i = 0; i < threadCount; i++) {
                executor.submit(new WebCrawlerRunnable(urlQueue, crawledLinks, webCrawlerClient, analyzer, maximumDepth, activeThreads));
            }

            //TODO: Improve waiting for finish
            while (true) {
                if (urlQueue.isEmpty() && activeThreads.get() == 0) {
                    Thread.sleep(100);
                    if (urlQueue.isEmpty() && activeThreads.get() == 0) {
                        break;
                    }
                }
                Thread.sleep(500);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            shutdownExecutor();
        }
    }

    @Override
    public Set<Link> getCrawledLinks() {
        return crawledLinks;
    }

    protected void shutdownExecutor() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
