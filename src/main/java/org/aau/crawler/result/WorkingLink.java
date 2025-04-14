package org.aau.crawler.result;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class WorkingLink extends Link {

    private final Set<String> headings;
    private final Set<String> subLinks;

    public WorkingLink(String url, int depth, Set<String> headings, Set<String> subLinks) {
        super(url, depth);
        this.headings = headings;
        this.subLinks = subLinks;
    }

    public Set<String> getSubLinks() {
        return Optional.ofNullable(subLinks).orElse(new HashSet<>());
    }

    @Override
    public String toString() {
        return """
                URL: %s <br>
                Depth: %s <br>
                Headings: <br>
                %s <br>
                """.formatted(url, depth, String.join("<br>\n", headings));
    }
}
