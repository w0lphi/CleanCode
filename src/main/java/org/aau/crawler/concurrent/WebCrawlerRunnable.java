package org.aau.crawler.concurrent;

import org.aau.config.WebCrawlerConfiguration;
import org.aau.crawler.analyzer.PageAnalyzer;
import org.aau.crawler.analyzer.PageAnalyzerImpl;
import org.aau.crawler.client.WebCrawlerClient;
import org.aau.crawler.client.WebCrawlerClientImpl;
import org.aau.crawler.error.CrawlingError;
import org.aau.crawler.parser.HtmlParserImpl;
import org.aau.crawler.result.BrokenLink;
import org.aau.crawler.result.WorkingLink;

import java.util.Set;

public class WebCrawlerRunnable implements Runnable {

    private final WebCrawlerSharedState sharedState;
    private final WebCrawlerClient webCrawlerClient;
    private final PageAnalyzer analyzer;
    private final WebCrawlerConfiguration configuration;

    public WebCrawlerRunnable(WebCrawlerSharedState sharedState, WebCrawlerConfiguration configuration) {
        this.sharedState = sharedState;
        this.webCrawlerClient = createWebCrawlerClient();
        this.analyzer = createPageAnalyzer();
        this.configuration = configuration;
    }

    @Override
    public void run() {
        System.out.printf("WebCrawler thread %s started %n", Thread.currentThread().getName());
        try (webCrawlerClient) {
            processCrawlTasks();
        } catch (Exception e) {
            System.err.printf("Unexpected error while running WebCrawler thread %s: %s%n", Thread.currentThread().getName(), e.getMessage());
            reportError("Unexpected error while running WebCrawler thread %s".formatted(Thread.currentThread().getName()), e);
        }
        System.out.printf("WebCrawler thread %s finished %n", Thread.currentThread().getName());
    }

    protected void processCrawlTasks() {
        while (true) {
            CrawlTask task = null;
            try {
                System.out.printf("WebCrawler thread %s trying to fetch task... %n", Thread.currentThread().getName());
                task = sharedState.getNextTask();
                if (task == null) {
                    if (!sharedState.hasActiveThreads() && sharedState.hasNoFurtherTasks()) {
                        System.out.printf("WebCrawler thread %s: No further tasks and no active threads, finishing job...%n", Thread.currentThread().getName());
                        sharedState.countDownCompletionLatch();
                        break;
                    }
                    continue;
                }

                System.out.printf("WebCrawler thread %s fetched task: %s%n", Thread.currentThread().getName(), task);
                sharedState.incrementActiveThreads();
                crawlLink(task.url(), task.depth());

            } catch (InterruptedException e) {
                System.err.printf("Web Crawler thread %s was interrupted while executing task: %s%n", Thread.currentThread().getName(), e.getMessage());
                reportError("Web Crawler thread %s was interrupted while executing task: %s".formatted(Thread.currentThread().getName(), task), e);
                Thread.currentThread().interrupt();
                break;
            } finally {
                if (task != null) {
                    sharedState.decrementActiveThreads();
                }
            }
        }
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
                sharedState.addCrawledLink(link);
                reportSublinks(link.getSubLinks(), depth);
                System.out.printf("WebCrawler thread %s successfully crawled link %s %n", Thread.currentThread().getName(), url);
            } catch (RuntimeException e) {
                System.err.printf("%s: Unexpected error while crawling %s, reporting broken link: %s%n", Thread.currentThread().getName(), url, e.getMessage());
                reportError("Unexpected error while crawling %s, reporting broken link".formatted(url), e);
                sharedState.addCrawledLink(new BrokenLink(url, depth));
            }
        } else {
            sharedState.addCrawledLink(new BrokenLink(url, depth));
        }
    }

    protected boolean shouldCrawl(String url, int depth) {
        return depth <= configuration.maximumDepth() && configuration.isAllowedDomain(url) && !isAlreadyCrawledUrl(url);
    }

    protected void reportSublinks(Set<String> subLinks, int depth) {
        subLinks.forEach(sub -> {
            try {
                if (depth + 1 <= configuration.maximumDepth() && !isAlreadyCrawledUrl(sub)) {
                    sharedState.addTask(new CrawlTask(sub, depth + 1));
                }
            } catch (InterruptedException e) {
                System.err.printf("Web Crawler thread %s was interrupted while reporting sublinks: %s%n", Thread.currentThread().getName(), e.getMessage());
                reportError("Web Crawler thread %s was interrupted while reporting sublinks".formatted(Thread.currentThread().getName()), e);
                Thread.currentThread().interrupt();
            }
        });
    }

    protected boolean isAlreadyCrawledUrl(String url) {
        return sharedState.containsCrawledUrl(url);
    }

    protected WebCrawlerClient createWebCrawlerClient() {
        return new WebCrawlerClientImpl();
    }

    protected PageAnalyzer createPageAnalyzer() {
        return new PageAnalyzerImpl(new HtmlParserImpl());
    }

    protected void reportError(String message, Throwable e) {
        var crawlingError = new CrawlingError(message, e);
        sharedState.reportCrawlingError(crawlingError);
    }
}
