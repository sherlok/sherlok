var signin = document.getElementById("sign_in");
if(typeof signin != 'undefined'  && signin){
    signin.href = signin.href + "?back_url=" + encodeURIComponent(window.location);
}

var signout = document.getElementById('sign_out');
if(typeof signout != 'undefined' && signout){
    signout.href = signout.href + "?back_url=" + encodeURIComponent(window.location);
}

function getCookie(cookie_name) {
    var start_pos = document.cookie.indexOf(cookie_name + "="); //start cookie name
    if (start_pos != -1) {
        start_pos = start_pos + cookie_name.length+1; //start cookie value                
        var end_pos = document.cookie.indexOf(";", start_pos);
        if (end_pos == -1) {
            end_pos = document.cookie.length;
        }
        return decodeURIComponent(document.cookie.substring(start_pos, end_pos)); 
    }
    else {
        return "";
    }
}        

var c = getCookie('WebCubbyUser');
c = decodeURIComponent(decodeURIComponent(c));
lre = /.*logged-in\=(\w*);.*/; 
ure = /.*my-name\=([\w|\-|\.|\ |\@|\+]*);.*/;
plus = /\+/gi;

if(c){ 
    l = lre.exec( c );
    if(l && l[1] && l[1] === 'true' ) {
        u = ure.exec( c );
        if(u && u[1]){ 
            var myncbi_username = document.getElementById("myncbiusername");
            var uname = document.getElementById('mnu');
            if (uname) {
                if (typeof uname != 'undefined') {
                    uname.appendChild(document.createTextNode(u[1].replace(plus, ' ')));
                    myncbi_username.style.display = "inline";
                
                    var signin = document.getElementById("sign_in");
                    signin.style.display = "none";                                          
                
                    var signout = document.getElementById("sign_out");
                    signout.style.display = "inline";
                    
                    var myncbi = document.getElementById('myncbi');
                    myncbi.style.display='inline';                                                      
                }
            }
        }
    }
}

(function( $ ){ 
    $( function() {
        if (typeof $.fn.ncbipopper == "function") {
            $('#info .external').each( function(){
                var $this = $( this );
                var popper = $this;
                popper.ncbipopper({
                    destSelector: '#external-disclaimer',
                    isDestElementCloseClick: false,
                    openAnimation: 'none', 
                    closeAnimation: 'none', 
                    isTriggerElementCloseClick: false,
                    triggerPosition: 'bottom center', 
                    destPosition: 'top center', 
                    hasArrow: true, 
                    arrowDirection: 'top'
                });
            }); 
        }
    });
})( jQuery );

if(typeof jQuery !== 'undefined' && jQuery.ui){
    var version = jQuery.ui.jig.version;
    var pieces = version.split(".");
    if(pieces[0] >= 1 && pieces[1] >= 11){
        if(pieces[1] == 11 && pieces[2] && pieces[3] >= 2){
            jQuery("#sign_in").click(function(e){        
                if(typeof jQuery.ui.jig.requiresLogin !== 'undefined'){
                    e.preventDefault();
                    jQuery.ui.jig.requiresLogin();
                }
            });
        }
    }
}
// Global Alerts - new
if (typeof(jQuery) != 'undefined') {
    jQuery.getScript("/core/alerts/alerts.js", function () {
        galert(['div.nav_and_browser', 'div.header', '#universal_header', 'body > *:nth-child(1)'])
    });
}