package org.aau.web;

public interface WebDriver {

    String getPageContent(String url);

    void close();

}
