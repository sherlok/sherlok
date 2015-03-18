# TODOs



--- a/config/pipelines/bluima/neuroner/neuroner_0.1.json
+++ b/config/pipelines/bluima/neuroner/neuroner_0.1.json
@@ -69,8 +69,9 @@
    "Neuron{NOT(REGEXP(\"[Cc]ells?\")) -> MARK(NeuronWithProperties, 1, 1)}; ",
    "(NeuronProperty+ (COMMA | \"and\" | (COMMA \"and\")))* {-> MARK(NeuronWithProperties, 1, 3)} NeuronProperty+ Neuron;",
    "// keep only longest NeuronWithProperties",
-   "// ERROR NeuronWithProperties{PARTOFNEQ(NeuronWithProperties) -> DEL};",
-   "ENGINE KeepLargestAnnotationAnnotator:0.0.1;",
+   "NeuronWithProperties{PARTOFNEQ(NeuronWithProperties) -> DEL};",
+   "//ENGINE NoOpAnnotator:0.0.1;",
+   "//ENGINE KeepLargestAnnotationAnnotator:0.0.1;",
    "// TODO remove cells without NeuronProperty",


* P1 should work offline, too



* P3 p not highlighted anymore when save

* .NET client, e.g. http://blog.anthonybaker.me/2013/05/how-to-consume-json-rest-api-in-net.html?m=1 or https://msdn.microsoft.com/en-us/magazine/ee309509.aspx

# OTHERS

* http://www.crunchbase.com/organization/indico-data-solutions
* http://honnibal.github.io/spaCy/
* https://github.com/NaturalNode/natural
* [date parser](http://natty.joestelmach.com/try.jsp)
* [watson concept insights](http://www.ibm.com/smarterplanet/us/en/ibmwatson/developercloud/doc/concept-insights/)
* [CLAVIN (Cartographic Location And Vicinity INdexer) open source software package for document geotagging and geoparsing](http://clavin.bericotechnologies.com/)

# DONE

* P3 order of pipelines in right col
* P2 spinning wheel while running tests


# TO IMPLEMENT

Sentiment:

* Stanford DL: http://nlp.stanford.edu/sentiment/code.html
* Lingpipe: http://alias-i.com/lingpipe/demos/tutorial/sentiment/read-me.html
* AlchemyAPI: http://grepcode.com/file/repo1.maven.org/maven2/org.apache.uima/AlchemyAPIAnnotator/2.3.1/org/apache/uima/alchemy/digester/sentiment/SentimentAnalysisDigesterProvider.java
* GATE: https://gate.ac.uk/sentiment/
* Cyc http://dev.cyc.com/cyc-api/ https://github.com/cycorp
* Joshua translation http://joshua-decoder.org/
* Medex: https://code.google.com/p/medex-uima/
* Textual Entailment: http://hltfbk.github.io/Excitement-Open-Platform/
* DkPro WSD https://code.google.com/p/dkpro-wsd/

DkPro

* list https://docs.google.com/spreadsheet/pub?key=0ApGcdapz0xSYdFNTREhKeFVEU1RsQzc0V0NKcE04b3c&single=true&gid=0&output=html
mvn urls: http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-model-releases-local/de/tudarmstadt/ukp/dkpro/core/

Parsers speed http://nlp.stanford.edu/software/stanford-dependencies.shtml#Methods

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
