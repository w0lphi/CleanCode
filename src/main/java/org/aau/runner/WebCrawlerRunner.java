package org.aau.runner;

import org.aau.crawler.parser.HtmlParserImpl;
import org.aau.crawler.analyzer.PageAnalyzerImpl;
import org.aau.crawler.WebCrawler;
import org.aau.crawler.client.WebCrawlerClientImpl;
import org.aau.writer.MarkdownFormatter;
import org.aau.writer.MarkdownWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;

public class WebCrawlerRunner {

    protected final WebCrawler crawler;
    protected final String outputDir;

    public WebCrawlerRunner(String startUrl, int maximumDepth, String outputDir) {
        this.crawler = initializeCrawler(startUrl, maximumDepth);
        this.outputDir = outputDir;
    }

    public Path executeCrawl() throws IOException {
        crawler.start();
        return writeCrawlerResultsToFile(crawler);
    }

    protected Path writeCrawlerResultsToFile(WebCrawler crawler) throws IOException {
        var writer = new MarkdownWriter(outputDir, new MarkdownFormatter());
        return writer.writeResultsToFile(crawler.getCrawledLinks(), OffsetDateTime.now());
    }

    protected WebCrawler initializeCrawler(String startUrl, int maxDepth) {
        var client = new WebCrawlerClientImpl();
        var analyzer = new PageAnalyzerImpl(new HtmlParserImpl());
        return new WebCrawler(startUrl, maxDepth, client, analyzer);
    }
}
