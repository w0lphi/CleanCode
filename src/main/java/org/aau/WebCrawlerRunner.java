package org.aau;

import org.aau.crawler.HtmlParser;
import org.aau.crawler.PageAnalyzer;
import org.aau.crawler.WebCrawler;
import org.aau.crawler.client.WebCrawlerClient;
import org.aau.writer.MarkdownFormatter;
import org.aau.writer.MarkdownWriter;

import java.nio.file.Path;
import java.time.OffsetDateTime;

public class WebCrawlerRunner {
    public Path run(String startUrl, int maxDepth, String outputDir) throws Exception {
        var client = new WebCrawlerClient();
        var analyzer = new PageAnalyzer(new HtmlParser());
        var crawler = new WebCrawler(startUrl, maxDepth, client, analyzer);
        crawler.start();

        var writer = new MarkdownWriter(outputDir, new MarkdownFormatter());
        return writer.writeResultsToFile(crawler.getCrawledLinks(), OffsetDateTime.now());
    }
}
