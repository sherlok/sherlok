var Sherlok = {

  blacklist : ["Sofa", "DocumentAnnotation","BrainRegionProp"], // TODO remove BrainRegionProp

  annotate: function (pipelineId, version, text, success) {
    $.post("/annotate/" + pipelineId, {"text": text, "version": version}, function(annotated_json) {
      // collect annotations
      var annots = [];
      annotated_json["@cas_views"]["1"].map(function(fs){
        var a = annotated_json["annotations"][fs];
        if (Sherlok.blacklist.indexOf(a["@type"]) == -1) {
          a["begin"] = a["begin"] || 0;
          annots[annots.length] = a;
        }
      });
      success(annots);
    })
    .fail(function(jqXHR, textStatus, errorThrown) {
      console.log("error",jqXHR);
      alert("Error: " + jqXHR.responseJSON.errorMessage);
    });
  },

  // annotateElement_old: function (txt, annotations, success) {
  //   // transform to array & filter annotations
  //   var annots = $.map(annotations, function(an, idx) {
  //     if (Sherlok.blacklist.indexOf(an["@type"]) == -1) {
  //       return [an];
  //     }
  //   });
  //   //console.log(annots);

  //   // highlight text (add spans)
  //   var newTxt = "";
  //   var last = txt.length + 1;
  //   $.each(annots.sort(predicatBy("end")), function(index3, a){
  //     //console.log("ANNOT: " + a.begin);
  //     if (a.end < last){
  //       newTxt = '<span class="inline-a np_'+ a['@type'].toLowerCase() + '" title="'+a['@type']+'">' + txt.substring(a.begin, a.end) + '</span>' +  txt.substring(a.end, last) + newTxt;
  //       last = a.begin;
  //     }
  //   });
  //   var res = txt.substring(0, last) + newTxt;
  //   success(res.replace(/(?:\r\n|\r|\n)/g, '<br />'));
  // },

  annotateElement: function (txt, annotations, success) {

    // an array, each position represents a char from txt
    // each value represents an array of the types at that char
    var charTypes = new Array(txt.length);
    for (var i = 0; i < txt.length; i++) {
      charTypes[i] = new Array();
    }
    // to index types
    var types = [];

    // transform to array & filter annotations
    $.map(annotations, function(an, idx) {
      if (Sherlok.blacklist.indexOf(an["@type"]) == -1) {
        // "index" type
        var type = an["@type"];
        if (types.indexOf(type) == -1){
          types.push(type);
        }
        for (var i = an.begin; i < an.end; i++) {
          var typeId = types.indexOf(type);
          if (charTypes[i].indexOf(typeId) == -1){
            charTypes[i].push(typeId);
          }
        }
      }
    });
    //console.log(types);
    //console.log(charTypes);

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
        }else{
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
  {"1": {"begin": 4, "end": 9, "@type": "Color"} }, function(data){ console.log(data); });
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
