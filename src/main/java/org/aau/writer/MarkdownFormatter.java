package org.aau.writer;

import org.aau.crawler.result.BrokenLink;
import org.aau.crawler.result.Link;
import org.aau.crawler.result.WorkingLink;

public class MarkdownFormatter implements LinkFormatter {

    @Override
    public String format(Link link) {
        if(link instanceof WorkingLink wl){
            return "## " + wl.getUrl() + "\n"
                    + "Depth: " + wl.getDepth() + "\n"
                    + "### Headings\n" + String.join("\n", wl.getHeadings()) + "\n";
        } else if (link instanceof BrokenLink bl) {
            return "## " + bl.getUrl() + " (broken)\n"
                    + "Depth: " + bl.getDepth() + "\n";
        }
        return link.getUrl();
    }
}
