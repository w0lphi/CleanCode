package org.aau.crawler;

import org.aau.crawler.result.WorkingLink;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Set;

public class PageAnalyzer {

    private final HtmlParser parser;

    public PageAnalyzer(HtmlParser parser) {
        this.parser = parser;
    }

    public WorkingLink analyze(String url, int depth, String html) {
        Document doc = Jsoup.parse(html, url);
        Set<String> links = parser.extractLinks(doc);
        Set<String> headings = parser.extractHeadings(doc);
        return new WorkingLink(url, depth, headings, links);
    }
}