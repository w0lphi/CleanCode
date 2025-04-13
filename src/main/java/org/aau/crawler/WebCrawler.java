package org.aau.crawler;

import org.aau.crawler.result.CrawlResult;
import org.aau.driver.CrawlerWebDriver;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;

import java.util.HashSet;
import java.util.Set;

public class WebCrawler {

    private final String startUrl;
    private final int maximumDepth;
    private final Set<CrawlResult> crawlResults = new HashSet<>();
    private final CrawlerWebDriver crawlerWebDriver;

    public WebCrawler(String startUrl, int maximumDepth) {
        this.startUrl = startUrl;
        this.maximumDepth = maximumDepth;
        this.crawlerWebDriver = new CrawlerWebDriver();
    }

    public void start() {
        try (crawlerWebDriver) {
            crawlUrl(startUrl, 0);
        }
    }

    void crawlUrl(String url, int depth) {
        if (depth <= maximumDepth && crawlResults.stream().noneMatch(crawlResult -> crawlResult.url().equals(url))) {
            try {
                System.out.printf("Crawling URL: url=%s, depth=%d\n", url, depth);
                CrawlResult crawlResult = buildCrawlResult(url, depth);
                crawlResults.add(crawlResult);
                crawlResult.subLinks().forEach(link -> crawlUrl(link, depth + 1));
            } catch (TimeoutException te) {
                System.err.printf("Timeout while loading: url=%s\n", url);
            } catch (UnhandledAlertException uae) {
                System.err.printf("Unexpected alert opened: url=%s, exception=%s\n", url, uae.getMessage());
            }  catch (Exception e) {
                System.err.printf("Unexpected exception: url=%s, exception=%s\n", url, e.getMessage());
            }
        }
    }

    CrawlResult buildCrawlResult(String url, int depth) {
        String pageContent = crawlerWebDriver.getPageContent(url);
        Document htmlDocument = Jsoup.parse(pageContent, url);
        Set<String> subLinks = extractLinks(htmlDocument);
        Set<String> headings = extractHeadings(htmlDocument);
        return new CrawlResult(url, depth, headings, subLinks);
    }

    Set<String> extractHeadings(Document htmlDocument) {
        Elements headings = htmlDocument.select(":is(h1,h2,h3,h4,h5)");
        Set<String> headingSet = new HashSet<>();
        for (Element heading : headings) {
            headingSet.add(heading.text());
        }
        return headingSet;
    }

    Set<String> extractLinks(Document htmlDocument) {
        Elements links = htmlDocument.select("a[href]");
        Set<String> urls = new HashSet<>();
        for (Element link : links) {
            String absHref = link.attr("abs:href");
            urls.add(absHref);
        }
        return urls;
    }

    public Set<CrawlResult> getCrawlResults() {
        return crawlResults;
    }
}
