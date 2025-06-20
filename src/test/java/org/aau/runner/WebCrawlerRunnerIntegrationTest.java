package org.aau.runner;

import org.aau.config.DomainFilter;
import org.aau.config.WebCrawlerConfiguration;
import org.aau.crawler.WebCrawler;
import org.aau.writer.MarkdownWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(MockServerExtension.class)
public class WebCrawlerRunnerIntegrationTest {

    private static final String TEST_OUTPUT_DIR = "build/test";
    MockServerClient client;
    WebCrawlerRunner webCrawlerRunner;
    String mockServerUrl;
    WebCrawler webCrawler;
    MarkdownWriter writer;

    @BeforeEach
    void setup(MockServerClient client) {
        this.client = client;
        this.mockServerUrl = "http://localhost:%d".formatted(client.getPort());
        var domainFilter = new DomainFilter(Set.of(mockServerUrl));
        var configuration = new WebCrawlerConfiguration(
                mockServerUrl,
                2,
                1,
                domainFilter,
                TEST_OUTPUT_DIR
        );

        this.webCrawlerRunner = new WebCrawlerRunner(configuration) {
            @Override
            protected WebCrawler createCrawler(WebCrawlerConfiguration configuration) {
                webCrawler = spy(super.createCrawler(configuration));
                return webCrawler;
            }

            @Override
            protected MarkdownWriter createMarkdownWriter(String outputDir) {
                writer = spy(super.createMarkdownWriter(outputDir));
                return writer;
            }
        };
    }

    @Test
    void testRun() throws IOException, InterruptedException {
        String workingPath = "/a-working-path/" + UUID.randomUUID();
        String brokenPath = "/broken-path/" + UUID.randomUUID();
        String h1Page1 = "TestHeading1-" + UUID.randomUUID();
        String htmlPage1 = """
                <html>
                    <head>
                        <title>Test Page Level 1</title>
                    </head>
                    <body>
                        <h1>%s</h1>
                        <a href="%s">working link</a>
                        <a href="%s">duplicate link</a>
                        <a href="%s">broken link</a>
                    </body>
                </html>
                
                """.formatted(
                h1Page1,
                mockServerUrl + workingPath,
                mockServerUrl + workingPath,
                mockServerUrl + brokenPath);


        String h1Page2 = "TestHeading2-" + UUID.randomUUID();
        String h2Page2 = "TestHeading3-" + UUID.randomUUID();
        String h3Page2 = "TestHeading4-" + UUID.randomUUID();
        String htmlPage2 = """
                <html>
                    <head>
                        <title>Test Page Level 2</title>
                    </head>
                    <body>
                        <h1>%s</h1>
                        <h2>%s</h2>
                        <h3>%s</h3>
                    </body>
                </html>
                
                """.formatted(h1Page2, h2Page2, h3Page2);

        client.when(request().withMethod("HEAD").withPath("/")).respond(response().withStatusCode(200));
        client.when(request().withMethod("HEAD").withPath(workingPath)).respond(response().withStatusCode(200));
        client.when(request().withMethod("HEAD").withPath(brokenPath)).respond(response().withStatusCode(400));

        client.when(
                request()
                        .withPath("/")
                        .withMethod("GET")
        ).respond(
                response()
                        .withStatusCode(200)
                        .withBody(htmlPage1)
        );
        client.when(
                request()
                        .withPath(workingPath)
                        .withMethod("GET")
        ).respond(
                response()
                        .withStatusCode(200)
                        .withBody(htmlPage2)
        );

        Path filepath = webCrawlerRunner.run();

        webCrawler.awaitCompletion();

        assertNotNull(filepath);
        verify(webCrawler).start();
        verify(writer).writeResultsToFile(anySet(), anyList(), any(OffsetDateTime.class));
        verify(webCrawler, times(1)).getCrawledLinks();

        List<String> expectedLines = List.of(
                "# Crawl Results",
                "",
                "Timestamp: \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}",
                "",
                "## %s".formatted(mockServerUrl),
                "Depth: 0",
                "### Headings",
                "^ %s".formatted(h1Page1),
                "",
                "## %s".formatted(mockServerUrl + workingPath),
                "Depth: 1",
                "### Headings",
                "^ %s\s\s".formatted(h1Page2),
                "^^ %s\s\s".formatted(h2Page2),
                "^^^ %s".formatted(h3Page2),
                "",
                "## %s (broken)".formatted(mockServerUrl + brokenPath),
                "Depth: 1",
                ""
        );

        List<String> actualLines = Files.readAllLines(filepath, Charset.defaultCharset());
        assertLinesMatch(expectedLines, actualLines);
    }

    @AfterEach
    void teardown() throws IOException {
        Path path = Paths.get(TEST_OUTPUT_DIR);
        if (Files.exists(path)) {
            try (var paths = Files.walk(path)) {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) {
                                throw new RuntimeException("Error when deleting file: path = %s".formatted(p), e);
                            }
                        });
            }
        }
    }
}
