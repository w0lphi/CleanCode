package org.aau.crawler.concurrent;

import org.aau.crawler.analyzer.PageAnalyzer;
import org.aau.crawler.client.WebCrawlerClient;
import org.aau.crawler.result.BrokenLink;
import org.aau.crawler.result.Link;
import org.aau.crawler.result.WorkingLink;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawlerRunnable implements Runnable {

    private final BlockingQueue<CrawlTask> urlQueue;
    private final Set<Link> crawledLinks;
    private final WebCrawlerClient webCrawlerClient;
    private final PageAnalyzer analyzer;
    private final int maximumDepth;
    private final AtomicInteger activeThreads;

    public WebCrawlerRunnable(BlockingQueue<CrawlTask> urlQueue, Set<Link> crawledLinks, WebCrawlerClient webCrawlerClient, PageAnalyzer analyzer, int maximumDepth, AtomicInteger activeThreads) {
        this.urlQueue = urlQueue;
        this.crawledLinks = crawledLinks;
        this.webCrawlerClient = webCrawlerClient;
        this.analyzer = analyzer;
        this.maximumDepth = maximumDepth;
        this.activeThreads = activeThreads;
    }

    @Override
    public void run() {
        while (true) {
            CrawlTask task = null;
            try {
                task = urlQueue.poll(5, TimeUnit.SECONDS);
                if (task == null) {
                    if (activeThreads.get() == 0 && urlQueue.isEmpty()) {
                        break;
                    }
                    continue;
                }

                activeThreads.incrementAndGet();
                crawlLink(task.url(), task.depth());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } finally {
                if (task != null) {
                    activeThreads.decrementAndGet();
                }
            }
        }
        System.out.println("Thread " + Thread.currentThread().getName() + " finished.");
    }

    protected void crawlLink(String url, int depth) {
        if (depth > maximumDepth || isAlreadyCrawledUrl(url)) {
            return;
        }

        if (webCrawlerClient.isPageAvailable(url)) {
            try {
                String html = webCrawlerClient.getPageContent(url);
                WorkingLink link = analyzer.analyze(url, depth, html);
                crawledLinks.add(link);
                reportSublinks(link.getSubLinks(), depth);
            } catch (Exception e) {
                crawledLinks.add(new BrokenLink(url, depth));
            }
        } else {
            crawledLinks.add(new BrokenLink(url, depth));
        }
    }

    protected void reportSublinks(Set<String> subLinks, int depth) {
        subLinks.forEach(sub -> {
            try {
                if (depth + 1 <= maximumDepth && !isAlreadyCrawledUrl(sub)) {
                    urlQueue.put(new CrawlTask(sub, depth + 1));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    protected boolean isAlreadyCrawledUrl(String url) {
        return crawledLinks.stream().anyMatch(crawlResult -> crawlResult.getUrl().equals(url));
    }
}
