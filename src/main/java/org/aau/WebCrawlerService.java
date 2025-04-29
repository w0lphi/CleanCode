package org.aau;

import org.aau.runner.WebCrawlerRunner;

import java.nio.file.Path;
import java.util.Scanner;

public class WebCrawlerService {

    public static void main(String[] args) {
        String startUrl = args[0];
        int maximumDepth;
        try {
            maximumDepth = Integer.parseInt(args[1]);
            if (maximumDepth < 0) {
                throw new NumberFormatException("Maximum depth cannot be negative");
            }
        } catch (NumberFormatException e) {
            System.err.println("Input is not a valid number!");
            return;
        }
        System.out.printf("Starting crawler: startUrl=%s, maximumDepth=%d%n", startUrl, maximumDepth);
        try {
            var runner = new WebCrawlerRunner(startUrl, maximumDepth, "build");
            Path result = runner.executeCrawl();
            System.out.printf("Crawl finished. Report available at: %s\n", result.toAbsolutePath());
        } catch (Exception e) {
            System.err.printf("Crawl failed: %s\n", e.getMessage());
        }
    }
}
