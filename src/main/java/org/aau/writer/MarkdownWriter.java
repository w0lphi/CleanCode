package org.aau.writer;

import org.aau.crawler.result.Link;

import java.io.IOException;
import java.nio.file.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MarkdownWriter {

    private final String outputDir;
    private final LinkFormatter formatter;

    public MarkdownWriter(String outputDir, LinkFormatter formatter) {
        this.outputDir = outputDir;
        this.formatter = formatter;
    }

    public Path writeResultsToFile(Set<Link> links, OffsetDateTime timestamp) throws IOException {
        String filename = "report-" + timestamp.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".md";
        Path path = Paths.get(outputDir, filename);
        List<String> lines = new ArrayList<>();
        lines.add("# Crawl Results\n");
        lines.add("Timestamp: " + timestamp + "\n");
        links.stream().map(formatter::format).forEach(lines::add);
        Files.createDirectories(path.getParent());
        return Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
