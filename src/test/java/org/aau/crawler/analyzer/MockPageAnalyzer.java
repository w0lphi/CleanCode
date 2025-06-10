package org.aau.crawler.analyzer;

import org.aau.crawler.result.WorkingLink;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class MockPageAnalyzer implements PageAnalyzer {
    private final Map<String, Set<String>> extractedLinksMap;
    private final Map<String, Set<String>> extractedHeadingsMap;

    public MockPageAnalyzer(Map<String, Set<String>> extractedLinksMap, Map<String, Set<String>> extractedHeadingsMap) {
        this.extractedLinksMap = extractedLinksMap;
        this.extractedHeadingsMap = extractedHeadingsMap;
    }

    @Override
    public WorkingLink analyze(String url, int depth, String html) {
        Set<String> links = extractedLinksMap.getOrDefault(url, Collections.emptySet());
        Set<String> headings = extractedHeadingsMap.getOrDefault(url, Collections.emptySet());
        return new WorkingLink(url, depth, headings, links);
    }
}