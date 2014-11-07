var Sherlok = {

    annotate: function (element_id) {

        var to_annotate = $("#to_annotate").val();
        $.post("/annotate/opennlp_en_ners", {"text": to_annotate, "version": "1.6.2"}, function(annotated_json) {
            console.log(annotated_json);

            var blacklist = ["Sofa", "DocumentAnnotation"];
            // parse json
            var txt = annotated_json["@cas_feature_structures"]["1"]["sofaString"];

            // collect annotations
            var annots = [];
            annotated_json["@cas_views"]["1"].map(function(fs){
                var annot = annotated_json["@cas_feature_structures"][fs];
                if (blacklist.indexOf(annot["@type"]) == -1){
                    var begin = annot["begin"] || 0, end = annot["end"];
                    var value = annot["value"];
                    //console.log("ANNOT1: "+begin, end, value);
                    annots[annots.length] = {begin: begin, end: end, value: value};
                }
            });

            // highlight text (add spans)
            var newTxt = "";
            var last = txt.length;
            $.each(annots.sort(predicatBy("end")), function(index3, a){
                //console.log("ANNOT: " + a.begin);
                if (a.end < last){
                    newTxt = '<span class="inline-a np_'+ a.value + '" title="'+a.value+'">' + txt.substring(a.begin, a.end) + '</span>' +  txt.substring(a.end, last) + newTxt;
                    last = a.begin;
                }
            });
            newTxt = txt.substring(0, last) + newTxt;
            $("#annotated").html(newTxt);
        });
    }
}

// sort by inverse predicate
// Usage:: myArray.sort(predicatBy("end"));
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
