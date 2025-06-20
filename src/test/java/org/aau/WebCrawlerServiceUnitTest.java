package org.aau;

import org.aau.config.DomainFilter;
import org.aau.config.WebCrawlerConfiguration;
import org.aau.runner.WebCrawlerRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebCrawlerServiceUnitTest {

    WebCrawlerService webCrawlerService;
    WebCrawlerRunner webCrawlerRunnerMock;

    @BeforeEach
    void setup() {
        webCrawlerRunnerMock = mock(WebCrawlerRunner.class);

        var domainFilter = new DomainFilter(Set.of("http://example.com"));
        var configuration = new WebCrawlerConfiguration(
                "http://example.com",
                1,
                1,
                domainFilter,
                ""
        );

        webCrawlerService = new WebCrawlerService(configuration) {
            @Override
            protected WebCrawlerRunner createWebCrawlerRunner(WebCrawlerConfiguration webCrawlerConfiguration) {
                return webCrawlerRunnerMock;
            }
        };
    }

    @Test
    void mainShouldThrowExceptionIfMaximumDepthIsNull() {
        String[] args = new String[2];
        args[0] = "http://example.com";
        args[1] = null;

        assertThrows(NumberFormatException.class, () -> WebCrawlerService.main(args));
    }

    @Test
    void mainShouldThrowExceptionIfMaximumDepthIsNegative() {
        String[] args = new String[2];
        args[0] = "http://example.com";
        args[1] = "-1";

        assertThrows(NumberFormatException.class, () -> WebCrawlerService.main(args));
    }

    @Test
    void mainShouldThrowExceptionIfStartUrlIsNull() {
        String[] args = new String[2];
        args[0] = null;
        args[1] = "0";

        assertThrows(IllegalArgumentException.class, () -> WebCrawlerService.main(args));
    }

    @Test
    void mainShouldThrowExceptionIfStartUrlIsEmpty() {
        String[] args = new String[2];
        args[0] = "";
        args[1] = "0";
        assertThrows(IllegalArgumentException.class, () -> WebCrawlerService.main(args));
    }

    @Test
    void mainShouldThrowExceptionIfThreadCountIsLessThanOne() {
        String[] args = new String[3];
        args[0] = "http://example.com";
        args[1] = "1";
        args[2] = "0";

        assertThrows(NumberFormatException.class, () -> WebCrawlerService.main(args));
    }

    @Test
    void mainShouldThrowExceptionIfThreadCountIsNaN() {
        String[] args = new String[3];
        args[0] = "http://example.com";
        args[1] = "1";
        args[2] = "null";

        assertThrows(NumberFormatException.class, () -> WebCrawlerService.main(args));
    }

    @Test
    void executeShouldThrowExceptionIfCrawlFails() {
        RuntimeException exception = new RuntimeException("Test exception");
        when(webCrawlerRunnerMock.run()).thenThrow(exception);

        var actualException = assertThrows(RuntimeException.class, () -> webCrawlerService.execute());
        assertEquals(exception, actualException.getCause());
    }

    @Test
    void executeShouldExecuteWebCrawlerRunner() {
        when(webCrawlerRunnerMock.run()).thenReturn(Path.of("build"));
        webCrawlerService.execute();
        verify(webCrawlerRunnerMock).run();
    }

}
