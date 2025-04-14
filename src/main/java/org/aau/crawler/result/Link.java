package org.aau.crawler.result;

import java.util.Objects;

public abstract class Link {

    protected final String url;
    protected final int depth;

    public Link(String url, int depth) {
        this.url = url;
        this.depth = depth;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return Objects.equals(url, link.url);
    }
}
