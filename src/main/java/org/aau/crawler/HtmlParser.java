package org.aau.crawler;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

public class HtmlParser {

    public Set<String> extractHeadings(Document htmlDocument) {
        Elements headings = htmlDocument.select(":is(h1,h2,h3,h4,h5)");
        Set<String> headingSet = new HashSet<>();
        for (Element heading : headings) {
            String tagName = heading.tagName();
            int level = Integer.parseInt(tagName.substring(1));
            String hashtags = "#".repeat(level);
            headingSet.add(hashtags + " " + heading.text());
        }
        return headingSet;
    }

    public Set<String> extractLinks(Document htmlDocument) {
        Elements links = htmlDocument.select("a[href]");
        Set<String> urls = new HashSet<>();
        for (Element link : links) {
            urls.add(link.attr("abs:href"));
        }
        return urls;
    }
}
