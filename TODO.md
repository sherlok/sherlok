# TODOs

Sentiment:

* Stanford DL: http://nlp.stanford.edu/sentiment/code.html
* Lingpipe: http://alias-i.com/lingpipe/demos/tutorial/sentiment/read-me.html
* AlchemyAPI: http://grepcode.com/file/repo1.maven.org/maven2/org.apache.uima/AlchemyAPIAnnotator/2.3.1/org/apache/uima/alchemy/digester/sentiment/SentimentAnalysisDigesterProvider.java
* GATE: https://gate.ac.uk/sentiment/

Medex: https://code.google.com/p/medex-uima/


DkPro

* list https://docs.google.com/spreadsheet/pub?key=0ApGcdapz0xSYdFNTREhKeFVEU1RsQzc0V0NKcE04b3c&single=true&gid=0&output=html
mvn urls: http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-model-releases-local/de/tudarmstadt/ukp/dkpro/core/

License
* http://www.gnu.org/licenses/gpl-faq.html#NFUseGPLPlugins
    * If the program dynamically links plug-ins, and they make function calls to each other and share data structures, we **believe** they form a single program, which must be treated as an extension of both the main program and the plug-ins. 
* http://stackoverflow.com/questions/2111047/does-the-gpl-state-that-dependencies-of-gpld-software-also-have-to-be-released
    *  Dynamic linking is debatable. 
* http://www.gnu.org/licenses/lgpl-java.html
* http://stackoverflow.com/questions/3752385/does-licensing-matter-for-maven-artifacts-that-are-runtime-or-test-scoped

---- 

# Notes


Spark framework

* http://www.sparkjava.com/documentation.html
* http://www.taywils.me/2013/11/05/javasparkframeworktutorial.html
* https://github.com/perwendel/spark-template-engines/tree/master/spark-template-freemarker

    cp sherlok-1.pom local_repo/sherlok/sherlok/1/

Integration testing:

* https://code.google.com/p/rest-assured/
* http://rest-assured.googlecode.com/svn/tags/2.3.4/apidocs/com/jayway/restassured/path/json/JsonPath.html

Jacoco Reports:

    mvn clean test jacoco:report

License

    mvn license:format

Gitstats Reports:

    cd /Users/richarde/dev_hdd/sources/gitstats
    git clone git://repo.or.cz/gitstats.git
    ./gitstats ../../uima/sherlok/sherlok_core ../../uima/sherlok/sherlok_core/target/gitstats
    open ../../uima/sherlok/sherlok_core/target/gitstats/index.html
