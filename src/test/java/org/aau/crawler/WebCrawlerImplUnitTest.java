package org.aau.crawler;

import org.aau.config.DomainFilter;
import org.aau.config.WebCrawlerConfiguration;
import org.aau.crawler.analyzer.PageAnalyzer;
import org.aau.crawler.client.WebCrawlerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebCrawlerImplUnitTest {

    private static final String START_URL = "http://test.com";
    private static final int DEPTH = 1;
    ExecutorService executorServiceMock = mock(ExecutorService.class);
    WebCrawlerImpl webCrawler;

    @BeforeEach
    public void setup() {
        var domainFilter = new DomainFilter(Set.of(START_URL));
        var configuration = new WebCrawlerConfiguration(
                START_URL,
                DEPTH,
                1,
                domainFilter,
                ""
        );
        var webCrawlerImpl = new WebCrawlerImpl(configuration){
            @Override
            protected ExecutorService createExecutorService(int threadCount) {
                return executorServiceMock;
            }
        };
        webCrawler = spy(webCrawlerImpl);
    }

    @Test
    void startShouldThrowRuntimeExceptionOnError() throws Exception {
        var exception = new NullPointerException("Test exception");
       when(executorServiceMock.submit(any(Runnable.class))).thenThrow(exception);

        RuntimeException re = assertThrows(RuntimeException.class, () -> webCrawler.start());
        assertEquals(exception, re.getCause());
        verify(webCrawler).start();
    }

}
