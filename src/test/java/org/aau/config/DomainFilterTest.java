package org.aau.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DomainFilterTest {

    private DomainFilter domainFilter;

    @BeforeEach
    void setUp() {
        domainFilter = new DomainFilter(Set.of("example.com", "test.org"));
    }

    @Test
    void testAllowedDomainExactMatch() {
        assertTrue(domainFilter.isAllowedDomain("http://example.com"));
        assertTrue(domainFilter.isAllowedDomain("https://test.org"));
    }

    @Test
    void testAllowedDomainWithSubdomain() {
        assertTrue(domainFilter.isAllowedDomain("http://sub.example.com"));
        assertTrue(domainFilter.isAllowedDomain("https://foo.test.org"));
    }

    @Test
    void testDisallowedDomain() {
        assertFalse(domainFilter.isAllowedDomain("http://unauthorized.com"));
        assertFalse(domainFilter.isAllowedDomain("https://other.org"));
    }

    @Test
    void testEmptyAllowedDomainsAllowsAll() {
        DomainFilter openFilter = new DomainFilter(Collections.emptySet());
        assertTrue(openFilter.isAllowedDomain("http://anydomain.com"));
    }

    @Test
    void testNullOrInvalidUrl() {
        assertFalse(domainFilter.isAllowedDomain(null));
        assertFalse(domainFilter.isAllowedDomain(""));
        assertFalse(domainFilter.isAllowedDomain("not a url"));
    }

    @Test
    void testGetAllowedDomainsImmutable() {
        Set<String> allowed = domainFilter.getAllowedDomains();
        assertThrows(UnsupportedOperationException.class, () -> allowed.add("malicious.com"));
    }

    @Test
    void testNormalizeDomainWithPrefix() {
        DomainFilter filter = new DomainFilter(Set.of("example.com"));
        assertTrue(filter.isAllowedDomain("http://example.com"));
        assertTrue(filter.isAllowedDomain("https://sub.example.com"));
    }

    @Test
    void testNormalizeNullDomain() {
        Set<String> setWithNull = new HashSet<>();
        setWithNull.add(null);
        DomainFilter filter = new DomainFilter(setWithNull);
        assertFalse(filter.isAllowedDomain("http://example.com"));
    }

    @Test
    void testNormalizeDomain() {
        DomainFilter filter = new DomainFilter(Set.of("https://example.com", "https://test.com", "http://mytest.com"));
        assertTrue(filter.isAllowedDomain("https://at.example.com"));
        assertTrue(filter.isAllowedDomain("https://sub.test.com"));
        assertTrue(filter.isAllowedDomain("http://de.mytest.com"));
        assertFalse(filter.isAllowedDomain("http://mytest1.com"));
    }
}
