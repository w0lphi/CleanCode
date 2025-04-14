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

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final String pathString;

    public MarkdownWriter(String path) {
        this.pathString = path;
    }

    public Path writeResultsToFile(Set<Link> links, OffsetDateTime timestamp) throws IOException {
        Path path = Paths.get(pathString);
        List<String> lines = new ArrayList<>();
        lines.add("# Crawl Results\n");
        lines.add("Results from %s\n".formatted(timestamp.format(DATE_TIME_FORMATTER)));
        lines.addAll(links.stream().map(Link::toString).toList());
        return Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
