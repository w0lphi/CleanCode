package org.aau.crawler;

import org.aau.crawler.analyzer.PageAnalyzer;
import org.aau.crawler.client.WebCrawlerClient;
import org.aau.crawler.result.BrokenLink;
import org.aau.crawler.result.Link;
import org.aau.crawler.result.WorkingLink;

import java.util.LinkedHashSet;
import java.util.Set;

public class WebCrawlerImpl implements WebCrawler {

    private final String startUrl;
    private final int maximumDepth;
    private final Set<Link> crawledLinks = new LinkedHashSet<>();
    private final WebCrawlerClient webCrawlerClient;
    private final PageAnalyzer analyzer;

    public WebCrawlerImpl(String startUrl, int maximumDepth, WebCrawlerClient webCrawlerClient, PageAnalyzer analyzer) {
        this.startUrl = startUrl;
        this.maximumDepth = maximumDepth;
        this.webCrawlerClient = webCrawlerClient;
        this.analyzer = analyzer;
    }

    @Override
    public void start() {
        try (webCrawlerClient) {
            crawlLinkRecursively(startUrl, 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<Link> getCrawledLinks() {
        return crawledLinks;
    }

    protected void crawlLinkRecursively(String url, int depth) {
        if (depth > maximumDepth || isAlreadyCrawledUrl(url)) return;

        if (webCrawlerClient.isPageAvailable(url)) {
            try {
                String html = webCrawlerClient.getPageContent(url);
                WorkingLink link = analyzer.analyze(url, depth, html);
                crawledLinks.add(link);
                link.getSubLinks().forEach(sub -> crawlLinkRecursively(sub, depth + 1));
            } catch (Exception e) {
                crawledLinks.add(new BrokenLink(url, depth));
            }
        } else {
            crawledLinks.add(new BrokenLink(url, depth));
        }
    }

    private boolean isAlreadyCrawledUrl(String url) {
        return crawledLinks.stream().anyMatch(crawlResult -> crawlResult.getUrl().equals(url));
    }
}
