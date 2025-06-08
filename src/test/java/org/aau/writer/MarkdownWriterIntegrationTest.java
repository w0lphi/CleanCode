package org.aau.writer;

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
        MarkdownWriter writer = new MarkdownWriter(TEST_FILE_PATH);
        Set<String> headings = new LinkedHashSet<>(List.of("Heading1", "Heading2", "Heading3"));
        Link workingLink = new WorkingLink("https://www.working.com", 1, headings, Set.of("Link3"));
        Link brokenLink = new BrokenLink("https://www.broken.com", 5);
        Set<Link> links = new LinkedHashSet<>(List.of(workingLink, brokenLink));

        OffsetDateTime timestamp = OffsetDateTime.now();
        Path expectedPath = Path.of(TEST_FILE_PATH).toAbsolutePath()
                .resolve("report-" + timestamp.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".md");

        Path filePath = writer.writeResultsToFile(links, timestamp);

        assertEquals(expectedPath, filePath.toAbsolutePath());
        assertTrue(Files.exists(filePath));

        Files.deleteIfExists(filePath);
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
