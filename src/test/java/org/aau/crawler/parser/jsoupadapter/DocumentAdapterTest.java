package org.aau.crawler.parser.jsoupadapter;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentAdapterTest {

    @Test
    void select_shouldReturnElementsAdapter() {
        String html = "<html><body><p>Test</p></body></html>";
        DocumentAdapter doc = new DocumentAdapter(Jsoup.parse(html));

        Elements result = doc.select("p");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).text());
    }

    @Test
    void text_shouldReturnAllTextContent() {
        String html = "<html><body><h1>Hello</h1><p>World</p></body></html>";
        DocumentAdapter doc = new DocumentAdapter(Jsoup.parse(html));

        assertTrue(doc.text().contains("Hello"));
        assertTrue(doc.text().contains("World"));
    }

    @Test
    void title_shouldReturnDocumentTitle() {
        String html = "<html><head><title>MyTitle</title></head><body></body></html>";
        DocumentAdapter doc = new DocumentAdapter(Jsoup.parse(html));

        assertEquals("MyTitle", doc.title());
    }
}
