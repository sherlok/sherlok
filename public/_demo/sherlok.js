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

    var spinnerOps = {
      lines: 17 // The number of lines to draw
    , length: 28 // The length of each line
    , width: 14 // The line thickness
    , radius: 42 // The radius of the inner circle
    , scale: 1 // Scales overall size of the spinner
    , corners: 1 // Corner roundness (0..1)
    , color: '#000' // #rgb or #rrggbb or array of colors
    , opacity: 0.25 // Opacity of the lines
    , rotate: 0 // The rotation offset
    , direction: 1 // 1: clockwise, -1: counterclockwise
    , speed: 0.6 // Rounds per second
    , trail: 60 // Afterglow percentage
    , fps: 20 // Frames per second when using setTimeout() as a fallback for CSS
    , zIndex: 2e9 // The z-index (defaults to 2000000000)
    , className: 'spinner' // The CSS class to assign to the spinner
    , top: '50%' // Top position relative to parent
    , left: '50%' // Left position relative to parent
    , shadow: true // Whether to render a shadow
    , hwaccel: true // Whether to use hardware acceleration
    , position: 'absolute' // Element positioning
    }
    var target = document.getElementById(element_id)
    var spinner = new Spinner(spinnerOps).spin(target);
    
    $.post("/annotate/" + pipelineId, {"text": text_to_annotate}, function(annotated_json) {

      var refs = annotated_json["_referenced_fss"];
      var sofa = refs["1"];
      var txt = sofa["sofaString"];

      // collect settings
      var blacklist = ["Sofa", "DocumentAnnotation", "GeniaPOSTag", "Token", "Sentence", "FSArray", "Cooccurrence"];
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

    }).fail(function(xhr, textStatus, errorThrown) {

        $("#"+element_id).html(
            '<div class="alert alert-danger fade in">' +
            '<a href="#" class="close" data-dismiss="alert">&times;</a>' +
            '<strong>' + errorThrown + '</strong> ' + xhr.responseText +
            '</div>'
        );

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
