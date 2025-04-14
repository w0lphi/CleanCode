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

    private final String pathString;

    public MarkdownWriter(String path) {
        this.pathString = path;
    }

    public void writeResultsToFile(Set<Link> links) throws IOException {
        Path path = Paths.get(pathString);
        List<String> lines = new ArrayList<>();
        lines.add("# Crawl Results\n");
        lines.add("Results from %s".formatted(OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));
        lines.addAll(links.stream().map(Link::toString).toList());
        Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
