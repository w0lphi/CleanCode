package org.aau.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DomainFilter {

    private final Set<String> allowedDomains;

    public DomainFilter(Set<String> domains) {
        this.allowedDomains = new HashSet<>();
        for (String domain : domains) {
            this.allowedDomains.add(normalizeDomain(domain));
        }
    }

    private String normalizeDomain(String domain) {
        if (domain == null) {
            return null;
        }
        domain = domain.toLowerCase().trim();
        if (domain.startsWith("http://") || domain.startsWith("https://")) {
            return getDomainFromUrl(domain);
        }

        return domain;
    }

    public String getDomainFromUrl(String urlString) {

        if (urlString == null || urlString.trim().isEmpty()) {
            return null;
        }

        try {
            URI url = new URI(urlString);
            String host = url.getHost();
            return normalizeDomain(host);
        } catch (URISyntaxException e) {
            System.err.printf("Invalid URI encountered %s: %s", urlString, e.getMessage());
            return null;
        }
    }

    public boolean isAllowedDomain(String urlString) {
        if (allowedDomains.isEmpty()) {
            return true;
        }

        String domain = getDomainFromUrl(urlString);
        if (domain == null) {
            return false;
        }

        return allowedDomains.stream()
                .anyMatch(allowedDomain -> domain.equals(allowedDomain) || domain.endsWith("." + allowedDomain));
    }

    public Set<String> getAllowedDomains() {
        return Collections.unmodifiableSet(allowedDomains);
    }
}
