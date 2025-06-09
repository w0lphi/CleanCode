package org.aau.crawler.parser;


import org.aau.html.Document;
import org.aau.html.Element;
import org.aau.html.Elements;

import java.util.LinkedHashSet;
import java.util.Set;

public class HtmlParserImpl implements HtmlParser {

    private static final String HEADING_LEVEL_CHARACTER = "^";

    @Override
    public Set<String> extractHeadings(Document htmlDocument) {
        Elements headings = htmlDocument.select(":is(h1,h2,h3,h4,h5)");
        Set<String> headingSet = new LinkedHashSet<>();
        for (Element heading : headings) {
            String tagName = heading.tagName();
            int level = Integer.parseInt(tagName.substring(1));
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
            linkSet.add(link.attr("abs:href"));
        }
        return linkSet;
    }
}
