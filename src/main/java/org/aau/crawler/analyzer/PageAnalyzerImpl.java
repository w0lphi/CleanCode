package org.aau.crawler.analyzer;

import org.aau.crawler.parser.HtmlParser;
import org.aau.crawler.parser.HtmlParserImpl;
import org.aau.crawler.result.WorkingLink;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Set;

public class PageAnalyzerImpl implements PageAnalyzer {

    private final HtmlParser parser;

    public PageAnalyzerImpl(HtmlParser parser) {
        this.parser = parser;
    }

    @Override
    public WorkingLink analyze(String url, int depth, String html) {
        Document doc = Jsoup.parse(html, url);
        Set<String> links = parser.extractLinks(doc);
        Set<String> headings = parser.extractHeadings(doc);
        return new WorkingLink(url, depth, headings, links);
    }
}