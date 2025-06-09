package org.aau.crawler.parser.jsoupadapter;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class ElementsAdapterTest {

    @Test
    void size_shouldReturnCorrectElementCount() {
        String html = "<ul><li>1</li><li>2</li><li>3</li></ul>";
        org.jsoup.select.Elements jsoupElements = Jsoup.parse(html).select("li");
        ElementsAdapter adapter = new ElementsAdapter(jsoupElements);

        assertEquals(3, adapter.size());
    }

    @Test
    void get_shouldReturnCorrectElementAdapter() {
        String html = "<div><p>first</p><p>second</p></div>";
        org.jsoup.select.Elements jsoupElements = Jsoup.parse(html).select("p");
        ElementsAdapter adapter = new ElementsAdapter(jsoupElements);

        Element first = adapter.get(0);
        assertEquals("first", first.text());

        Element second = adapter.get(1);
        assertEquals("second", second.text());
    }

    @Test
    void iterator_shouldAllowLoopingOverElements() {
        String html = "<div><a>1</a><a>2</a></div>";
        ElementsAdapter adapter = new ElementsAdapter(Jsoup.parse(html).select("a"));

        Iterator<Element> it = adapter.iterator();
        int count = 0;
        while (it.hasNext()) {
            Element el = it.next();
            assertNotNull(el.text());
            count++;
        }

        assertEquals(2, count);
    }
}
