package org.aau.crawler.error;

public record CrawlingError(String message, Throwable cause) {

    public String toMarkdownString() {
        return """
                **Message**: %s\s\s
                **Cause**: %s [%s]\s\s
                """.formatted(message, cause.getMessage(), cause.getClass().getSimpleName());
    }
}
