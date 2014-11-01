var Sherlok = {

    annotate: function (element_id) {

        console.log("annotate:: " + element_id);

        var to_annotate = $("#to_annotate").val();

        $.post("/annotate/default", {"text": to_annotate}, function(annotated_json) {
            console.log(annotated_json);

            var whitelist = ["NamedEntity"];
            // parse json
            var txt = annotated_json["@cas_feature_structures"]["1"]["sofaString"];

            var annots = [];
            annotated_json["@cas_views"]["1"].map(function(fs){
                var annot = annotated_json["@cas_feature_structures"][fs];
                if (whitelist.indexOf(annot["@type"]) > -1){
                    var begin = annot["begin"] || 0, end = annot["end"];
                    var value = annot["value"];
                    console.log("ANNOT1: "+begin, end);
                    annots[annots.length] = {begin: begin, end: end, value: value};
                }
            });
            console.log(annots);


            var newTxt = "";
            var last = txt.length;
            $.each(annots.sort(predicatBy("end")), function(index3, a){
                console.log("ANNOT: "+a.start);
                if (a.end < last){
                    newTxt = '<span class="inline-a np_'+ a.value + '" title="'+a.value+'">' + txt.substring(a.start, a.end) + '</span>' +  txt.substring(a.end, last) + newTxt;
                    last = a.start;
                }
            });
            newTxt = txt.substring(0, last) + newTxt;
            $("#annotated").append(newTxt);
        });
    }

//     display_results : function(res, _size, _from){
//         $("#results").empty();
//         $("#results").append('<p id="hits_cnt">Displaying results {0}-{1} from {2}:</p><ul>'.format(_from, _from + res.hits.length, res.total));
//         $.each(res.hits, function(index, hit) {
//             var txt = hit._source.sentence_text;
//             var newTxt = "";
//             var last = txt.length;
//             $.each(hit._source.neuron.sort(predicatBy("end")), function(index, n){
//                         //console.log("NEURON: "+n.start + n.neuron_text);
//                         $.each(n.neuron_properties.sort(predicatBy("end")), function(index3, np){
//                             //console.log("NP: "+np.start+np.property_text);
//                             if (np.end < last){
//                                 newTxt = '<span class="inline-a np_'+ np.neuron_type.toLowerCase() + '" title="'+np.neuron_type+'">' + np.property_text + '</span>' +  txt.substring(np.end, last) + newTxt;
//                                 last = np.start;
//                             }
//                         });
//                     });
//             newTxt = txt.substring(0, last) + newTxt;

//             $("#results").append('<p>{0} (<a href="http://www.ncbi.nlm.nih.gov/pubmed/{1}">PubMed</a>)</p>'
//                 .format(newTxt, hit._source.pm_id));
//         });
// $("#results").append('</ul><p><a class="next_page" href="#" onclick="javascript:next_page('+_from+')"'+'">Next Page</a></p>');
// }

}


// sort by inverse predicate
//Usage:: myArray.sort(predicatBy("end"));
function predicatBy(prop){
   return function(a,b){
      if( a[prop] > b[prop]){
          return -1;
      }else if( a[prop] < b[prop] ){
          return 1;
      }
      return 0;
   }
}




// mini templating, similar to Python's string.format()
//"hello {0}".format("world");
if (!String.prototype.format) {
    String.prototype.format = function() {
      var args = arguments;
      return this.replace(/{(\d+)}/g, function(match, number) {
        return typeof args[number] != 'undefined'
        ? args[number]
        : match
        ;
    });
  };
}

if (!String.prototype.capitalize) {
  String.prototype.capitalize = function() {
      return this.charAt(0).toUpperCase() + this.slice(1);
  }
}
