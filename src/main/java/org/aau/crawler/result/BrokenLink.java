package org.aau.crawler.result;

public class BrokenLink extends Link {

    public BrokenLink(String url, int depth) {
        super(url, depth);
    }

    @Override
    public String toString() {
        return """
                URL: %s (broken link) <br>
                Depth: %s <br>
                """.formatted(url, depth);
    }
}
