package org.aau.html;

public class ElementAdapter implements Element {
    private final org.jsoup.nodes.Element element;

    public ElementAdapter(org.jsoup.nodes.Element element) {
        this.element = element;
    }

    @Override
    public String attr(String key) {
        return element.attr(key);
    }

    @Override
    public String text() {
        return element.text();
    }

    @Override
    public Elements select(String cssQuery) {
        return new ElementsAdapter(element.select(cssQuery));
    }

    @Override
    public String tagName() {
        return element.tagName();
    }
}
