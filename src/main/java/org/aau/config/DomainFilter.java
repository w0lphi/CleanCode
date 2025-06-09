package org.aau.config;

import org.aau.util.StringUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DomainFilter {

    private final Set<String> allowedDomains;

    public DomainFilter(Set<String> domains) {
        this.allowedDomains = domains.stream()
                .map(this::normalizeDomain)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

    }

    private Optional<String> normalizeDomain(String domain) {
        if (StringUtil.isEmpty(domain)) {
            return Optional.empty();
        }
        domain = domain.toLowerCase().trim();
        if (domain.startsWith("http://") || domain.startsWith("https://")) {
            return getDomainFromUrl(domain);
        }

        return Optional.of(domain);
    }

    private Optional<String> getDomainFromUrl(String urlString) {
        if (StringUtil.isEmpty(urlString)) {
            return Optional.empty();
        }

        try {
            URI url = new URI(urlString);
            String host = url.getHost();
            return normalizeDomain(host);
        } catch (URISyntaxException e) {
            System.err.printf("Invalid URI encountered %s: %s", urlString, e.getMessage());
            return Optional.empty();
        }
    }

    public boolean isAllowedDomain(String urlString) {
        if (allowedDomains.isEmpty()) {
            return true;
        }

        Optional<String> domain = getDomainFromUrl(urlString);
        if (domain.isEmpty()) {
            return false;
        }

        String domainValue = domain.get();
        return allowedDomains.stream()
                .anyMatch(allowedDomain -> domainValue.equals(allowedDomain) || domainValue.endsWith("." + allowedDomain));
    }

    public Set<String> getAllowedDomains() {
        return Collections.unmodifiableSet(allowedDomains);
    }
}
