package org.aau.crawler;

import org.aau.crawler.analyzer.PageAnalyzer;
import org.aau.crawler.client.WebCrawlerClient;
import org.aau.crawler.client.WebCrawlerClientImpl;
import org.aau.crawler.result.BrokenLink;
import org.aau.crawler.result.Link;
import org.aau.crawler.result.WorkingLink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.TimeoutException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebCrawlerImplUnitTest {

    private static final String START_URL = "http://test.com";
    private static final int DEPTH = 1;
    WebCrawlerClient webCrawlerClientMock;
    PageAnalyzer pageAnalyzerMock;
    WebCrawlerImpl webCrawler;

    @BeforeEach
    public void setup() {
        webCrawlerClientMock = mock(WebCrawlerClient.class);
        pageAnalyzerMock = mock(PageAnalyzer.class);
        var webCrawlerImpl = new WebCrawlerImpl(START_URL, DEPTH, webCrawlerClientMock, pageAnalyzerMock);
        webCrawler = spy(webCrawlerImpl);
    }

    @Test
    void startShouldThrowRuntimeExceptionOnError() throws Exception {
        var exception = new Exception("Test exception");
        when(webCrawlerClientMock.isPageAvailable(anyString())).thenReturn(false);
        doThrow(exception).when(webCrawlerClientMock).close();

        RuntimeException re = assertThrows(RuntimeException.class, () -> webCrawler.start());
        assertEquals(exception, re.getCause());
        verify(webCrawler).crawlLinkRecursively(START_URL, 0);
    }

    @Test
    void crawlLinkRecursivelyShouldAddBrokenLinkOnException() {
        when(webCrawlerClientMock.isPageAvailable(anyString())).thenReturn(true);
        doThrow(new TimeoutException("Test exception")).when(webCrawlerClientMock).getPageContent(anyString());

        assertDoesNotThrow(() -> webCrawler.crawlLinkRecursively(START_URL, 0));
        assertEquals(1, webCrawler.getCrawledLinks().size());
        Link brokenLink = webCrawler.getCrawledLinks().iterator().next();
        assertEquals(BrokenLink.class, brokenLink.getClass());
        assertEquals(START_URL, brokenLink.getUrl());
    }

    @Test
    void crawlLinkRecursivelyShouldAddBrokenLinkForUnavailablePage(){
        when(webCrawlerClientMock.isPageAvailable(anyString())).thenReturn(false);

        webCrawler.crawlLinkRecursively(START_URL, 0);
        assertEquals(1, webCrawler.getCrawledLinks().size());
        Link brokenLink = webCrawler.getCrawledLinks().iterator().next();
        assertEquals(BrokenLink.class, brokenLink.getClass());
        assertEquals(START_URL, brokenLink.getUrl());
    }


    @Test
    void crawlLinkRecursivelyShouldAddWorkingLinkForWorkingPages(){
        Set<String> headings = new LinkedHashSet<>(List.of("Heading1", "Heading2", "Heading3"));
        WorkingLink expectedWorkingLink = new WorkingLink(START_URL, 0, headings, Set.of());
        String html = "<html></html>";
        when(webCrawlerClientMock.isPageAvailable(anyString())).thenReturn(true);
        when(webCrawlerClientMock.getPageContent(anyString())).thenReturn(html);
        when(pageAnalyzerMock.analyze(START_URL, 0, html)).thenReturn(expectedWorkingLink);

        webCrawler.crawlLinkRecursively(START_URL, 0);
        assertEquals(1, webCrawler.getCrawledLinks().size());
        Link workingLink = webCrawler.getCrawledLinks().iterator().next();
        assertEquals(expectedWorkingLink, workingLink);
        assertEquals(START_URL, workingLink.getUrl());
        assertEquals(0, workingLink.getDepth());
    }

    @Test
    void crawlLinkRecursivelyShouldAddLinkForAllSubPages(){

        BrokenLink brokenSublink = new BrokenLink(START_URL + "/broken", DEPTH);
        WorkingLink workingSublink = new WorkingLink(START_URL + "/working", DEPTH, Set.of(), Set.of());

        Set<String> headings = new LinkedHashSet<>(List.of("Heading1", "Heading2", "Heading3"));
        Set<String> sublinks = new LinkedHashSet<>(List.of(brokenSublink.getUrl(), workingSublink.getUrl()));
        WorkingLink parentLink = new WorkingLink(START_URL, 0, headings, sublinks);
        String html = "<html></html>";
        when(webCrawlerClientMock.isPageAvailable(parentLink.getUrl())).thenReturn(true);
        when(webCrawlerClientMock.isPageAvailable(workingSublink.getUrl())).thenReturn(true);
        when(webCrawlerClientMock.isPageAvailable(brokenSublink.getUrl())).thenReturn(false);
        when(webCrawlerClientMock.getPageContent(anyString())).thenReturn(html);
        when(pageAnalyzerMock.analyze(parentLink.getUrl(), parentLink.getDepth(), html)).thenReturn(parentLink);
        when(pageAnalyzerMock.analyze(workingSublink.getUrl(), workingSublink.getDepth(), html)).thenReturn(workingSublink);

        webCrawler.crawlLinkRecursively(START_URL, 0);

        assertEquals(3, webCrawler.getCrawledLinks().size());
        List<Link> crawledLinks = webCrawler.getCrawledLinks().stream().toList();
        assertEquals(parentLink, crawledLinks.getFirst());
        assertEquals(parentLink.getDepth(), crawledLinks.getFirst().getDepth());
        assertEquals(parentLink.getSubLinks(), ((WorkingLink)crawledLinks.getFirst()).getSubLinks());
        assertEquals(parentLink.getHeadings(), ((WorkingLink)crawledLinks.getFirst()).getHeadings());

        assertEquals(brokenSublink, crawledLinks.get(1));
        assertEquals(brokenSublink.getDepth(), crawledLinks.get(1).getDepth());

        assertEquals(workingSublink, crawledLinks.getLast());
        assertEquals(workingSublink.getDepth(), crawledLinks.getLast().getDepth());
        assertEquals(workingSublink.getSubLinks(), ((WorkingLink)crawledLinks.getLast()).getSubLinks());
        assertEquals(workingSublink.getHeadings(), ((WorkingLink)crawledLinks.getLast()).getHeadings());

        verify(webCrawler, times(3)).crawlLinkRecursively(anyString(), anyInt());
        verify(webCrawlerClientMock, times(3)).isPageAvailable(anyString());
        verify(pageAnalyzerMock, times(2)).analyze(anyString(), anyInt(), anyString());
    }

    @Test
    void isAlreadyCrawledUrlShouldReturnTrueForAlreadyCrawledUrl(){
        Link link = new WorkingLink(START_URL, DEPTH, Set.of(), Set.of());
        when(webCrawler.getCrawledLinks()).thenReturn(Set.of(link));
        assertTrue(webCrawler.isAlreadyCrawledUrl(link.getUrl()));
    }



}
