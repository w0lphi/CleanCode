package org.aau.crawler.parser.jsoupadapter;

public interface Element {
    String attr(String key);
    String text();
    Elements select(String cssQuery);

    String tagName();
}
