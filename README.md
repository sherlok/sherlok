# Sherlok

_Distributed restful text mining._

Sherlok is a flexible and powerful open source, distributed, real-time text-mining engine. Sherlok works as a RESTful annotation server based on [Apache UIMA](http://uima.apache.org/). For example, Sherlok can:

* highlight persons and locations in text (using [DKPro OpenNLP](https://www.ukp.tu-darmstadt.de/research/current-projects/dkpro/)),
* perform sentiment analysis using deep learning (using [Stanford Sentiment](http://nlp.stanford.edu/sentiment/)),
* analyse the syntax of tweets (using [TweetNLP](http://www.ark.cs.cmu.edu/TweetNLP/)),
* identify proteins and chemicals in biomedical texts (using [Bluima](https://github.com/BlueBrain/bluima)),
* analyze clinical text and perform knowledge extraction (using [Apache cTAKES](http://ctakes.apache.org/index.html))


### Getting Started

* Download and unzip the latest Sherlok [release](https://github.com/renaud/sherlok/releases)
* Run `bin/sherlok` (Unix), or `bin/sherlok.bat` (Windows)
* Start annotating [http://localhost:9600/annotate/opennlp_ners?text=Jack Burton...](http://localhost:9600/annotate/opennlp_en_ners?text=Jack Burton %28born April 29, 1954 in El Paso%29, also known as Jake Burton, is an American snowboarder and founder of Burton Snowboards.)
* Check the demo at http://localhost:9600/_demo/index.html




# Formats

WARNING: some features below are not yet implemented!

### Pipelines

A pipeline describes the steps to perform a text mining analysis (e.g. split words, remove determinants, annotate locations, ...). For example, the pipeline below first performs some preprocessing: segmenting raw text in sentences and tokens (words), then tagging words with their corresponding [part-of-speech](http://en.wikipedia.org/wiki/Part-of-speech_tagging). This preprocessing is then used by two [named entity recognizers](http://en.wikipedia.org/wiki/Named-entity_recognition) (NERs) that identify persons and location.

    {
      "name" : "opennlp.ners.en",
      "version" : "1.6.2",
      "language" : "en",
      "domain" : "dkpro",
      "description" : "annotates English persons and locations using OpenNLP models",
      "engines" : [ 
        { "id" : "opennlp.segmenter.en:1.6.2" }, 
        { "id" : "opennlp.pos.en:1.6.2" }, 
        { "id" : "opennlp.ner.person.en:1.6.2" }, 
        { "id" : "opennlp.ner.location.en:1.6.2"}
        ],
      "output" : {
        "annotations" : [ "dkpro.NamedEntity" ]
      }
    }


* Name: a unique name for this pipeline. Letters, numbers and underscore only
* Version: a unique id for this pipeline. Letters, numbers and underscore only
* Language: (optional, defaults to 'en') which ISO language this pipeline works for.
* Domain: useful to group pipelines together. Letters, numbers, slashes and underscore only
* Description (optional):
* Load_on_startup (optional, defaults to `false`): whether this pipeline should be loaded on server startup
* Annotations: which annotations to include in the output JSON
* Payloads (optional): which payloads to include in the output JSON (see Payload chapter below)

Engines can have the following formats: 
    { "id": "engine id (see chapter below)"}
    { "script": "Ruta script (see chapter below)"}

### Engines

An engine performs a single text analysis step in a pipeline. Engines can be reused across different pipelines. This is how we can define its configuration settings:

    {
        "name": "opennlp.segmenter.en",
        "version": "1.6.2",
        "domain": "dkpro",
        "class": "de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter",
        "bundle": "dkpro.opennlp.en:1.6.2",
        "params" {
            "language": "en"
        }
    }

* Name: a unique name for this engine. Letters, numbers and underscore only
* Version: a unique id for this engine. Letters, numbers and underscore only
* Domain: useful to group engines together. Letters, numbers, slashes and underscore only
* Description (optional)
* Class: the Java UIMA class name of this engine
* Bundle: which bundle this engine comes from (see Bundle chapter below)
* Parameters: provides (or overwrites) the engine parameters. Some parameters are optionals, other are required (this is defined in the UIMA engine).

### Bundles

A Bundle helps group together a set of library dependencies.

    {
      "name" : "dkpro.opennlp.en",
      "version" : "1.6.2",
      "description" : "all opennlp engines and models for English",
      "repositories" : {
        "dkpro" : "http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-model-releases-local/"
      },
      "dependencies" : [ {
        "type" : "mvn",
        "value" : "de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.stanfordnlp-gpl:1.6.2"
      }, {
        "type" : "mvn",
        "value" : "de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.opennlp-asl:1.6.2"
      }, ... ]
    }
    
* Name: a unique name for this bundel. Letters, numbers and underscore only
* Version: a unique id for this bundle. Letters, numbers and underscore only
* Description (optional)
* Dependencies: a list of all the dependencies of this bundle 
* Repositories: additional maven repositories

Dependencies can optionally have the following formats: 

    { "mvn": "{group_id}:{artifact_id}:{version}"}
    { "git": "{git_url}:{revision_id}"}
    { "jar": "{jar_url_or_path}"}

* `mvn` corresponds to a released maven artifact, this is the default value.
* `git` can be any accessible git repository that contains a Maven project (TODO)
* `jar` corresponds to a local or remote jar (TODO)
