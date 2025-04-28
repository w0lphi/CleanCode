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
                } catch (NumberFormatException e) {
                    System.err.println("Input is not a number, please try again!");
                }
            }

            try {
                var runner = new WebCrawlerRunner(startUrl, maximumDepth, "build");
                Path result = runner.executeCrawl();
                System.out.printf("Crawl finished. Report available at: %s\n", result.toAbsolutePath());
            } catch (Exception e) {
                System.err.printf("Crawl failed: %s\n", e.getMessage());
            }

        }
    }
}
