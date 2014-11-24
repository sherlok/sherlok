var Sherlok = {

  annotate: function (pipelineId, version, text_to_annotate, success) {

    $.post("/annotate/" + pipelineId, {"text": text_to_annotate, "version": version}, function(annotated_json) {

      var txt = annotated_json["@cas_feature_structures"]["1"]["sofaString"];

      // collect annotations
      var blacklist = ["Sofa", "DocumentAnnotation"];
      var annots = [];
      annotated_json["@cas_views"]["1"].map(function(fs){
        var annot = annotated_json["@cas_feature_structures"][fs];
        if (blacklist.indexOf(annot["@type"]) == -1) {
          var begin = annot["begin"] || 0, end = annot["end"];
          var value = annot["value"] || 'annotation';
          annots[annots.length] = {begin: begin, end: end, value: value};
        }
      });

      // highlight text (add spans)
      var newTxt = "";
      var last = txt.length;
      $.each(annots.sort(predicatBy("end")), function(index3, a){
        console.log("ANNOT: " + a.begin);
        if (a.end < last){
          newTxt = '<span class="inline-a np_'+ a.value + '" title="'+a.value+'">' + txt.substring(a.begin, a.end) + '</span>' +  txt.substring(a.end, last) + newTxt;
          last = a.begin;
        }
      });
      var res = txt.substring(0, last) + newTxt;
      success(res);
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