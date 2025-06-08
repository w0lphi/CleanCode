package org.aau.crawler;

import org.aau.config.DomainFilter;
import org.aau.config.WebCrawlerConfiguration;
import org.aau.crawler.analyzer.PageAnalyzer;
import org.aau.crawler.client.WebCrawlerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
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
        var domainFilter = new DomainFilter(Set.of(START_URL));
        var configuration = new WebCrawlerConfiguration(
                START_URL,
                DEPTH,
                1,
                domainFilter,
                ""
        );
        var webCrawlerImpl = new WebCrawlerImpl(configuration, webCrawlerClientMock, pageAnalyzerMock);
        webCrawler = spy(webCrawlerImpl);
    }

    @Test
    void startShouldThrowRuntimeExceptionOnError() throws Exception {
        var exception = new Exception("Test exception");
        when(webCrawlerClientMock.isPageAvailable(anyString())).thenReturn(false);
        doThrow(exception).when(webCrawlerClientMock).close();

        RuntimeException re = assertThrows(RuntimeException.class, () -> webCrawler.start());
        assertEquals(exception, re.getCause());
        verify(webCrawler).start();
    }

}
