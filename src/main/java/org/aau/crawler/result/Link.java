package org.aau.crawler.result;

import java.util.Comparator;
import java.util.Objects;

public abstract class Link implements Comparable<Link> {

    protected final String url;
    protected final int depth;

    public Link(String url, int depth) {
        this.url = url;
        this.depth = depth;
    }

    public String getUrl() {
        return url;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return Objects.equals(url, link.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public int compareTo(Link o) {
        return Comparator.comparing(Link::getUrl)
                .thenComparingInt(Link::getDepth)
                .compare(this, o);
    }

    public abstract String toMarkdownString();
}
