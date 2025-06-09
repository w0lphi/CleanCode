package org.aau.crawler;

import org.aau.config.DomainFilter;
import org.aau.config.WebCrawlerConfiguration;
import org.aau.crawler.error.CrawlingError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
        var webCrawlerImpl = new WebCrawlerImpl(configuration) {
            @Override
            protected ExecutorService createExecutorService(int threadCount) {
                return executorServiceMock;
            }
        };
        webCrawler = spy(webCrawlerImpl);
    }

    @Test
    void startShouldReportErrorOnRuntimeException() {
        var exception = new NullPointerException("Test exception");
        when(executorServiceMock.submit(any(Runnable.class))).thenThrow(exception);

        assertDoesNotThrow(() -> webCrawler.start());

        List<CrawlingError> errors = webCrawler.getErrors();
        assertEquals(1, errors.size());
        assertEquals(exception, errors.getFirst().cause());
        verify(webCrawler).shutdownExecutor();
    }

    @Test
    void startShouldReportErrorOnInterruptedException() throws InterruptedException {
        var exception = new InterruptedException("Test exception");
        doThrow(exception).when(webCrawler).awaitCompletion();

        assertDoesNotThrow(() -> webCrawler.start());

        assertTrue(Thread.currentThread().isInterrupted());
        List<CrawlingError> errors = webCrawler.getErrors();
        assertEquals(1, errors.size());
        assertEquals(exception, errors.getFirst().cause());
        verify(webCrawler).shutdownExecutor();
    }

    @Test
    void shutdownExecutorShouldReportErrorOnInterruptedException() throws InterruptedException {
        var exception = new InterruptedException("Test exception");
        when(executorServiceMock.awaitTermination(anyLong(), any(TimeUnit.class))).thenThrow(exception);

        assertDoesNotThrow(() -> webCrawler.shutdownExecutor());

        assertTrue(Thread.currentThread().isInterrupted());
        List<CrawlingError> errors = webCrawler.getErrors();
        assertEquals(1, errors.size());
        assertEquals(exception, errors.getFirst().cause());
        verify(executorServiceMock).shutdownNow();
    }

}
