package org.aau.crawler;

import org.aau.crawler.client.WebCrawlerClient;
import org.aau.crawler.result.BrokenLink;
import org.aau.crawler.result.Link;
import org.aau.crawler.result.WorkingLink;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

public class WebCrawler {

    private final String startUrl;
    private final int maximumDepth;
    private final Set<Link> crawledLinks = new HashSet<>();
    private final WebCrawlerClient webCrawlerClient;

    public WebCrawler(String startUrl, int maximumDepth) {
        this.startUrl = startUrl;
        this.maximumDepth = maximumDepth;
        this.webCrawlerClient = new WebCrawlerClient();
    }

    public void start() {
        try (webCrawlerClient) {
            crawlLinkRecursively(startUrl, 0);
        }
    }

    void crawlLinkRecursively(String url, int depth) {
        if (depth > maximumDepth || isAlreadyCrawledUrl(url)) {
            return;
        }

        System.out.printf("Crawling URL: url=%s, depth=%d\n", url, depth);
        if (webCrawlerClient.isPageAvailable(url)) {
            try {
                WorkingLink workingLink = buildWorkingLink(url, depth);
                crawledLinks.add(workingLink);
                workingLink.getSubLinks().forEach(link -> crawlLinkRecursively(link, depth + 1));
            } catch (Exception e) {
                System.err.printf("Exception occurred while crawling: url=%s, error=%s\n", url, e.getMessage());
                crawledLinks.add(new BrokenLink(url, depth));
            }
        } else {
            System.err.printf("Page not available: url=%s\n", url);
            crawledLinks.add(new BrokenLink(url, depth));
        }
    }

    boolean isAlreadyCrawledUrl(String url) {
        return crawledLinks.stream().anyMatch(crawlResult -> crawlResult.getUrl().equals(url));
    }

    String getPageContent(String url) {
        return webCrawlerClient.getPageContent(url);
    }

    WorkingLink buildWorkingLink(String url, int depth) {
        String pageContent = getPageContent(url);
        Document htmlDocument = Jsoup.parse(pageContent, url);
        Set<String> subLinks = extractLinks(htmlDocument);
        Set<String> headings = extractHeadings(htmlDocument);
        return new WorkingLink(url, depth, headings, subLinks);
    }

    Set<String> extractHeadings(Document htmlDocument) {
        Elements headings = htmlDocument.select(":is(h1,h2,h3,h4,h5)");
        Set<String> headingSet = new HashSet<>();
        for (Element heading : headings) {
            String tagName = heading.tagName();
            int level = Integer.parseInt(tagName.substring(1));
            String hashtags = "#".repeat(level);
            headingSet.add(hashtags + " " + heading.text());
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

    public Set<Link> getCrawledLinks() {
        return crawledLinks;
    }
}
