package org.aau;

import org.aau.crawler.WebCrawler;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("---- Web Crawler started ----");
            System.out.println("Pleaser enter URL to crawl: ");
            String startUrl = scanner.nextLine();
            Integer maximumDepth = null;
            while (maximumDepth == null) {
                try {
                    System.out.println("Pleaser enter maximum depth to crawl: ");
                    maximumDepth = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.err.println("Input is not a number, please try again!");
                }
            }

            WebCrawler crawler = new WebCrawler(startUrl, maximumDepth);
            crawler.start();
            System.out.println("---- Web Crawler finished -----");
            crawler.getCrawlResults().forEach(System.out::println);
        }
    }

}
