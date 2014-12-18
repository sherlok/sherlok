# Sherlok

[![Build Status](https://travis-ci.org/renaud/sherlok.svg?branch=master)](https://travis-ci.org/renaud/sherlok)

_Distributed restful text mining._

Sherlok is a flexible and powerful open source, distributed, real-time text-mining engine. Sherlok works as a RESTful annotation server based on [Apache UIMA](http://uima.apache.org/). For example, Sherlok can:

- [x] highlight persons and locations in text (using [DKPro OpenNLP](https://www.ukp.tu-darmstadt.de/research/current-projects/dkpro/)),
- [ ] perform sentiment analysis using deep learning (using [Stanford Sentiment](http://nlp.stanford.edu/sentiment/)),
- [ ] analyse the syntax of tweets (using [TweetNLP](http://www.ark.cs.cmu.edu/TweetNLP/)),
- [ ] identify proteins and chemicals in biomedical texts (using [Bluima](https://github.com/BlueBrain/bluima)),
- [ ] analyze clinical text and perform knowledge extraction (using [Apache cTAKES](http://ctakes.apache.org/index.html))


### Getting Started

* Download and unzip the latest Sherlok [release](https://github.com/renaud/sherlok/releases)
* Run `bin/sherlok` (Unix), or `bin/sherlok.bat` (Windows)
* Start annotating [http://localhost:9600/annotate/opennlp.ners.en?text=Jack Burton...](http://localhost:9600/annotate/opennlp.ners.en?text=Jack Burton %28born April 29, 1954 in El Paso%29, also known as Jake Burton, is an American snowboarder and founder of Burton Snowboards.)
* Check the demo at http://localhost:9600/_demo/index.html

### Featured

* annotate persons and locations ([demo](http://localhost:9600/annotate/opennlp.ners.en?text=Jack Burton %28born April 29, 1954 in El Paso%29, also known as Jake Burton, is an American snowboarder and founder of Burton Snowboards.))
* annotate neurons with [neuroNER](https://github.com/renaud/neuroNER) ([demo](http://localhost:9600/annotate/neuroner?text=Layer V and layer iii large pyramidal neurons. Slowly adapting stretch receptor neuron))
* Berkeley parser ([demo](http://localhost:9600/annotate/berkeleyparser.en?text=The blue house of my childhood was bought: what a pity!))
* Malt parser ([demo](http://localhost:9600/annotate/maltparser.en?text=The blue house of my childhood was bought: what a pity!))
