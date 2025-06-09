package org.aau.crawler.analyzer;

import org.aau.crawler.parser.HtmlParser;
import org.aau.crawler.result.WorkingLink;
import org.jsoup.Jsoup;
import org.aau.crawler.parser.jsoupadapter.DocumentAdapter;

import java.util.Set;

public class PageAnalyzerImpl implements PageAnalyzer {

    private final HtmlParser parser;

    public PageAnalyzerImpl(HtmlParser parser) {
        this.parser = parser;
    }

    @Override
    public WorkingLink analyze(String url, int depth, String html) {
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html, url);
        org.aau.crawler.parser.jsoupadapter.Document doc = new DocumentAdapter(jsoupDoc);
        Set<String> links = parser.extractLinks(doc);
        Set<String> headings = parser.extractHeadings(doc);
        return new WorkingLink(url, depth, headings, links);
    }
}