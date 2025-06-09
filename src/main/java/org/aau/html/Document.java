package org.aau.html;

public interface Document {
    Elements select(String cssQuery);

    String text();

    String title();
}
