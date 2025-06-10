package org.aau.crawler.parser;

import org.aau.html.DocumentAdapter;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class HtmlParserImplIntegrationTest {

    @Test
    void extractHeadingsAndLinks_shouldWorkOnValidHtml() {
        String html = """
            <html>
              <head><title>Title</title></head>
              <body>
                <h2>Heading</h2>
                <a href="https://example.org">Link</a>
              </body>
            </html>
            """;

        HtmlParserImpl parser = new HtmlParserImpl();
        DocumentAdapter doc = new DocumentAdapter(html);

        Set<String> headings = parser.extractHeadings(doc);
        Set<String> links = parser.extractLinks(doc);

        assertTrue(headings.stream().anyMatch(h -> h.contains("^^ Heading")));
        assertTrue(links.contains("https://example.org"));
    }
}
