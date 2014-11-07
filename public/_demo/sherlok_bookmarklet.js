(function(){

    var v = "1.11.1"; // the minimum version of jQuery we want

    // check prior inclusion of JQuery and version
    if (window.jQuery === undefined || window.jQuery.fn.jquery < v) {
        var done = false;
        var script = document.createElement("script");
        script.src = "http://ajax.googleapis.com/ajax/libs/jquery/" + v + "/jquery.min.js";
        script.onload = script.onreadystatechange = function(){
            if (!done && (!this.readyState || this.readyState == "loaded" || this.readyState == "complete")) {
                done = true;
                initMyBookmarklet();
            }
        };
        document.getElementsByTagName("head")[0].appendChild(script);
    } else {
        initMyBookmarklet();
    }

    function initMyBookmarklet() {
        (window.sherlokBookmarklet = function() {
            // your JavaScript code goes here!

            if (window.jQuery) {
                console.log("jquery installed!");
            }
            debugger;
            var element_to_annotate = $("div.abstr/div/p");
            console.log(element_to_annotate);
            var txt_to_annotate = element_to_annotate.text();
            annotate(txt_to_annotate, element_to_annotate);

        })();
    }

    function annotate(txt_to_annotate, element_to_annotate){
        $.post("localhost:9600/annotate/default", {"text": txt_to_annotate}, function(annotated_json) {
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

            element_to_annotate.empty();
            element_to_annotate.html(newTxt);
        });
    }

})();
