package org.aau.crawler.parser.jsoupadapter;

public interface Elements extends Iterable<Element> {
    int size();
    Element get(int index);
}