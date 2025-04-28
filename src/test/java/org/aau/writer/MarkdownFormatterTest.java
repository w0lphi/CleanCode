package org.aau.writer;

import org.aau.crawler.result.BrokenLink;
import org.aau.crawler.result.Link;
import org.aau.crawler.result.WorkingLink;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class MarkdownFormatterTest {

    private final MarkdownFormatter formatter = new MarkdownFormatter();

    @Test
    void format_shouldFormatterWorkingLinkCorrectly() {
        WorkingLink workingLink = new WorkingLink(
                "https://example.com",
                1,
                Set.of("# Heading 1", "## Heading 2"),
                Set.of("https://example.com/page1")
        );

        String result = formatter.format(workingLink);

        assertTrue(result.contains("## https://example.com"));
        assertTrue(result.contains("Depth: 1"));
        assertTrue(result.contains("### Headings"));
        assertTrue(result.contains("# Heading 1"));
        assertTrue(result.contains("## Heading 2"));
    }

    @Test
    void format_shouldFormatterBrokenLinkCorrectly() {
        BrokenLink brokenLink = new BrokenLink(
                "https://example.com/broken",
                2
        );

        String result = formatter.format(brokenLink);

        assertTrue(result.contains("## https://example.com/broken (broken)"));
        assertTrue(result.contains("Depth: 2"));
    }

    @Test
    void format_shouldReturnUrlForGenericLink() {
        Link genericLink = new Link("https://example.com/generic", 0) {
        };

        String result = formatter.format(genericLink);

        assertEquals("https://example.com/generic", result);
    }
}