package org.aau.writer;

import org.aau.crawler.result.BrokenLink;
import org.aau.crawler.result.Link;
import org.aau.crawler.result.WorkingLink;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MarkdownWriterIntegrationTest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String TEST_FILE_PATH = "build/test.md";

    @Test
    void testWriteResultsToFile() throws IOException {
        Path expectedPath = Path.of(TEST_FILE_PATH).toAbsolutePath();
        MarkdownWriter writer = new MarkdownWriter(TEST_FILE_PATH);
        Link workingLink = new WorkingLink("https://www.working.com", 1, Set.of("Heading1", "Heading2", "Heading3"), Set.of("Link3"));
        Link brokenLink = new BrokenLink("https://www.broken.com", 5);
        OffsetDateTime timestamp = OffsetDateTime.now();
        String expectedContent = """
                # Crawl Results
                
                Results from %s
                
                %s
                %s
                """.formatted(timestamp.format(DATE_TIME_FORMATTER), workingLink, brokenLink);

        Path filePath = writer.writeResultsToFile(Set.of(workingLink, brokenLink), timestamp);
        assertEquals(expectedPath, filePath.toAbsolutePath());
        assertTrue(Files.exists(filePath));

        StringBuilder builder = new StringBuilder();
        for (String line : Files.readAllLines(filePath)) {
            builder.append(line).append(System.lineSeparator());
        }
        assertEquals(expectedContent, builder.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Path.of(TEST_FILE_PATH));
    }

}
