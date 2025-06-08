# How to run 

First the web crawler has to be build with:

`./gradlew build`

Then it can be run by executing

`./gradlew run --args='<startUrl> <maximumDepth> <threadNumber> <allowedDomains>'`

`<startUrl>` -> The Url to start the crawl from  
`<maximumDepth>` -> The maximum depth which should be crawled   
`<Number of threads>` -> The number of threads which should be used for crawling. Must be positive. 
If no number is given, the crawler uses a single thread  
`<allowedDomains>` -> A comma-seperated list of allowed domains to crawl. If non are given, all domains are allowed

# How to test

The tests can be executed by runnning `./gradlew clean test`. The Jacoco report can then be found under `build/reports/jacoco/test`
