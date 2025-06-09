package org.aau.html;

public interface Element {
    String attr(String key);

    String text();

    Elements select(String cssQuery);

    String tagName();
}
