package org.aau;

import org.aau.crawler.WebCrawler;
import org.aau.writer.MarkdownWriter;

import java.io.IOException;
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

            WebCrawler crawler = new WebCrawler(startUrl, maximumDepth);
            crawler.start();
            System.out.println("Web Crawler finished!");

            MarkdownWriter writer = new MarkdownWriter("build/report.md");
            try {
                Path filepath = writer.writeResultsToFile(crawler.getCrawledLinks());
                System.out.printf("Web Crawler Results available at: %s\n", filepath.toAbsolutePath());
            } catch (IOException e) {
                System.err.printf("Error writing results to file: error=%s\n", e.getMessage());
            }

        }
    }
}
