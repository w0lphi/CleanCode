package org.aau.html;

import org.jsoup.Jsoup;

public class DocumentAdapter implements Document {
    private final org.jsoup.nodes.Document document;

    public DocumentAdapter(String html, String url) {
        this.document = Jsoup.parse(html, url);
    }

    public DocumentAdapter(String html) {
        this.document = Jsoup.parse(html);
    }

    @Override
    public Elements select(String cssQuery) {
        return new ElementsAdapter(document.select(cssQuery));
    }

    @Override
    public String text() {
        return document.text();
    }

    @Override
    public String title() {
        return document.title();
    }
}
