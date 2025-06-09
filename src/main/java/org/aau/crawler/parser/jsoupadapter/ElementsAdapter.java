package org.aau.crawler.parser.jsoupadapter;

import org.jsoup.select.Elements;
import java.util.Iterator;

public class ElementsAdapter implements org.aau.crawler.parser.jsoupadapter.Elements {
    private final Elements elements;

    public ElementsAdapter(Elements elements) {
        this.elements = elements;
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public org.aau.crawler.parser.jsoupadapter.Element get(int index) {
        return new ElementAdapter(elements.get(index));
    }

    @Override
    public Iterator<org.aau.crawler.parser.jsoupadapter.Element> iterator() {
        return new Iterator<>() {
            private final Iterator<org.jsoup.nodes.Element> jsoupIterator = elements.iterator();

            @Override
            public boolean hasNext() {
                return jsoupIterator.hasNext();
            }

            @Override
            public org.aau.crawler.parser.jsoupadapter.Element next() {
                return new ElementAdapter(jsoupIterator.next());
            }
        };
    }
}
