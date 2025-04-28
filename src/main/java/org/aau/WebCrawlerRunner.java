package org.aau;

import org.aau.crawler.HtmlParser;
import org.aau.crawler.PageAnalyzer;
import org.aau.crawler.WebCrawler;
import org.aau.crawler.client.WebCrawlerClient;
import org.aau.writer.MarkdownFormatter;
import org.aau.writer.MarkdownWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;

public class WebCrawlerRunner {

    private final WebCrawler crawler;
    private final String outputDir;

    public WebCrawlerRunner(String startUrl, int maximumDepth, String outputDir) {
        this.crawler = initializeCrawler(startUrl, maximumDepth);
        this.outputDir = outputDir;
    }

    public Path executeCrawl() throws IOException {
        crawler.start();
        return writeCrawlerResultsToFile(crawler);
    }

    private Path writeCrawlerResultsToFile(WebCrawler crawler) throws IOException {
        var writer = new MarkdownWriter(outputDir, new MarkdownFormatter());
        return writer.writeResultsToFile(crawler.getCrawledLinks(), OffsetDateTime.now());
    }

    private WebCrawler initializeCrawler(String startUrl, int maxDepth) {
        var client = new WebCrawlerClient();
        var analyzer = new PageAnalyzer(new HtmlParser());
        return new WebCrawler(startUrl, maxDepth, client, analyzer);
    }
}
