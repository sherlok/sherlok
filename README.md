
# Sherlok

_Distributed restful text mining._

Sherlok is a flexible and powerful open source, distributed, real-time text-mining engine. Sherlok works as a RESTful annotation server based on Apache UIMA. Sherlok can:

* Highlight persons and locations in text (using [DKPro OpenNLP](https://www.ukp.tu-darmstadt.de/research/current-projects/dkpro/)),
* Sentiment analysis using deep learning (using [Stanford Sentiment](http://nlp.stanford.edu/sentiment/)),
* Syntactic analysis of tweets (using [TweetNLP](http://www.ark.cs.cmu.edu/TweetNLP/)),
* Identify proteins and chemicals in biomedical texts (using [Bluima](https://github.com/BlueBrain/bluima)),
* Clinical text analysis and knowledge extraction (using [Apache cTAKES](http://ctakes.apache.org/index.html))


## Getting Started

* Download and unzip the latest Sherlok [release](https://github.com/renaud/sherlok/releases)
* Run `bin/sherlok` (Unix), or `bin/sherlok.bat` (Windows)
* Start annotating [http://localhost:9600/annotate/opennlp_ners?text=Jack Burton...](http://localhost:9600/annotate/opennlp_ners?text=Jack Burton %28born April 29, 1954 in El Paso%29, also known as Jake Burton, is an American snowboarder and founder of Burton Snowboards.)
* Check the demo at http://localhost:9600/_demo/index.html


## WARNING: some features below are not yet implemented!

## Pipelines

A pipeline describes the steps to perform a text mining analysis. This analysis consists of a list of steps to be performed on text (e.g. split words, remove determinants, annotate locations, ...).

    {   
        "name": "opennlp_ners",
        "version": "1",
        "language": "en",
        "description": "annotates English persons and locations using OpenNLP models",
        "load_on_startup": true,
        "engines" : [
            { "name": "OpenNlpEnSegmenter:1.6.2" },
            { "name": "OpenNlpEnPosTagger:1.6.2" },
            { "name": "OpenNlpEnPersonFinder:1.6.2" },
            { "name": "OpenNlpEnLocationFinder:1.6.2" }
        ],
        "output" : {
            "annotations": [
                "person",
                "location"
            ],
            "payloads": [
            ]
        }
    }


* Name: a unique name for this pipeline. Letters, numbers and underscore only
* Version: a unique id for this pipeline. Letters, numbers and underscore only
* Language: which language this pipeline works for (ISO code, or "all")
* Domain: useful to group pipelines together. Letters, numbers, slashes and underscore only
* Description (optional):
* Load_on_startup (optional): whether this pipeline should be loaded on server startup. Defaults to `false`
* Engines : a list of engines (see Engines chapter below)
* Annotations: which annotations to include in the output JSON
* Payloads (optional): which payloads to include in the output JSON (see Payload chapter below)

## Engines

An engine performs a single text analysis step in a pipeline. Engines can be reused across different pipelines. This is how we can define its configuration settings:

    {
        "name": "OpenNlpEnSegmenter",
        "version": "1.6.2",
        "domain": "dkpro",
        "class": "de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter",
        "bundle": "dkpro_opennlp_en:1.6.2",
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

## Bundles

A Bundle helps group together a set of library dependencies.

    {
      "name" : "dkpro_opennlp_en",
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

Dependencies can have the following formats: 

    { "mvn": "{group_id}:{artifact_id}:{version}"}
    { "git": "{git_url}:{revision_id}"}
    { "jar": "{jar_url_or_path}"}

* `mvn` corresponds to a released maven artifact 
* `git` can be any accessible git repository that contains a Maven project (TODO)
* `jar` corresponds to a local or remote jar (TODO)
