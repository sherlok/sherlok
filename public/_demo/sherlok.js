function isInArray(value, array) {
  return array.indexOf(value) > -1;
}

function isInt(value) {
  return !isNaN(value) && 
         parseInt(Number(value)) == value && 
         !isNaN(parseInt(value, 10));
}

// http://stackoverflow.com/a/5047731/520217
function textToHTML(text)
{
    return ((text || "") + "")  // make sure it's a string;
        .replace(/"/g, "&quot;")
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/\t/g, "    ")
        .replace(/ /g, "&#8203;&nbsp;&#8203;")
        .replace(/\r\n|\r|\n/g, "<br />");
}

var Sherlok = {

  annotate: function (pipelineId, text_to_annotate, element_id) {
    
    $.post("/annotate/" + pipelineId, {"text": text_to_annotate}, function(annotated_json) {

      var refs = annotated_json["_referenced_fss"];
      var sofa = refs["1"];
      var txt = sofa["sofaString"];

      // collect settings
      var blacklist = ["Sofa", "DocumentAnnotation", "GeniaPOSTag", "Token", "Sentence", "FSArray", "Cooccurrence", "Measure"];
      var annots = [];

      // collect annotations
      var annotationSet = annotated_json["_views"]["_InitialView"];
      for (type in annotationSet) {
        if (!isInArray(type, blacklist)) {
          for (k in annotationSet[type]) {
            var annot = annotationSet[type][k];

            // handle references
            if (isInt(annot)) {
              annot = refs[annot];
            }
            
            var begin = annot["begin"] || 0;
            var end   = annot["end"];
            var value = annot["value"] || type; // if no "value", use type instead.
            if (isInt(value)) value = type;
            var desc  = textToHTML(JSON.stringify(annot, null, 2));
            annots[annots.length] = {begin: begin, end: end, value: value, desc: desc};
          }
        }
      }
    
      // highlight text (add spans)
      var newTxt = "";
      var last = txt.length;
      $.each(annots.sort(predicatBy("end")), function(index3, a){
        console.log(a);
        if (a.end <= last){
          
          newTxt =
              '<a class="inline-a np_' + a.value.toLowerCase() + '" ' +
                    'data-toggle="popover" data-trigger="hover" data-placement="bottom" ' +
                    'data-content="' + a.desc + '" ' +
                    'title="' + a.value + '">' +
                txt.substring(a.begin, a.end) +
              '</a>' +
              txt.substring(a.end, last) + newTxt; 

          last = a.begin;
        }
      });
      newTxt = txt.substring(0, last) + newTxt;
      $("#"+element_id).html(newTxt);
      $('[data-toggle="popover"]').popover({html: true}); 
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
