package org.aau.config;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WebCrawlerConfigurationTest {

    @Test
    void testGetters() {
        DomainFilter filter = new DomainFilter(Set.of("example.com"));
        WebCrawlerConfiguration config = new WebCrawlerConfiguration(
                "http://example.com",
                3,
                5,
                filter,
                "/tmp/output"
        );

        assertEquals("http://example.com", config.startUrl());
        assertEquals(3, config.maximumDepth());
        assertEquals(5, config.threadCount());
        assertEquals(filter, config.domainFilter());
        assertEquals("/tmp/output", config.outputDir());
    }

    @Test
    void testIsAllowedDomainDelegation() {
        DomainFilter filter = new DomainFilter(Set.of("example.com"));
        WebCrawlerConfiguration config = new WebCrawlerConfiguration(
                "http://example.com",
                2,
                2,
                filter,
                "/output"
        );

        assertTrue(config.isAllowedDomain("http://sub.example.com"));
        assertFalse(config.isAllowedDomain("http://unauthorized.org"));
    }

    @Test
    void testToStringFormat() {
        DomainFilter filter = new DomainFilter(Set.of("example.com", "test.org"));
        WebCrawlerConfiguration config = new WebCrawlerConfiguration(
                "http://start.org",
                1,
                1,
                filter,
                "/data"
        );

        String output = config.toString();
        assertTrue(output.contains("WebCrawlerConfiguration[startUrl = http://start.org"));
        assertTrue(output.contains("maximumDepth = 1"));
        assertTrue(output.contains("threadCount = 1"));
        assertTrue(output.contains("allowedDomains = [example.com, test.org]") ||
                output.contains("allowedDomains = [test.org, example.com]")); // order is not guaranteed
        assertTrue(output.contains("outputDir = /data"));
    }
}
