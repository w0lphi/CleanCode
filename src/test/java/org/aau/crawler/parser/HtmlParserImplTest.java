package org.aau.crawler.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class HtmlParserImplTest {

    private final HtmlParserImpl parser = new HtmlParserImpl();

    @Test
    void  extractHeadings_shouldReturnFormattedHeadings(){
        String html = "<h1>Main Title</h1><h2>Sub Title</h2><h5>Deep Title</h5>";
        Document doc = Jsoup.parse(html);

        Set<String> headings = parser.extractHeadings(doc);

        assertTrue(headings.contains("+ Main Title"));
        assertTrue(headings.contains("++ Sub Title"));
        assertTrue(headings.contains("+++++ Deep Title"));
        assertEquals(3, headings.size());
    }

    @Test
    void extractLinks_shouldReturnAbsoluteUrls(){
        String html = "<a href='https://www.example.com'>Link 1</a><a href='https://www.example.org'>Link 2</a>";
        Document doc = Jsoup.parse(html);

        Set<String> links = parser.extractLinks(doc);

        assertTrue(links.contains("https://www.example.com"));
        assertTrue(links.contains("https://www.example.org"));
        assertEquals(2, links.size());
    }
}
