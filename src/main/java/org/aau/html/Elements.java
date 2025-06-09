package org.aau.html;

public interface Elements extends Iterable<Element> {
    int size();

    Element get(int index);
}