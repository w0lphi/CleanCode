package org.aau.runner;

import org.aau.crawler.WebCrawler;
import org.aau.crawler.WebCrawlerImpl;
import org.aau.crawler.analyzer.PageAnalyzerImpl;
import org.aau.crawler.client.WebCrawlerClientImpl;
import org.aau.crawler.parser.HtmlParserImpl;
import org.aau.writer.MarkdownFormatter;
import org.aau.writer.MarkdownWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;

public class WebCrawlerRunner {

    private final WebCrawler crawler;
    private final MarkdownWriter writer;

    public WebCrawlerRunner(String startUrl, int maximumDepth, String outputDir) {
        this.crawler = createCrawler(startUrl, maximumDepth);
        this.writer = createMarkdownWriter(outputDir);
    }

    public Path executeCrawl() {
        try {
            crawler.start();
            return writeCrawlerResultsToFile(crawler, OffsetDateTime.now());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Path writeCrawlerResultsToFile(WebCrawler crawler, OffsetDateTime timestamp) throws IOException {
        return writer.writeResultsToFile(crawler.getCrawledLinks(), timestamp);
    }

    protected WebCrawler createCrawler(String startUrl, int maximumDepth) {
        var client = new WebCrawlerClientImpl();
        var analyzer = new PageAnalyzerImpl(new HtmlParserImpl());
        return new WebCrawlerImpl(startUrl, maximumDepth, client, analyzer);
    }

    protected MarkdownWriter createMarkdownWriter(String outputDir) {
        return new MarkdownWriter(outputDir, new MarkdownFormatter());
    }
}
