package org.aau.crawler.parser.jsoupadapter;

public interface Document {
    Elements select(String cssQuery);
    String text();
    String title();
}
