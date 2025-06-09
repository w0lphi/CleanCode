package org.aau.crawler.parser;

import org.aau.html.Document;

import java.util.Set;

public interface HtmlParser {

    Set<String> extractHeadings(Document htmlDocument);

    Set<String> extractLinks(Document htmlDocument);
}
