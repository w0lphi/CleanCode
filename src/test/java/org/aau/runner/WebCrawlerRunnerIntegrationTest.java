package org.aau.runner;

import org.aau.crawler.WebCrawler;
import org.aau.writer.MarkdownWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.spy;
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
        this.webCrawlerRunner = new WebCrawlerRunner(mockServerUrl, 2, TEST_OUTPUT_DIR) {
            @Override
            protected WebCrawler createCrawler(String startUrl, int maximumDepth) {
                webCrawler = spy(super.createCrawler(startUrl, maximumDepth));
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
    void testExecuteCrawl() throws IOException {
        String workingPath = "/working-path";
        String brokenPath = "/broken-path";
        String h1Page1 = "TestHeading1";
        String htmlPage1 = """
                <html>
                    <head>
                        <title>Test Page Level 1</title>
                    </head>
                    <body>
                        <h1>%s</h1>
                        <a href="%s">working link</a>
                        <a href="%s">broken link</a>
                    </body>
                </html>
                
                """.formatted(h1Page1, mockServerUrl + workingPath, mockServerUrl + brokenPath);


        String h1Page2 = "TestHeading2";
        String h2Page2 = "TestHeading3";
        String h3Page2 = "TestHeading4";
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

        Path filepath = webCrawlerRunner.executeCrawl();

        assertNotNull(filepath);
        verify(webCrawler).start();
        verify(writer).writeResultsToFile(anySet(), any(OffsetDateTime.class));
        verify(webCrawler).getCrawledLinks();

        List<String> expectedLines = List.of(
                "# Crawl Results",
                "",
                "Timestamp: \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}",
                "",
                "## %s".formatted(mockServerUrl),
                "Depth: 0",
                "### Headings",
                "# TestHeading1",
                "",
                "## %s".formatted(mockServerUrl + workingPath),
                "Depth: 1",
                "### Headings",
                "# TestHeading2",
                "## TestHeading3",
                "### TestHeading4",
                "",
                "## %s (broken)".formatted(mockServerUrl + brokenPath),
                "Depth: 1",
                ""
        );

        List<String> actualLines = Files.readAllLines(filepath, Charset.defaultCharset());
        assertLinesMatch(expectedLines, actualLines);
    }
}
