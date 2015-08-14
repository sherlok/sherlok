/**
 * Copyright (C) 2014-2015 Renaud Richardet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var Sherlok = {

  blacklist : ["Sofa", "DocumentAnnotation"],

  annotate: function (pipelineId, version, text, success) {
    post("http://localhost:9600/annotate/" + pipelineId, {text: text, version: version}, success,
    function(status, responseText) {
      console.log("error", status,responseText);
      alert("Error: " + responseText);
    });
  },

  annotateElement: function (txt, annotations, success) {

    // an array, each position represents a char from txt
    // each value represents an array of the types at that char
    var charTypes = new Array(txt.length);
    for (var i = 0; i < txt.length; i++) {
      charTypes[i] = new Array();
    }
    // types to index
    var types = [];

    // transform to above charTypes array & filter annotations
    for (var type in annotations) {
        if (annotations.hasOwnProperty(type)) {
          if (Sherlok.blacklist.indexOf(type) == -1) {
            // "index" type
            if (types.indexOf(type) == -1){
              types.push(type);
            }
            var typeId = types.indexOf(type);
            // update charTypes
            annotations[type].map( function(an) {
              for (var i = an.begin; i < an.end; i++) {
                if (charTypes[i] && charTypes[i].indexOf(typeId) == -1){
                  charTypes[i].push(typeId);
                }
              }
            });
          }
        }
      }
    // console.log(types);
    // console.log(charTypes);

    getTypes = function(typesMappings, types){
      var ret = "";
      types.map( function(type) {
        ret += ' np_' + typesMappings[type];
      })
      return ret;
    }

    var html = "";
    var lastTypes = [];
    var lastHasTypes = false;
    var spanOpen = false;
    closeSpan = function(){
      html += '</span>';
      spanOpen = false;
    }
    openSpan = function(types, c){
      var classes = getTypes(types, c);
      var title = classes.replace(/np_/g,'').trim();
      html += '<span class="inline-a'+classes+'" title="'+title+'">';
      spanOpen = true;
    }

    for (var i = 0; i < charTypes.length; i++) {
      var c = charTypes[i]; // holds type ids (if any)
      var hasTypes = c.length > 0; // if c has any types

      // only change span if types are not identical
      if (!arraysIdentical(c, lastTypes)) {
        if (lastHasTypes){ // close (if last was open)
          closeSpan();
        }
        if (hasTypes){ // open
          openSpan(types, c);
        }
      }

      // replace newlines with br
      if (/*!hasTypes &&*/ txt[i].match(/(?:\r\n|\r|\n)/g)) {
        if(spanOpen){ // close and reopen
          closeSpan();
          html += '<br/>';
          openSpan(types, c);
        } else {
          html += '<br/>';
        }
      } else {
        html += txt[i]; // add character
      }

      lastTypes = c;
      lastHasTypes = hasTypes;
    }

    if (lastHasTypes){ // close (if last was open)
      html += '</span>';
    }
    //console.log(html);
    success(html, types);
  }
}

/*
Sherlok.annotateElement('the green cat\nnewline',
  {"Color": [{"begin": 4, "end": 9}] }, function(data){ console.log(data); });
Sherlok.annotateElement('green cat',
  {"2": {"begin": 0, "end": 5, "@type": "Green"}, "1": {"begin": 0, "end": 5, "@type": "Color"}}, function(data){ console.log(data); });
Sherlok.annotateElement('abcd',
  {"1": {"begin": 0, "end": 3, "@type": "T03"}, "2": {"begin": 2, "end": 4, "@type": "T24"} }, function(data){ console.log(data); });
Sherlok.annotateElement('layer V neurons',
  {"92":{"begin":0,"end":7,"@type":"Layer","properties":{"ontologyId":"HBP_NEUROTRANSMITTER:0000005"},"ontologyId":"HBP_NEUROTRANSMITTER:0000005"},"106":{"begin":0,"end":7,"@type":"BrainRegionProp"},"110":{"begin":8,"end":15,"@type":"Neuron"},"134":{"begin":0,"end":15,"@type":"NeuronWithProperties"},"150":{"begin":0,"end":15,"@type":"NeuronWithProperties"},"178":{"begin":8,"end":15,"@type":"NeuronWithProperties"} }, function(data){ console.log(data); });
*/



function arraysIdentical(a, b) {
    var i = a.length;
    if (i != b.length) return false;
    while (i--) {
        if (a[i] !== b[i]) return false;
    }
    return true;
};

function predicatBy(prop) { // sort by inverse predicate
  return function(a,b) {
           if (a[prop] > b[prop]) { return -1;
    } else if (a[prop] < b[prop]) { return 1;
    } else {                        return 0;}
  }
}

function post (url, data, success, error) {
  var request = new XMLHttpRequest();
  request.open('POST', url, true);
  request.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');
  request.onreadystatechange = function() {
    if (this.readyState === 4) {
      if (this.status >= 200 && this.status < 400) {
        success(JSON.parse(this.responseText));
      } else {
        error(this.status, this.responseText);
      }
    }
  };
  var transformRequest =  function(obj) {
    var str = [];
    for(var p in obj)
    str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
      return str.join("&");
  };
  request.send(transformRequest(data));
  request = null;
}

/*
Sherlok.annotate('02.ruta.annotate.countries', null, 'I &love Italy.', function(data){
  console.log(data);
  Sherlok.annotateElement('I &love Italy.', data["_views"]["_InitialView"], function(an){
    console.log(an);
  });
}, function(data){ console.log(data); });
*/
