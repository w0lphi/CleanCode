package org.aau;

import org.aau.runner.WebCrawlerRunner;

import java.nio.file.Path;

public class WebCrawlerService {


    private static final String DEFAULT_OUTPUT_DIR = "build";
    private final WebCrawlerRunner webCrawlerRunner;

    public WebCrawlerService(String startUrl, int maximumDepth) {
        this(startUrl, maximumDepth, DEFAULT_OUTPUT_DIR);
    }

    public WebCrawlerService(String startUrl, int maximumDepth, String outputDir) {
        this.webCrawlerRunner = createWebCrawlerRunner(startUrl, maximumDepth, outputDir);
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
            throw new NumberFormatException("Input is not a valid number!");
        }

        if (maximumDepth < 0) {
            throw new NumberFormatException("Maximum depth cannot be negative");
        }

        String outputDir = DEFAULT_OUTPUT_DIR;
        if (args.length > 2) {
            String subFolder = args[2];
            outputDir = String.join("/", outputDir, subFolder);
        }

        System.out.printf("Starting crawler: startUrl=%s, maximumDepth=%d%n", startUrl, maximumDepth);
        new WebCrawlerService(startUrl, maximumDepth, outputDir).execute();
    }

    protected WebCrawlerRunner createWebCrawlerRunner(String startUrl, int maximumDepth, String outputDir) {
        return new WebCrawlerRunner(startUrl, maximumDepth, outputDir);
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
