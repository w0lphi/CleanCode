package org.aau.crawler.concurrent;

import org.aau.crawler.error.CrawlingError;
import org.aau.crawler.result.Link;

import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public record WebCrawlerSharedState(
        BlockingQueue<CrawlTask> urlQueue,
        Set<Link> crawledLinks,
        AtomicInteger activeThreads,
        CountDownLatch completionLatch,
        List<CrawlingError> crawlingErrors) {

    public boolean containsCrawledUrl(String url) {
        synchronized (crawledLinks) {
            return crawledLinks.stream().anyMatch(crawlResult -> crawlResult.getUrl().equals(url));
        }
    }

    public void addCrawledLink(Link link) {
        synchronized (crawledLinks) {
            crawledLinks.add(link);
        }
    }

    public void reportCrawlingError(CrawlingError crawlingError) {
        synchronized (crawlingErrors) {
            crawlingErrors.add(crawlingError);
        }
    }

    public CrawlTask getNextTask() throws InterruptedException {
        return urlQueue.poll(5, TimeUnit.SECONDS);
    }

    public void addTask(CrawlTask task) throws InterruptedException {
        urlQueue.put(task);
    }

    public boolean hasNoFurtherTasks() {
        return urlQueue.isEmpty();
    }

    public void incrementActiveThreads() {
        activeThreads.incrementAndGet();
    }

    public void decrementActiveThreads() {
        activeThreads.decrementAndGet();
    }

    public boolean hasActiveThreads() {
        return activeThreads.get() > 0;
    }

    public void countDownCompletionLatch() {
        completionLatch.countDown();
    }
}
