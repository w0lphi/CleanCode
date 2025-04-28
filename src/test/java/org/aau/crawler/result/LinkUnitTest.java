package org.aau.crawler.result;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LinkUnitTest {

    @Test
    void workingLinkGetSubLinksShouldReturnEmptySetOnNull() {
        var workingLink = new WorkingLink("https://www.test.com", 10, null, null);
        Set<String> subLinks = workingLink.getSubLinks();
        assertNotNull(subLinks);
        assertEquals(0, subLinks.size());
    }

    @Test
    void workingLinkGetSubLinksShouldReturnCorrectSet() {
        Set<String> expectedSubLinks = Set.of(
                "https://www.test.com/1",
                "https://www.test.com/2",
                "https://www.test.com/3"
        );
        var workingLink = new WorkingLink("https://www.test.com", 10, null, expectedSubLinks);
        assertEquals(expectedSubLinks, workingLink.getSubLinks());
    }

    @Test
    void equalsShouldReturnTrueWhenSameUrl() {
        Link workingLink1 = new WorkingLink("https://www.test.com", 10, Set.of("Heading1"), Set.of("Link1"));
        Link workingLink2 = new WorkingLink("https://www.test.com", 1, Set.of("Heading1", "Heading2", "Heading3"), Set.of("Link3"));
        assertEquals(workingLink1, workingLink2);
    }

    @Test
    void equalsShouldReturnFalseWhenDifferentUrl() {
        Link workingLink1 = new WorkingLink("https://www.test.com", 10, Set.of("Heading1"), Set.of("Link1"));
        Link workingLink2 = new WorkingLink("https://www.test1.com", 1, Set.of("Heading1", "Heading2", "Heading3"), Set.of("Link3"));
        assertNotEquals(workingLink1, workingLink2);
    }

    @Test
    void equalsShouldReturnFalseWhenDifferentSubClass() {
        Link workingLink = new WorkingLink("https://www.test.com", 10, Set.of("Heading1"), Set.of("Link1"));
        Link brokenLink = new BrokenLink("https://www.test.com", 10);
        assertNotEquals(workingLink, brokenLink);
    }

    @Test
    void getUrlShouldReturnUrl() {
        String url = "https://www.test.com";
        Link workingLink = new WorkingLink(url, 10, Set.of("Heading1"), Set.of("Link1"));
        assertEquals(url, workingLink.getUrl());
    }

}
