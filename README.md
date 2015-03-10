# Sherlok

[![Build Status](https://travis-ci.org/sherlok/sherlok.svg?branch=master)](https://travis-ci.org/sherlok/sherlok)

_Distributed restful text mining._

Sherlok is a flexible and powerful open source, distributed, real-time text-mining engine. Sherlok works as a RESTful annotation server based on [Apache UIMA](http://uima.apache.org/). For example, Sherlok can:

- [x] highlight persons and locations in text (using [DKPro OpenNLP](https://www.ukp.tu-darmstadt.de/research/current-projects/dkpro/)),
- [ ] perform sentiment analysis using deep learning (using [Stanford Sentiment](http://nlp.stanford.edu/sentiment/)),
- [ ] analyse the syntax of tweets (using [TweetNLP](http://www.ark.cs.cmu.edu/TweetNLP/)),
- [ ] identify proteins and chemicals in biomedical texts (using [Bluima](https://github.com/BlueBrain/bluima)),
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

#### Create new Pipelines with the editor

http://localhost:9600/_editor/
