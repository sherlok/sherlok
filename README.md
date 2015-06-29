# Sherlok

_Distributed restful text mining._

[![Join the chat at https://gitter.im/sherlok/sherlok](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/sherlok/sherlok?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Build Status](https://travis-ci.org/sherlok/sherlok.svg?branch=master)](https://travis-ci.org/sherlok/sherlok)

Sherlok is a flexible and powerful open source, distributed, real-time text-mining engine. Sherlok works as a RESTful annotation server based on [Apache UIMA](http://uima.apache.org/). For example, Sherlok can:

- [x] highlight persons and locations in text (using [DKPro OpenNLP](https://www.ukp.tu-darmstadt.de/research/current-projects/dkpro/)),
- [x] identify proteins and brain regions in biomedical texts (using [Bluima](https://github.com/BlueBrain/bluima)),
- [ ] perform sentiment analysis using deep learning (using [Stanford Sentiment](http://nlp.stanford.edu/sentiment/)),
- [ ] analyse the syntax of tweets (using [TweetNLP](http://www.ark.cs.cmu.edu/TweetNLP/)),
- [ ] analyze clinical text and perform knowledge extraction (using [Apache cTAKES](http://ctakes.apache.org/index.html))


#### Getting Started

* Download and unzip the latest Sherlok [release](https://github.com/renaud/sherlok/releases)
* [Install](TODO) a Java runtime
* Run `bin/sherlok` (Unix), or `bin/sherlok.bat` (Windows)


#### Annotate neuron mentions from Python:

    pip install --upgrade sherlok

    >>> from sherlok import Sherlok
    >>> print list(Sherlok().annotate('neuroner', 'layer 4 neuron'))

    [(0, 14, 'layer 4 neuron', u'Neuron', {}),
     (8, 14, 'neuron',  u'Neuron', {}),
     (8, 14, 'neuron',  u'NeuronTrigger', {}),
     (0, 7,  'layer 4', u'Layer', {u'ontologyId': u'HBP_LAYER:0000004'})]


#### Tag persons and locations with Javascript:

    require('sherlok');
    var text = 'Jack Burton (born April 29, 1954 in El Paso), also known as Jake Burton, is an American snowboarder and founder of Burton Snowboards.';
    sherlok.annotate('opennlp.ners.en', text, function(annotation){
          console$(annotation);
    });
    { begin=0, end=11,  value="person"}
    { begin=36, end=43, value="location"}
    { begin=60, end=71, value="person"}

#### More Built-in Text mining pipelines

* Berkeley parser ([demo](http://localhost:9600/annotate/berkeleyparser.en?text=The blue house of my childhood was bought: what a pity!))
* Malt parser ([demo](http://localhost:9600/annotate/maltparser.en?text=The blue house of my childhood was bought: what a pity!))


### Further Documentation

 * [How to install and deploy Sherlok on your server.](https://github.com/sherlok/sherlok/wiki/Installation-and-Deployment)
 * [What are the major concept in Sherlok.](https://github.com/sherlok/sherlok/wiki/Architecture-and-Concepts#concepts)
  * [What is a pipeline.](https://github.com/sherlok/sherlok/wiki/Architecture-and-Concepts#pipelines)
  * [What is a bundle.](https://github.com/sherlok/sherlok/wiki/Architecture-and-Concepts#bundles)
  * [How to automatically download remote resources.](https://github.com/sherlok/sherlok/wiki/Architecture-and-Concepts#external-resources--configuration-variables)
 * [How to communicate with Sherlok.](https://github.com/sherlok/sherlok/wiki/REST-queries)
  * [What does REST mean.](https://github.com/sherlok/sherlok/wiki/REST-queries#quick-introduction-to-rest-queries-with-curl)
  * [What actions can Sherlok perform.](https://github.com/sherlok/sherlok/wiki/REST-queries#list-of-available-rest-queries)
 * [Using the built-in editor](https://github.com/sherlok/sherlok/wiki/Online-editor)
 * [How to extend Sherlok capability.](https://github.com/sherlok/sherlok/wiki/Creating-new-Sherlok-components)

