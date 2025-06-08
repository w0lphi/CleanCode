package org.aau.config;

public record WebCrawlerConfiguration(
        String startUrl,
        int maximumDepth,
        int threadCount,
        DomainFilter domainFilter,
        String outputDir
) {

    public boolean isAllowedDomain(String url) {
        return domainFilter().isAllowedDomain(url);
    }

    @Override
    public String toString() {
        return "%s[startUrl = %s, maximumDepth = %s, threadCount = %s, allowedDomains = [%s], outputDir = %s]%n".formatted(
                this.getClass().getSimpleName(),
                startUrl,
                maximumDepth,
                threadCount,
                String.join(", ", domainFilter.getAllowedDomains()),
                outputDir);
    }
}
