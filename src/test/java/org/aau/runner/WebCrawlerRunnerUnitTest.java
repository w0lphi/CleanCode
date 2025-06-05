package org.aau.runner;

import org.aau.crawler.WebCrawler;
import org.aau.crawler.result.BrokenLink;
import org.aau.crawler.result.Link;
import org.aau.crawler.result.WorkingLink;
import org.aau.writer.MarkdownWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebCrawlerRunnerUnitTest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String OUTPUT_DIR = "build/test";
    private static final String startUrl = "http://test.com";
    WebCrawler crawlerMock;
    MarkdownWriter writerMock;
    Set<Link> links;

    @BeforeEach
    void setup() {
        this.crawlerMock = mock(WebCrawler.class);
        this.writerMock = mock(MarkdownWriter.class);

        Set<String> headings = new LinkedHashSet<>(List.of("Heading1", "Heading2", "Heading3"));
        Link workingLink = new WorkingLink("https://www.working.com", 1, headings, Set.of("Link3"));
        Link brokenLink = new BrokenLink("https://www.broken.com", 5);
        this.links = new LinkedHashSet<>(List.of(workingLink, brokenLink));
        when(crawlerMock.getCrawledLinks()).thenReturn(links);
    }

    @Test
    void testWriteSortedCrawlerResultsToFile() throws IOException {
        Path expectedPath = Path.of(OUTPUT_DIR).toAbsolutePath();
        var webCrawlerRunner = new WebCrawlerRunner(startUrl, 1, 1, OUTPUT_DIR) {
            @Override
            protected WebCrawler createCrawler(String startUrl, int maximumDepth, int threadCount) {
                return crawlerMock;
            }
        };

        OffsetDateTime timestamp = OffsetDateTime.now();
        String expectedContent = """
                # Crawl Results
                
                Timestamp: %s
                
                ## https://www.broken.com (broken)
                Depth: 5
                
                ## https://www.working.com
                Depth: 1
                ### Headings
                Heading1\s\s
                Heading2\s\s
                Heading3
                
                """.formatted(timestamp.format(DATE_TIME_FORMATTER));

        Path filePath = webCrawlerRunner.writeSortedCrawlerResultsToFile(crawlerMock, timestamp);

        assertTrue(filePath.toAbsolutePath().toString().startsWith(expectedPath + "/report-"));
        assertTrue(Files.exists(filePath));
        verify(crawlerMock).getCrawledLinks();

        StringBuilder builder = new StringBuilder();
        for (String line : Files.readAllLines(filePath)) {
            builder.append(line).append(System.lineSeparator());
        }
        assertEquals(expectedContent, builder.toString());
    }

    @Test
    void testRun() throws IOException {
        var webCrawlerRunner = new WebCrawlerRunner(startUrl, 1, 1, OUTPUT_DIR) {
            @Override
            protected WebCrawler createCrawler(String startUrl, int maximumDepth, int threadCount) {
                return crawlerMock;
            }

            @Override
            protected MarkdownWriter createMarkdownWriter(String outputDir) {
                return writerMock;
            }
        };

        webCrawlerRunner.run();
        verify(crawlerMock).start();
        verify(writerMock).writeResultsToFile(eq(links), any(OffsetDateTime.class));
        verify(crawlerMock).getCrawledLinks();
    }

    @Test
    void runShouldThrowRuntimeExceptionIfWritingFails() throws IOException {
        var ioException = new IOException("Test Exception");
        when(writerMock.writeResultsToFile(anySet(), any(OffsetDateTime.class))).thenThrow(ioException);
        var webCrawlerRunner = new WebCrawlerRunner(startUrl, 1, 1, OUTPUT_DIR) {
            @Override
            protected WebCrawler createCrawler(String startUrl, int maximumDepth, int threadCount) {
                return crawlerMock;
            }

            @Override
            protected MarkdownWriter createMarkdownWriter(String outputDir) {
                return writerMock;
            }
        };

        RuntimeException re = assertThrows(RuntimeException.class, webCrawlerRunner::run);
        assertEquals(ioException, re.getCause());
    }

}
