package org.aau.runner;

import org.aau.config.WebCrawlerConfiguration;
import org.aau.crawler.WebCrawler;
import org.aau.crawler.WebCrawlerImpl;
import org.aau.writer.MarkdownWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.TreeSet;

public class WebCrawlerRunner {

    private final WebCrawler crawler;
    private final MarkdownWriter writer;

    public WebCrawlerRunner(WebCrawlerConfiguration configuration) {
        this.crawler = createCrawler(configuration);
        this.writer = createMarkdownWriter(configuration.outputDir());
    }

    public Path run() throws RuntimeException {
        try {
            crawler.start();
            return writeSortedCrawlerResultsToFile(crawler, OffsetDateTime.now());
        } catch (IOException e) {
            System.err.printf("An unexpected error occurred while running WebCrawlerRunner: %s%n", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    protected Path writeSortedCrawlerResultsToFile(WebCrawler crawler, OffsetDateTime timestamp) throws IOException {
        return writer.writeResultsToFile(new TreeSet<>(crawler.getCrawledLinks()), timestamp);
    }

    protected WebCrawler createCrawler(WebCrawlerConfiguration configuration) {
        return new WebCrawlerImpl(configuration);
    }

    protected MarkdownWriter createMarkdownWriter(String outputDir) {
        return new MarkdownWriter(outputDir);
    }
}
