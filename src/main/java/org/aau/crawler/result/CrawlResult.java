package org.aau.crawler.result;

import java.util.Objects;
import java.util.Set;

public record CrawlResult(String url, int depth, Set<String> headings, Set<String> subLinks) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrawlResult that = (CrawlResult) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, subLinks);
    }

    @Override
    public String toString() {
        return """
                URL: %s <br>
                Depth: %s <br>
                Headings:
                %s <br>
                """.formatted(url, depth, String.join("\n", headings));
    }
}
