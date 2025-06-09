package org.aau.writer;

import org.aau.crawler.error.CrawlingError;
import org.aau.crawler.result.BrokenLink;
import org.aau.crawler.result.Link;
import org.aau.crawler.result.WorkingLink;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MarkdownWriterIntegrationTest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String TEST_FILE_PATH = "build/test";

    @Test
    void testWriteResultsToFile() throws IOException {
        Path expectedPath = Path.of(TEST_FILE_PATH).toAbsolutePath();
        MarkdownWriter writer = new MarkdownWriter(TEST_FILE_PATH);
        Set<String> headings = new LinkedHashSet<>(List.of("Heading1", "Heading2", "Heading3"));
        Link workingLink = new WorkingLink("https://www.working.com", 1, headings, Set.of("Link3"));
        Link brokenLink = new BrokenLink("https://www.broken.com", 5);
        Set<Link> links = new LinkedHashSet<>(List.of(workingLink, brokenLink));
        var crawlingError = new CrawlingError("Unexpected Error", new RuntimeException("Something went wrong"));
        List<CrawlingError> errors = List.of(crawlingError);
        OffsetDateTime timestamp = OffsetDateTime.now();
        String expectedContent = """
                # Crawl Results
                
                Timestamp: %s
                
                ## https://www.working.com
                Depth: 1
                ### Headings
                Heading1\s\s
                Heading2\s\s
                Heading3
                
                ## https://www.broken.com (broken)
                Depth: 5
                
                # Errors
                
                Count: 1
                
                ## Error Messages
                
                Message: Unexpected Error
                Cause: Something went wrong
                
                """.formatted(timestamp.format(DATE_TIME_FORMATTER));

        Path filePath = writer.writeResultsToFile(links, errors, timestamp);
        assertEquals(expectedPath + "/report-" + timestamp.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".md", filePath.toAbsolutePath().toString());
        assertTrue(Files.exists(filePath));

        StringBuilder builder = new StringBuilder();
        for (String line : Files.readAllLines(filePath)) {
            builder.append(line).append(System.lineSeparator());
        }
        assertEquals(expectedContent, builder.toString());
    }

    @AfterEach
    void teardown() throws IOException {
        Path path = Paths.get(TEST_FILE_PATH);
        if (Files.exists(path)) {
            try (var paths = Files.walk(path)) {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                throw new RuntimeException("Error when deleting file: path = %s".formatted(p), e);
                            }
                        });
            }
        }
    }
}
