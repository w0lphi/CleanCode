package org.aau.crawler.parser.jsoupadapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ElementAdapterTest {

    @Test
    void attr_shouldReturnCorrectAttribute() {
        String html = "<a href='https://example.com'>Link</a>";
        Element jsoupElement = Jsoup.parse(html).selectFirst("a");
        ElementAdapter adapter = new ElementAdapter(jsoupElement);

        assertEquals("https://example.com", adapter.attr("href"));
    }

    @Test
    void text_shouldReturnElementText() {
        String html = "<div>Text here</div>";
        Element jsoupElement = Jsoup.parse(html).selectFirst("div");
        ElementAdapter adapter = new ElementAdapter(jsoupElement);

        assertEquals("Text here", adapter.text());
    }

    @Test
    void tagName_shouldReturnCorrectTag() {
        String html = "<section>Content</section>";
        Element jsoupElement = Jsoup.parse(html).selectFirst("section");
        ElementAdapter adapter = new ElementAdapter(jsoupElement);

        assertEquals("section", adapter.tagName());
    }

    @Test
    void select_shouldReturnSubElements() {
        String html = "<div><span>one</span><span>two</span></div>";
        Element jsoupElement = Jsoup.parse(html).selectFirst("div");
        ElementAdapter adapter = new ElementAdapter(jsoupElement);

        Elements result = adapter.select("span");
        assertEquals(2, result.size());
        assertEquals("one", result.get(0).text());
    }
}
