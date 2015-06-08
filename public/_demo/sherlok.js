function isInArray(value, array) {
  return array.indexOf(value) > -1;
}

var Sherlok = {

  annotate: function (pipelineId, text_to_annotate, element_id) {
    
    $.post("/annotate/" + pipelineId, {"text": text_to_annotate}, function(annotated_json) {

      var sofa = annotated_json["_referenced_fss"]["1"];
      var txt = sofa["sofaString"];

      // collect annotations
      var blacklist = ["Sofa", "DocumentAnnotation"];
      var annots = [];

      var annotationSet = annotated_json["_views"]["_InitialView"];
      for (type in annotationSet) {
        if (!isInArray(type, blacklist)) {
          for (k in annotationSet[type]) {
            var annot = annotationSet[type][k];
            var begin = annot["begin"] || 0, end = annot["end"];
            var value = annot["value"] || type; // if no "value", use type instead.
            annots[annots.length] = {begin: begin, end: end, value: value};
          }
        }
      }
    
      // highlight text (add spans)
      var newTxt = "";
      var last = txt.length;
      $.each(annots.sort(predicatBy("end")), function(index3, a){
        console.log(a);
        if (a.end < last){
          newTxt = '<span class="inline-a np_'+ a.value + '" title="'+a.value+'">' + txt.substring(a.begin, a.end) + '</span>' +  txt.substring(a.end, last) + newTxt;
          last = a.begin;
        }
      });
      newTxt = txt.substring(0, last) + newTxt;
      $("#"+element_id).html(newTxt);
    });
  }
}

// sort by inverse predicate
function predicatBy(prop) {
  return function(a,b) {
    if (a[prop] > b[prop]) {
      return -1;
    } else if (a[prop] < b[prop]) {
      return 1;
    }
    return 0;
  }
}
