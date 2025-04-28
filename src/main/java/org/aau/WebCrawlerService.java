package org.aau;

import org.aau.runner.WebCrawlerRunner;

import java.nio.file.Path;
import java.util.Scanner;

public class WebCrawlerService {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("---- Web Crawler started ----");
            System.out.println("Please enter URL to crawl: ");
            String startUrl = scanner.nextLine();
            Integer maximumDepth = null;
            while (maximumDepth == null) {
                try {
                    System.out.println("Please enter maximum depth to crawl: ");
                    maximumDepth = Integer.parseInt(scanner.nextLine());
                    if (maximumDepth < 0) {
                        throw new NumberFormatException("Maximum depth cannot be negative");
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Input is not a valid number, please try again!");
                }
            }

            try {
                var runner = new WebCrawlerRunner(startUrl, maximumDepth, "build");
                System.out.println("Starting web crawler ...");
                Path result = runner.executeCrawl();
                System.out.printf("Crawl finished. Report available at: %s\n", result.toAbsolutePath());
            } catch (Exception e) {
                System.err.printf("Crawl failed: %s\n", e.getMessage());
            }

        }
    }
}
