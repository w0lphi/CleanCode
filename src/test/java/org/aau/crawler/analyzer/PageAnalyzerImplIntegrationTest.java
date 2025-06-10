package org.aau.crawler.analyzer;

import org.aau.crawler.parser.HtmlParser;
import org.aau.crawler.parser.HtmlParserImpl;
import org.aau.crawler.result.WorkingLink;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PageAnalyzerImplIntegrationTest {

    @Test
    void analyzePage_shouldExtractExpectedHeadingsAndLinks() {
        String html = """
            <html>
              <head><title>Test Page</title></head>
              <body>
                <h1>Main Title</h1>
                <h2>Subheading</h2>
                <a href="https://example.com">Example</a>
                <a href="https://broken-link.com">Broken</a>
              </body>
            </html>
            """;

        HtmlParser parser = new HtmlParserImpl();
        PageAnalyzerImpl analyzer = new PageAnalyzerImpl(parser);

        WorkingLink result = analyzer.analyze("https://test.com", 1, html);

        assertTrue(result.getHeadings().contains("^ Main Title"));
        assertTrue(result.getHeadings().contains("^^ Subheading"));
        assertTrue(result.getSubLinks().contains("https://example.com"));
        assertTrue(result.getSubLinks().contains("https://broken-link.com"));
    }
}
