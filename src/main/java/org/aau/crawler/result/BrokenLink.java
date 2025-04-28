package org.aau.crawler.result;

public class BrokenLink extends Link {

    public BrokenLink(String url, int depth) {
        super(url, depth);
    }

    @Override
    public String toMarkdownString() {
        return """
                ## %s (broken)
                Depth: %s
                """.formatted(this.getUrl(), this.getDepth());
    }
}
