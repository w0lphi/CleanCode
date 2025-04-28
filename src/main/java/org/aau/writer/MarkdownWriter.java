package org.aau.writer;

import org.aau.crawler.result.Link;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MarkdownWriter {

    private final String outputDir;

    public MarkdownWriter(String outputDir) {
        this.outputDir = outputDir;
    }

    public Path writeResultsToFile(Set<Link> links, OffsetDateTime timestamp) throws IOException {
        String filename = "report-" + timestamp.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".md";
        Path path = Paths.get(outputDir, filename);
        List<String> lines = new ArrayList<>();
        lines.add("# Crawl Results\n");
        lines.add("Timestamp: " + timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
        links.stream().map(Link::toMarkdownString).forEach(lines::add);
        Files.createDirectories(path.getParent());
        return Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
