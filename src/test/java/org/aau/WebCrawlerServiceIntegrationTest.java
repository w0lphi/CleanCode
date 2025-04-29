package org.aau;

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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(MockServerExtension.class)
public class WebCrawlerServiceIntegrationTest {

    private static final String TEST_OUTPUT_SUB_DIR = "crawler-service-test";
    Path testDirectory;
    MockServerClient client;
    String mockServerUrl;

    @BeforeEach
    void setup(MockServerClient client) {
        this.client = client;
        this.mockServerUrl = "http://localhost:%d".formatted(client.getPort());
        String buildDir = System.getProperty("project.buildDir", "build"); // Fallback: "build"
        String buildDirAbsolutPath = Path.of(buildDir).toAbsolutePath().toString();
        this.testDirectory = Path.of(buildDirAbsolutPath, TEST_OUTPUT_SUB_DIR);
    }

    @Test
    void testMain() throws IOException {
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

        String[] args = new String[]{mockServerUrl, "2", TEST_OUTPUT_SUB_DIR};
        WebCrawlerService.main(args);

        List<String> expectedLines = List.of(
                "# Crawl Results",
                "",
                "Timestamp: \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}",
                "",
                "## %s".formatted(mockServerUrl),
                "Depth: 0",
                "### Headings",
                "+ %s".formatted(h1Page1),
                "",
                "## %s".formatted(mockServerUrl + workingPath),
                "Depth: 1",
                "### Headings",
                "+ %s\s\s".formatted(h1Page2),
                "++ %s\s\s".formatted(h2Page2),
                "+++ %s".formatted(h3Page2),
                "",
                "## %s (broken)".formatted(mockServerUrl + brokenPath),
                "Depth: 1",
                ""
        );


        try (Stream<Path> files = Files.list(testDirectory)) {
            Path filepath = files
                    .filter(Files::isRegularFile).min(Comparator.comparing(path -> path.getFileName().toString()))
                    .orElseThrow(() -> new RuntimeException("No files found."));

            List<String> actualLines = Files.readAllLines(filepath, Charset.defaultCharset());
            assertLinesMatch(expectedLines, actualLines);
        }
    }

    @AfterEach
    void teardown() throws IOException {
        if (Files.exists(testDirectory)) {
            try (var paths = Files.walk(testDirectory)) {
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
