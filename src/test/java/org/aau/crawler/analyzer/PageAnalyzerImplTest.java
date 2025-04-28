package org.aau.crawler.analyzer;

import org.aau.crawler.parser.HtmlParser;
import org.aau.crawler.result.WorkingLink;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PageAnalyzerImplTest {

    @Test
    void analyze_shouldReturnWorkingLinkWithExtractedData() {
        // Arrange
        HtmlParser parserMock = mock(HtmlParser.class);
        PageAnalyzerImpl analyzer = new PageAnalyzerImpl(parserMock);

        String url = "https://example.com";
        String html = "<html><body><h1>Title</h1><a href=\"/page\">Link</a></body></html>";
        Set<String> mockLinks = Set.of("https://example.com/page");
        Set<String> mockHeadings = Set.of("# Title");

        when(parserMock.extractLinks(any())).thenReturn(mockLinks);
        when(parserMock.extractHeadings(any())).thenReturn(mockHeadings);

        // Act
        WorkingLink result = analyzer.analyze(url, 1, html);

        // Assert
        assertEquals(url, result.getUrl());
        assertEquals(1, result.getDepth());
        assertEquals(mockLinks, result.getSubLinks());
        assertEquals(mockHeadings, result.getHeadings());

        verify(parserMock).extractLinks(any());
        verify(parserMock).extractHeadings(any());
    }
}
