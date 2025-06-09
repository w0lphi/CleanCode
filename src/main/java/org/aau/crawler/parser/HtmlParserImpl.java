package org.aau.crawler.parser;

import org.aau.crawler.parser.jsoupadapter.Document;
import org.aau.crawler.parser.jsoupadapter.DocumentAdapter;
import org.aau.crawler.parser.jsoupadapter.Element;
import org.aau.crawler.parser.jsoupadapter.Elements;
import org.jsoup.Jsoup;

import java.util.LinkedHashSet;
import java.util.Set;

public class HtmlParserImpl implements HtmlParser {

    private static final String HEADING_LEVEL_CHARACTER = "^";

    public Document parse(String html) {
        return new DocumentAdapter(Jsoup.parse(html));
    }

    @Override
    public Set<String> extractHeadings(Document htmlDocument) {
        Elements headings = htmlDocument.select(":is(h1,h2,h3,h4,h5)");
        Set<String> headingSet = new LinkedHashSet<>();
        for (Element heading : headings) {
            String tagName = heading.attr("tagName"); // optional
            int level = Integer.parseInt(heading.tagName().substring(1)); // alternativ: extract h1-h5
            String headingLevel = HEADING_LEVEL_CHARACTER.repeat(level);
            headingSet.add(headingLevel + " " + heading.text());
        }
        return headingSet;
    }

    @Override
    public Set<String> extractLinks(Document htmlDocument) {
        Elements links = htmlDocument.select("a[href]");
        Set<String> linkSet = new LinkedHashSet<>();
        for (Element link : links) {
            linkSet.add(link.attr("href"));
        }
        return linkSet;
    }
}
