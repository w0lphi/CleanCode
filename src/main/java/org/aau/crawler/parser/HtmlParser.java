package org.aau.crawler.parser;

import java.util.Set;

public interface HtmlParser {

    Set<String> extractHeadings(org.aau.crawler.parser.jsoupadapter.Document htmlDocument);
    Set<String> extractLinks(org.aau.crawler.parser.jsoupadapter.Document htmlDocument);
}
