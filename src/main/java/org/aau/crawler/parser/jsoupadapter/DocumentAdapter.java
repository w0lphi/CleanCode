package org.aau.crawler.parser.jsoupadapter;

public class DocumentAdapter implements Document {
    private final org.jsoup.nodes.Document document;

    public DocumentAdapter(org.jsoup.nodes.Document document) {
        this.document = document;
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
