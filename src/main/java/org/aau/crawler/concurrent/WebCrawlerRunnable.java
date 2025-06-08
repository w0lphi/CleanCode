package org.aau.crawler.concurrent;

import org.aau.config.WebCrawlerConfiguration;
import org.aau.crawler.analyzer.PageAnalyzer;
import org.aau.crawler.client.WebCrawlerClient;
import org.aau.crawler.result.BrokenLink;
import org.aau.crawler.result.WorkingLink;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class WebCrawlerRunnable implements Runnable {

    private final WebCrawlerSharedState sharedState;
    private final WebCrawlerClient webCrawlerClient;
    private final PageAnalyzer analyzer;
    private final WebCrawlerConfiguration configuration;

    public WebCrawlerRunnable(WebCrawlerSharedState sharedState,
                              WebCrawlerClient webCrawlerClient,
                              PageAnalyzer analyzer,
                              WebCrawlerConfiguration configuration) {
        this.sharedState = sharedState;
        this.webCrawlerClient = webCrawlerClient;
        this.analyzer = analyzer;
        this.configuration = configuration;
    }

    @Override
    public void run() {
        System.out.printf("WebCrawler thread %s started %n", Thread.currentThread().getName());
        while (true) {
            CrawlTask task = null;
            try {
                System.out.printf("WebCrawler thread %s trying to fetch task... %n", Thread.currentThread().getName());
                task = sharedState.urlQueue().poll(5, TimeUnit.SECONDS);
                if (task == null) {
                    if (sharedState.activeThreads().get() == 0 && sharedState.urlQueue().isEmpty()) {
                        System.out.printf("WebCrawler thread %s: No further tasks and no active threads, finishing job...%n", Thread.currentThread().getName());
                        sharedState.completionLatch().countDown();
                        break;
                    }
                    continue;
                }

                System.out.printf("WebCrawler thread %s fetched task: %s %n", Thread.currentThread().getName(), task);
                sharedState.activeThreads().incrementAndGet();
                crawlLink(task.url(), task.depth());

            } catch (InterruptedException e) {
                System.err.printf("Web Crawler thread %s threw InterruptedException while executing task: %s", Thread.currentThread().getName(), e.getMessage());
                Thread.currentThread().interrupt();
                break;
            } finally {
                if (task != null) {
                    sharedState.activeThreads().decrementAndGet();
                }
            }
        }
        System.out.printf("WebCrawler thread %s finished %n", Thread.currentThread().getName());
    }

    protected void crawlLink(String url, int depth) {
        if (!shouldCrawl(url, depth)) {
            System.out.printf("WebCrawler thread %s skipping link %s %n", Thread.currentThread().getName(), url);
            return;
        }

        if (webCrawlerClient.isPageAvailable(url)) {
            try {
                String html = webCrawlerClient.getPageContent(url);
                WorkingLink link = analyzer.analyze(url, depth, html);
                sharedState.crawledLinks().add(link);
                reportSublinks(link.getSubLinks(), depth);
                System.out.printf("WebCrawler thread %s successfully crawled link %s %n", Thread.currentThread().getName(), url);
            } catch (Exception e) {
                System.err.printf("%s: Unexpected error while crawling %s: %s%n", Thread.currentThread().getName(), url, e.getMessage());
                sharedState.crawledLinks().add(new BrokenLink(url, depth));
            }
        } else {
            sharedState.crawledLinks().add(new BrokenLink(url, depth));
        }
    }

    protected boolean shouldCrawl(String url, int depth) {
        return depth <= configuration.maximumDepth() && configuration.isAllowedDomain(url) && !isAlreadyCrawledUrl(url);
    }

    protected void reportSublinks(Set<String> subLinks, int depth) {
        subLinks.forEach(sub -> {
            try {
                if (depth + 1 <= configuration.maximumDepth() && !isAlreadyCrawledUrl(sub)) {
                    sharedState.urlQueue().put(new CrawlTask(sub, depth + 1));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    protected boolean isAlreadyCrawledUrl(String url) {
        return sharedState.containsCrawledUrl(url);
    }
}
