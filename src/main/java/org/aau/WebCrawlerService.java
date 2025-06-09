package org.aau;

import org.aau.config.DomainFilter;
import org.aau.config.WebCrawlerConfiguration;
import org.aau.runner.WebCrawlerRunner;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WebCrawlerService {


    private static final String DEFAULT_OUTPUT_DIR = "build";
    private final WebCrawlerRunner webCrawlerRunner;

    public WebCrawlerService(WebCrawlerConfiguration configuration) {
        this.webCrawlerRunner = createWebCrawlerRunner(configuration);
    }

    public static void main(String[] args) {
        String startUrl = args[0];
        if (startUrl == null || startUrl.isEmpty()) {
            throw new IllegalArgumentException("Starting URL must not be empty");
        }

        int maximumDepth;
        try {
            maximumDepth = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.printf("Error when parsing maximum depth: %s\n", e.getMessage());
            throw new NumberFormatException("Maximum depth is not a valid number!");
        }

        if (maximumDepth < 0) {
            throw new NumberFormatException("Maximum depth cannot be negative");
        }

        int threadCount = 1;
        if (args.length > 2) {
            try {
                threadCount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                System.err.printf("Error when parsing thread count: %s\n", e.getMessage());
                throw new NumberFormatException("Thead count is not a valid number!");
            }
        }

        if (threadCount < 1) {
            throw new NumberFormatException("Thread count cannot be less than 1");
        }

        Set<String> allowedDomains = new HashSet<>();
        allowedDomains.add(startUrl);

        if (args.length > 3) {
            allowedDomains.addAll(List.of(args[3].split(",")));
        }

        DomainFilter domainFilter = new DomainFilter(allowedDomains);

        String outputDir = DEFAULT_OUTPUT_DIR;
        if (args.length > 4) {
            String subFolder = args[4];
            outputDir = String.join("/", outputDir, subFolder);
        }

        WebCrawlerConfiguration configuration = new WebCrawlerConfiguration(startUrl, maximumDepth, threadCount, domainFilter, outputDir);

        System.out.printf("Starting crawler: configuration=%s", configuration);
        new WebCrawlerService(configuration).execute();
    }

    protected WebCrawlerRunner createWebCrawlerRunner(WebCrawlerConfiguration configuration) {
        return new WebCrawlerRunner(configuration);
    }

    protected void execute() {
        try {
            Path result = webCrawlerRunner.run();
            System.out.printf("Crawl finished. Report available at: %s\n", result.toAbsolutePath());
        } catch (Exception e) {
            System.err.printf("Error while crawling: %s\n", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
