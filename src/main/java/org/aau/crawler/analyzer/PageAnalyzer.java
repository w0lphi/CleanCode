package org.aau.crawler.analyzer;

import org.aau.crawler.result.WorkingLink;

public interface PageAnalyzer {
    WorkingLink analyze(String url, int depth, String html);
}
