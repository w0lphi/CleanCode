# How to run 

First the web crawler has to be build with:

`./gradlew build`

Then it can be run by executing

`./gradlew run --args='<startUrl> <maximumDepth>'`

\<startUrl\> -> The Url to start the crawl from
\<maximumDepth\> -> THe maximum depth which should be crawled

# How to test

The tests can be executed by runnning `./gradlew clean test`. The Jacoco report can then be found under `build/reports/jacoco/test`
