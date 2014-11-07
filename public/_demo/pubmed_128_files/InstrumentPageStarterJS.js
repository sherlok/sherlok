if (typeof ncbi === "undefined") {
    ncbi = {};
}

ncbi.sgAppsWithScrolling = [
    {"ncbi_app": "entrez",
     "ncbi_db": "gene",
     "ncbi_report": "full_report"},
     {"foo": "bar"}
];

;
(function(){if(typeof ncbi==="undefined")ncbi={};if(ncbi.sg)typeof console!=="undefined"&&console.error&&console.error("The logging JavaScript was added twice in the document. Please include it one time.");else{ncbi.sg=function(){};ncbi.sg.getInstance=function(){if(!ncbi.sg._instance)ncbi.sg._instance=new ncbi.sg;return ncbi.sg._instance};ncbi.sg._instance=null;ncbi.sg._version="29";(function(){var a=navigator.cookieEnabled?true:false;if(typeof navigator.cookieEnabled==="undefined"&&!a){document.cookie=
"testcookie";a=document.cookie.indexOf("testcookie")!==-1?true:false}ncbi.sg.isCookieEnabled=a})();ncbi.sg.appLogIgnore=["ncbi_sessionid","ncbi_clickdisabled"];ncbi.sg.prototype={init:function(){this.isProcessRunning=true;this._setUpMetaTagValues();this._setScrollingEnabled();if(this._cachedVals.ncbi_db&&this._cachedVals.ncbi_db.value.toLowerCase()=="pubmed"&&this._cachedVals.ncbi_report&&this._cachedVals.ncbi_report.value.toLowerCase()=="abstract"&&this._cachedVals.ncbi_resultcount&&this._cachedVals.ncbi_resultcount.value.toLowerCase()==
"1"||this._cachedVals.ncbi_db&&this._cachedVals.ncbi_db.value.toLowerCase()=="pmc"&&this._cachedVals.ncbi_report&&this._cachedVals.ncbi_report.value.toLowerCase()=="record"&&this._cachedVals.ncbi_type&&this._cachedVals.ncbi_type.value.toLowerCase()=="fulltext")ncbi.sg.isHoverEnabled=true;this._setUpPathParts();this._setUpCustomProps();this._addOnScrollListeners();this._send("init");this._scheduleDOMReadyPing();this._sendPrev();this._addPrintPing();this._setBeforeScrollDetails()},_vals:{},_cachedVals:{},
_hasInitRun:false,_readyPinged:false,_linkObjs:[],_pathParts:{part1:"",part2:"",part3:"",part4:""},_scheduleDOMReadyPing:function(){var a=this,b=function(){return typeof jQuery!=="undefined"&&typeof jQuery.ui!=="undefined"&&typeof jQuery.ui.jig!=="undefined"?typeof jQuery.ui.jig.version!=="undefined"&&jQuery.ui.jig.version?jQuery.ui.jig.version:"unknown":"unknown"},d=function(){var m={},l=jQuery.ui.jig._foundWidgets;for(var u in l)m["jigWidget_"+u]=l[u];m.jigVersion=b();return m},c=function(){if(!a._readyPinged){var m=
{};if(typeof jQuery!=="undefined"&&typeof jQuery.ui!=="undefined"&&typeof jQuery.ui.jig!=="undefined"&&typeof jQuery.ui.jig.scanned!=="undefined")if(jQuery.ui.jig.scanned){m=d();m.jsevent="domready";ncbi.sg._ping(m);a._readyPinged=true}else{if(typeof jQuery.ui.jig.scan!=="undefined"&&jQuery.ui.jig.scan){var l=jQuery.ui.jig.scan;jQuery.ui.jig.scan=function(){var u=l.apply(this,arguments);c();return u}}}else{m.jigVersion="nojig";m.jsevent="domready";ncbi.sg._ping(m);a._readyPinged=true}}};if(document.addEventListener){var f=
function(){document.removeEventListener("DOMContentLoaded",f,false);c()};document.addEventListener("DOMContentLoaded",f)}else if(document.attachEvent){var g=function(){if(document.readyState==="complete"){document.detachEvent("onreadystatechange",g);c()}};document.attachEvent("onreadystatechange",g)}},_setScrollingEnabled:function(){try{ncbi.sg.isScrollingEnabled=false;var a=ncbi.sgAppsWithScrolling;if(typeof a!=="undefined")for(var b=0;b<a.length;b++){var d=true,c=a[b];for(var f in c)if(!this._cachedVals[f]||
this._cachedVals[f].value.toLowerCase()!=c[f]){d=false;break}if(d){ncbi.sg.isScrollingEnabled=true;break}}}catch(g){ncbi.sg.getInstance().noteEventData("jserror",{jserror:g.toString(),SELF_URL:window.location.href},["ncbi_sessionid","ncbi_phid"])}},_setUpMetaTagValues:function(){for(var a=document.getElementsByTagName("meta"),b=0;b<a.length;b++){var d=a[b].name;d.indexOf("ncbi_")===0&&this.addEntry(d,a[b].content)}},_setUpCachedMetaTagValues:function(a){for(var b=0;b<a.length;b++){var d=this._cachedVals[a[b]];
d&&this.addEntry(a[b],d.value)}},cachedNames:["ncbi_app","ncbi_db","ncbi_pcid","ncbi_pdid","ncbi_phid","ncbi_sessionid","ncbi_uidlist"],ignoreLengthRestrictions:["jserror","jserrorlocation"],addEntry:function(a,b){if(!(b===undefined||b.length===0)){if(b.length>100&&this.ignoreLengthRestrictions.indexOf(a)===-1)b=b.substr(0,100);this._cachedVals[a]={sProp:a,value:b}}},getVal:function(a){return typeof this._cachedVals[a]!=="undefined"&&this._cachedVals[a]&&typeof this._cachedVals[a].value!=="undefined"?
this._cachedVals[a].value:null},removeAllEntries:function(){for(var a={},b=this.cachedNames.length,d=0;d<b;d++){var c=this.cachedNames[d],f=this._cachedVals[c];if(f)a[c]=f}this._cachedVals=a;for(var g in this._pathParts)this._pathParts[g]=""},_setUpCustomProps:function(){var a=this._pathParts.part1,b=this._pathParts.part2.length>0?":"+this._pathParts.part2:"",d=this._pathParts.part3.length>0?":"+this._pathParts.part3:"",c=this._pathParts.part4.length>0?":"+this._pathParts.part4:"";a={pagename:a+b+
d,server:window.location.hostname,sitesect2:a+b,subsect3:a+b+d,subsect4:a+b+d+c,heir1:(a+b+d+c).replace(/:/g,"|")};for(var f in a)this.addEntry(f,a[f]);this._sessionIdCheck();this._staticPageCheck();this._prevHitCheck();this._browserConfigurationSettings();this._hashCheck()},_staticPageCheck:function(){this._cachedVals.ncbi_app&&this._cachedVals.ncbi_app.value.length>0||this.addEntry("ncbi_app","static");this._cachedVals.ncbi_pdid&&this._cachedVals.ncbi_pdid.value.length>0||this.addEntry("ncbi_pdid",
(document.title||"unknown").replace(/\s+/g,""))},_sessionIdCheck:function(){if(!(this._cachedVals.ncbi_sessionid&&this._cachedVals.ncbi_sessionid.value.length>0)){var a="";if(a.length===0){var b=this.getCookie("WebCubbyUser")||this.getCookie("WebEnv");if(b.length>0){b=unescape(b).split("@");if(b.length>1)a=b[b.length-1]}}if(a.length===0)a="UNK_SESSION";this.addEntry("ncbi_sessionid",a)}},getBrowserWidthHeight:function(){var a=this.getViewportWidth(),b=this.getViewportHeight();return{width:a,height:b}},
_browserConfigurationSettings:function(){var a=this.getBrowserWidthHeight();this.addEntry("browserwidth",a.width);this.addEntry("browserheight",a.height);this.addEntry("screenwidth",screen.width);this.addEntry("screenheight",screen.height);this.addEntry("screenavailwidth",screen.availWidth);this.addEntry("screenavailheight",screen.availHeight);if(document&&document.body){var b=document.body.scrollWidth,d=document.body.scrollHeight,c=d>a.height?"true":"false";this.addEntry("canscroll_x",b>a.width?
"true":"false");this.addEntry("canscroll_y",c);this.addEntry("scrollwidth",b);this.addEntry("scrollheight",d)}if(screen.colorDepth)this.addEntry("colorDepth",screen.colorDepth);else screen.pixelDepth&&this.addEntry("colorDepth",screen.pixelDepth)},_hashCheck:function(){var a=window.location.hash;if(a){a=a.replace("#","");this.addEntry("urlhash",a)}(a=window.location.search.match(/[?&]campaign=([^&]*)/))&&this.addEntry("campaign",a[1])},_createPHID:function(){var a=this._cachedVals.ncbi_sessionid.value,
b=a.substr(0,15)+"9"+(new Date).getTime().toString(),d=a.length;b+=a.substr(d-(32-b.length),d);a={value:b};this.addEntry("ncbi_phid",b);return a},currentPageHitId:null,_prevHitCheck:function(){var a=this.getCookie("ncbi_prevPHID"),b=this._cachedVals.ncbi_phid;a.length>0&&this.addEntry("prev_phid",a);if(!b||!b.value||b.value.length===0)b=this._createPHID();this.currentPageHitId=b.value;var d=this;ncbi.sg._hasFocus&&d.setCookie("ncbi_prevPHID",b.value);var c=window.onfocus;window.onfocus=function(f){d.getCookie("ncbi_prevPHID")!==
b.value&&d.setCookie("ncbi_prevPHID",b.value);typeof c==="function"&&c(f)}},_setUpPathParts:function(){var a=this._cachedVals.ncbi_app,b=this._cachedVals.ncbi_db,d=this._cachedVals.ncbi_pdid,c=this._cachedVals.ncbi_pcid;this._pathParts.part1=a!==undefined?a.value:"";this._pathParts.part2=b!==undefined?b.value:"";this._pathParts.part3=d!==undefined?d.value:"";this._pathParts.part4=c!==undefined?c.value:""},getPerfStats:function(){var a=window.performance;if(!a)return{};var b=a.timing;if(b)b={dns:b.domainLookupEnd-
b.domainLookupStart,connect:b.connectEnd-b.connectStart,ttfb:b.responseStart-b.connectEnd,basePage:b.responseEnd-b.responseStart,frontEnd:b.loadEventStart-b.responseEnd};else return{};if(a=a.navigation){b.navType=a.type;b.redirectCount=a.redirectCount}return b},setPerfStats:function(a,b){var d=this.getPerfStats();for(var c in d){var f=d[c];if(f>=0){var g="jsperf_"+c;if(b)a[g]=f;else a.push(g+"="+f)}}},getExtraRenderStats:function(){var a={SELF_URL:encodeURIComponent(window.location.href)};if(typeof document!==
"undefined"&&typeof document.referrer!=="undefined")a.HTTP_REFERER=encodeURIComponent(document.referrer);return a},setExtraRenderStats:function(a){var b=this.getExtraRenderStats();for(var d in b)a.push(d+"="+b[d])},_send:function(a,b,d){if(typeof d==="undefined"||d===null)d=true;var c=[];if(a==="init"){c.push("jsevent=render");ncbi.sg.renderTime=new Date;if(typeof ncbi_startTime!=="undefined"){c.push("jsrendertime="+(ncbi.sg.renderTime-ncbi_startTime));ncbi.sg.loadTime&&c.push("jsloadtime="+(ncbi.sg.loadTime-
ncbi_startTime))}this.setPerfStats(c);this.setExtraRenderStats(c);c.push("cookieenabled="+(ncbi.sg.isCookieEnabled?"true":"false"))}for(var f in this._cachedVals)ncbi.sg.appLogIgnore.indexOf(f)===-1&&c.push(f+"="+encodeURIComponent(this._cachedVals[f].value));this._sendAl(c.join("&"),b,true,d);this._hasInitRun=true;var g=this;setTimeout(function(){g.isProcessRunning=false;g.runSGProcess()},300)},send:function(a,b){this._send(a,b,false)},_sendPrev:function(){var a=ncbi.sg.getInstance(),b=a.getCookie("clicknext");
if(b){ncbi.sg._ping(b);a.setCookie("clicknext","")}if(b=a.getCookie("prevsearch")){ncbi.sg._ping(b);a.setCookie("prevsearch","")}if(b=a.getCookie("unloadnext")){ncbi.sg._ping(b);a.setCookie("unloadnext","")}},_sendAl:function(a,b,d,c){if(typeof c==="undefined"||c===null)c=true;if(a.indexOf("jseventms")===-1)a+="&jseventms="+ncbi.sg.getInstance().getMillisecondsSinceSunday();a.match(/jsevent=search/i)&&this._storeNext("prevsearch",a.replace(/jsevent=search(next)?/i,"jsevent=searchnext"),null,c);a+=
"&sgVersion="+ncbi.sg._version;if(a.indexOf("sgSource")===-1)a=this._setSgSource(a,c);c=window.location.port?":"+window.location.port:"";reqURL=typeof __ncbi_stat_url!=="undefined"?__ncbi_stat_url+"?"+a:window.location.protocol+"//"+window.location.hostname+c+"/stat?"+a;this.makeAjaxCall(reqURL,function(){typeof b==="function"&&b()},d)},sendAl:function(a,b,d){this._sendAl(a,b,d,false)},_processingQueue:[],isProcessRunning:false,addSGProcess:function(a){this._processingQueue.push(a)},getSGProcess:function(a){return this._processingQueue.shift(a)},
runSGProcess:function(){if(this.isProcessRunning||this._processingQueue.length===0||!this._hasInitRun)return false;this.isProcessRunning=true;this.removeAllEntries();var a=this.getSGProcess();this._setUpCachedMetaTagValues(a.metadata);this.addEntry("jsevent",a.eventName);for(var b in a.props)this.addEntry(b,a.props[b]);this._send(a.eventName,a.callbackFnc);var d=this;setTimeout(function(){d.isProcessRunning=false;d.runSGProcess()},300)},noteEventData:function(a,b,d,c){this.addSGProcess({eventName:a,
props:b,metadata:d,callback:c});this.runSGProcess()},setCookie:function(a,b,d){if(window.sessionStorage)try{sessionStorage.setItem(a,b)}catch(c){}var f=new Date;d!==null&&f.setDate(f.getDate()+d);document.cookie=a+"="+escape(b)+(d===null?"":"; expires="+f.toGMTString())+"; domain="+escape(".nih.gov")+"; path=/"},_setSgSource:function(a,b){a+="&sgSource="+(b?"native":"api");return a},_storeNext:function(a,b,d,c){if(typeof c==="undefined"||c===null)c=true;b=this._setSgSource(b,c);this.setCookie(a,b,
d)},getCookie:function(a){var b;if(window.sessionStorage){try{b=sessionStorage.getItem(a)||""}catch(d){b=""}if(b.length>0)return b}if(document.cookie.length>0){b=document.cookie.indexOf(a+"=");if(b!==-1){b=b+a.length+1;a=document.cookie.indexOf(";",b);if(a===-1)a=document.cookie.length;return unescape(document.cookie.substring(b,a))}}return""},getTransport:function(){var a=null;if(window.XMLHttpRequest)try{a=new XMLHttpRequest;this.getTransport=function(){return new XMLHttpRequest}}catch(b){a=null}if(window.ActiveXObject&&
a===null)try{a=new ActiveXObject("Msxml2.XMLHTTP");this.getTransport=function(){return new ActiveXObject("Msxml2.XMLHTTP")}}catch(d){try{a=new ActiveXObject("Microsoft.XMLHTTP");this.getTransport=function(){return new ActiveXObject("Microsoft.XMLHTTP")}}catch(c){a=false}}if(a===null)this.getTransport=function(){return null};return this.getTransport()},makeAjaxCall:function(a,b,d){var c=this.getTransport();c._ncbi_skipOverride=true;c.open("GET",a,d);if(d)c.onreadystatechange=function(){if(c.readyState===
4){ncbi.sg.outstandingPings-=1;b(c)}};if(a.search(/jsevent=(click|search|unload)next/)!==-1||(a.search("jsevent=render")!==-1||a.search("jsevent=domready")!==-1||a.search("jsevent=jserror")!==-1)&&a.search("sgSource=api")===-1)ncbi.sg.pingsFired.push(a);ncbi.sg.lastPing=c;ncbi.sg.outstandingPings+=1;c.send(null);return c},scrollDetails:{maxScroll_x:0,maxScroll_y:0,currScroll_x:0,currScroll_y:0,hasScrolled:false},scrollEventDetails:{xTenths:0,yTenths:0,xMax:0,yMax:0},_getScrollXYPx:function(){return[window.pageXOffset||
document.documentElement.scrollLeft||document.body.scrollLeft||0,window.pageYOffset||document.documentElement.scrollTop||document.body.scrollTop||0]},_getScrollXY:function(){var a=this.getViewportHeight(),b=this.getViewportWidth(),d=document.body.scrollHeight,c=document.body.scrollWidth,f=this._getScrollXYPx(),g=Math.round(f[1]/a*10)/10;return{xRel:Math.round(f[0]/b*10)/10,yRel:g,viewportHeight:a,viewportWidth:b,pageHeight:d,pageWidth:c}},_addOnScrollListeners:function(){var a=window.onscroll,b=this;
window.onscroll=function(){if(ncbi.sg.isScrollingEnabled){b._setScrollDetails();b.scrollDetails.hasScrolled=true;b._addScrollEvent()}else{b._setScrollDetails();b.scrollDetails.hasScrolled=true}if(typeof a==="function")return a()}},getViewportHeight:function(){return window.innerHeight?window.innerHeight:document.documentElement&&document.documentElement.clientHeight?document.documentElement.clientHeight:document.body!==null?document.body.clientHeight:"NA"},getViewportWidth:function(){return window.innerWidth?
window.innerWidth:document.documentElement&&document.documentElement.clientWidth?document.documentElement.clientWidth:document.body!==null?document.body.clientWidth:"NA"},_setScrollDetails:function(){this.scrollDetails.currScroll_y=window.pageYOffset||document.documentElement.scrollTop||document.body.scrollTop||0;this.scrollDetails.currScroll_x=window.pageXOffset||document.documentElement.scrollLeft||document.body.scrollLeft||0;this.getViewportWidth();this.getViewportHeight();if(this.scrollDetails.maxScroll_y<
this.scrollDetails.currScroll_y)this.scrollDetails.maxScroll_y=this.scrollDetails.currScroll_y;if(this.scrollDetails.maxScroll_x<this.scrollDetails.currScroll_x)this.scrollDetails.maxScroll_x=this.scrollDetails.currScroll_x},isVisible:function(a){var b=this.findEffectiveStyleProperty(a,"visibility");a=this._isDisplayed(a);return b!="hidden"&&a},_isDisplayed:function(a){if(this.findEffectiveStyleProperty(a,"display")=="none")return false;if(a.parentNode.style)return this._isDisplayed(a.parentNode);
return true},findEffectiveStyleProperty:function(a,b){var d=this.findEffectiveStyle(a)[b];if(d=="inherit"&&a.parentNode.style)return this.findEffectiveStyleProperty(a.parentNode,b);return d},findEffectiveStyle:function(a){if(a.style!=undefined){if(window.getComputedStyle)return window.getComputedStyle(a,null);if(a.currentStyle)return a.currentStyle;if(window.document.defaultView&&window.document.defaultView.getComputedStyle)return window.document.defaultView.getComputedStyle(a,null);return null}},
findElementPos:function(a,b,d){var c=0,f=0;if(a.offsetLeft)c+=parseInt(a.offsetLeft);if(a.offsetTop)f+=parseInt(a.offsetTop);if(a.scrollTop&&a.scrollTop>0){f-=parseInt(a.scrollTop);d=true}if(a.scrollLeft&&a.scrollLeft>0){c-=parseInt(a.scrollLeft);b=true}if(a.offsetParent){b=this.findElementPos(a.offsetParent,b,d);if(b==-1)return-1;c+=b[0];f+=b[1]}else if(a.ownerDocument){var g=a.ownerDocument.defaultView;if(!g&&a.ownerDocument.parentWindow)g=a.ownerDocument.parentWindow;if(g){var m=g.pageXOffset!==
undefined?g.pageXOffset:(a.document.documentElement||a.document.body.parentNode||a.document.body).scrollLeft;a=g.pageYOffset!==undefined?g.pageYOffset:(a.document.documentElement||a.document.body.parentNode||a.document.body).scrollTop;if(!d&&a&&a>0)f-=parseInt(a);if(!b&&m&&m>0)c-=parseInt(m)}}return[c,f]},addObjData:function(a,b){for(var d in b)a[d]=b[d]},getJoinedData:function(a){var b=[];for(var d in a)b.push(d+"="+encodeURIComponent(a[d]));return b.join("&")},addScrollHeadingData:function(a,b){var d=
this.scrollEventDetails.headings;if(d){a["numHeadings."+this._scrollOrder+".scrollInfo"]=d.length;for(var c=0;c<d.length;c++){var f=d[c];if(f.visible){var g={};if(f.innerText)g["innerText."+f.index+".headings."+this._scrollOrder+".scrollInfo"]=f.innerText;if(f.id)g["id."+f.index+".headings."+this._scrollOrder+".scrollInfo"]=f.id;g["tagName."+f.index+".headings."+this._scrollOrder+".scrollInfo"]=f.tagName;b=b?b:1800;this.getJoinedData(a).length+this.getJoinedData(g).length<b&&this.addObjData(a,g)}}}return a},
getVisibleHeadings:function(){for(var a=[],b=document.getElementsByTagName("*"),d=-1,c=0;c<b.length;c++){for(var f=b[c],g=false,m=f.tagName.toLowerCase(),l=0;l<7;l++)if(m=="h"+l)g=true;if(g){d+=1;l=this.findElementPos(f);g=l[1];l=l[0];var u=f.offsetHeight,z=f.offsetWidth,A=this.getViewportHeight(),h=this.getViewportWidth(),k={},n=f.getAttribute("id")||f.id;if(n)k.id=n;if(n=(f.getAttribute("innerText")||f.innerText||f.getAttribute("textContent")||f.textContent||"").replace(/^\s+|\s+$/g,""))k.innerText=
n.substring(0,50);k.index=d;k.tagName=m;if(g+u>=0&&g<=A&&l+z>=0&&l<=h)if(this.isVisible(f)){k.visible=true;a.push(k)}}}return a},_setBeforeScrollDetails:function(a){this._lastScroll=a?a:null;a=this._getScrollXY();var b=a.yRel;this.scrollEventDetails.xTenths=Math.round(a.xRel*10);this.scrollEventDetails.yTenths=Math.round(b*10);this.scrollEventDetails.xMax=Math.max(this.scrollEventDetails.xTenths,this.scrollEventDetails.xMax);this.scrollEventDetails.yMax=Math.max(this.scrollEventDetails.yTenths,this.scrollEventDetails.yMax);
this.scrollEventDetails.headings=this.getVisibleHeadings()},getScrollDetails:function(a,b){if(!ncbi.sg.isScrollingEnabled){this._setScrollDetails();return this.scrollDetails}var d=this.scrollEventDetails;d.tstamp=(new Date).getTime();var c=this._getScrollXY(),f=null;if(b||!this._lastScroll||d.tstamp-this._lastScroll>1E3){this._scrollOrder=this._scrollOrder!=undefined?this._scrollOrder+1:0;b="yTenths."+this._scrollOrder+".scrollInfo";f="xTenths."+this._scrollOrder+".scrollInfo";var g="maxXTenths."+
this._scrollOrder+".scrollInfo",m="maxYTenths."+this._scrollOrder+".scrollInfo",l={};l["duration."+this._scrollOrder+".scrollInfo"]=this._lastScroll?d.tstamp-this._lastScroll:new Date-ncbi.sg.loadTime;l[f]=this.scrollEventDetails.xTenths;l[b]=this.scrollEventDetails.yTenths;l[g]=this.scrollEventDetails.xMax;l[m]=this.scrollEventDetails.yMax;l["viewportHeight."+this._scrollOrder+".scrollInfo"]=c.viewportHeight;l["viewportWidth."+this._scrollOrder+".scrollInfo"]=c.viewportWidth;l["maxPossibleScrollTenthsY."+
this._scrollOrder+".scrollInfo"]=Math.round((c.pageHeight/c.viewportHeight-1)*10);l["maxPossibleScrollTenthsX."+this._scrollOrder+".scrollInfo"]=Math.round((c.pageWidth/c.viewportWidth-1)*10);f=l=this.addScrollHeadingData(l,a)}this._setBeforeScrollDetails(d.tstamp);return f},getScrollDetailsAr:function(a,b){var d=[];a=this.getScrollDetails(a,b);for(var c in a)d.push(c+"="+encodeURIComponent(a[c]));return d},addScrollDetailsAr:function(a,b,d){b=this.getScrollDetailsAr(b,d);for(d=0;d<b.length;d++)a.push(b[d])},
addScrollDetails:function(a,b,d){b=this.getScrollDetails(b,d);for(var c in b)a[c]=b[c]},trimScrollDetails:function(){},_addScrollEvent:function(){try{var a=this.getScrollDetails();if(a){a.jsevent="scroll";ncbi.sg._ping(a)}}catch(b){ncbi.sg.getInstance().noteEventData("jserror",{jserror:b.toString(),SELF_URL:window.location.href},["ncbi_sessionid","ncbi_phid"])}},_addPrintPing:function(){function a(){var c=document.createElement("style");c.type="text/css";var f=document.createElement("style");f.type=
"text/css";f.media="print";for(var g=["jsevent=print"],m=0;m<b.cachedNames.length;m++){var l=b.cachedNames[m],u=b._cachedVals[l];u&&l!=="ncbi_sessionid"&&g.push(l+"="+encodeURIComponent(u.value))}g=".print-log { position:absolute;left:-10000px;top:auto;width:1px;height:1px;overflow:hidden; }.print-log li { list-style-image: url('/stat?"+g.join("&")+"'); }";if(f.styleSheet){f.styleSheet.cssText=g;c.styleSheet.cssText=".print-log { position:absolute;left:-10000px;top:auto;width:1px;height:1px;overflow:hidden; }"}else{f.appendChild(document.createTextNode(g));
c.appendChild(document.createTextNode(".print-log { position:absolute;left:-10000px;top:auto;width:1px;height:1px;overflow:hidden; }"))}g=document.getElementsByTagName("head")[0];g.appendChild(f);g.appendChild(c);c=document.createElement("ul");c.className="print-log";f=document.createElement("li");c.appendChild(f);document.body.appendChild(c)}var b=this;if(typeof jQuery!=="undefined")jQuery(a);else{var d=window.onload;window.onload=function(c){d&&d(c);a()}}},getMillisecondsSinceSunday:function(){var a=
new Date,b=new Date;b.setDate(b.getDate()-b.getDay());b.setHours(0);b.setMinutes(0);b.setSeconds(0);b.setMilliseconds(0);return(a-b).toString(36)},addLinkObjs:function(a){this._linkObjs.push(a)},isInLinkObjs:function(a){for(var b=this._linkObjs,d=0;d<b.length;d++)for(var c=b[d],f=0;f<c.length;f++)if(a==c[f])return true;return false}};ncbi.sg.lastPing=null;ncbi.sg.hasNotedErrorEvent=false;(function(){function a(){ncbi.sg.getInstance().setCookie("ncbi_prevPHID",ncbi.sg.getInstance().currentPageHitId);
if(z.length>0)for(;z.length>0;)d(z.pop());var e={jsevent:"unload",ncbi_pingaction:"unload"};if(typeof ncbi_startTime!=="undefined"){e.ncbi_timeonpage=new Date-ncbi_startTime;if(typeof ncbi_onloadTime!=="undefined"&&ncbi_onloadTime)e.ncbi_onloadTime=ncbi_onloadTime-ncbi_startTime}var i=ncbi.sg.getInstance();i.setPerfStats(e,true);i.addScrollDetails(e,1800-i.getJoinedData(e).length,true);if(!k){ncbi.sg._ping(e);var r="";for(var p in e)r+=p+"="+(p==="jsevent"?"unloadnext":e[p])+"&";r+="ncbi_phid="+i.currentPageHitId;
i._storeNext("unloadnext",r,null)}k=true}function b(e){for(var i=z.length-1;i>=-1;i--)if(z[i]===e){z.slice(i,1);break}d(e)}function d(e,i){if(A.indexOf(e.tstamp)===-1){A.push(e.tstamp);h.push(e);c("click",e,i)}}function c(e,i,r,p){if(typeof p==="undefined"||p===null)p=true;var q=e==="click"?"link":"elem",s=i.link,j=i.evt,t=s.id||"",y=s.name||"",C=s.sid||"",G=s.href||"",D=s.innerText||s.textContent||"";if(D.length>50)D=D.substr(0,50);var H=s.getAttribute?s.getAttribute("ref")||s.ref||"":"",I=s.className?
s.className.replace(/^\s?/,"").replace(/\s?$/,"").split(/\s/g).join(",")||"":"";i=[];var E=[],B=s.parentNode;if(B)for(var x=0;x<6&&B!==null;x++){(parId=B.id)&&i.push(parId);if(parClassName=B.className)E=E.concat(parClassName.split(/\s/));B=B.parentNode}B=ncbi.sg.getInstance();x=B.currentPageHitId||"";var v=[];t.length>0&&v.push(q+"_id="+encodeURIComponent(t));y.length>0&&v.push(q+"_name="+encodeURIComponent(y));C.length>0&&v.push(q+"_sid="+encodeURIComponent(C));G.length>0&&v.push(q+"_href="+encodeURIComponent(G));
D.length>0&&v.push(q+"_text="+encodeURIComponent(D));I.length>0&&v.push(q+"_class="+encodeURIComponent(I));x=B.getBrowserWidthHeight();x.width!==null&&v.push("browserwidth="+encodeURIComponent(x.width));x.height!==null&&v.push("browserheight="+encodeURIComponent(x.height));for(var F in j){x=j[F];x!==undefined&&v.push(F.toLowerCase()+"="+x.toString())}v.push("jsevent="+e);H.length>0&&v.push(H);if(typeof jQuery!=="undefined")if(s=jQuery(s).attr("sg")){s=s.split(/\}\s*,\s*\{/);for(x=0;x<s.length;x++){j=
s[x].match(/name\s*:\s*'(.+)',\s*selector\s*:\s*'(.+)'/);if(j.length===3){F="cust_"+j[1];j=jQuery(j[2]).val();v.push(F+"="+encodeURIComponent(j))}}}if(r&&r.length>0)for(;r.length>0;)v.push(r.shift());i.length>0&&v.push("ancestorId="+i.join(","));E.length>0&&v.push("ancestorClassName="+E.join(",").replace(/\s+/g," ").replace(/(^\s|\s$)/g,""));B.addScrollDetailsAr(v,1800-v.join("&").length,true);if(e==="click"){e=v.join("&").replace("jsevent=click","jsevent=clicknext");x=ncbi.sg.getInstance().currentPageHitId||
"";e+="&ncbi_phid="+x;B._storeNext("clicknext",e,null,p)}ncbi.sg._ping(v,true,null,null,p)}function f(e){var i={};if(e){if(e.clientX||e.clientY){var r=ncbi.sg.getInstance()._getScrollXYPx();i.evt_coor_x=e.clientX+r[0];i.evt_coor_y=e.clientY+r[1]}else if(e.pageX||e.pageY){i.evt_coor_x=e.pageX;i.evt_coor_y=e.pageY}i.jseventms=ncbi.sg.getInstance().getMillisecondsSinceSunday()}return i}function g(e,i,r,p,q){var s={},j=null,t=null;if(typeof i==="string"){j=i;t=r}else{s=f(i);j=r;t=p}if(t){i=typeof t;if(i===
"string")t=[t];else if(i==="object"&&!(t instanceof Array)){i=[];for(var y in t)i.push(y+"="+t[y]);t=i}}c(j,{link:e,evt:s},t,q)}function m(e,i,r){var p=[];if(typeof i==="undefined")i=true;if(typeof e==="object"&&!(e instanceof Array))for(var q in e)p.push(q+"="+encodeURIComponent(e[q]));else if(typeof e==="string")p.push(e);else p=e;e=ncbi.sg.getInstance().currentPageHitId||"";q=null;if(typeof ncbi.sg.loadTime!=="undefined")q=new Date-ncbi.sg.loadTime;var s=p.join("&");if(s.indexOf("jsevent=clicknext")!==
-1||s.indexOf("jsevent=searchnext")!==-1||s.indexOf("jsevent=unloadnext")!==-1){e.length>0&&p.push("next_phid="+encodeURIComponent(e));q!==null&&p.push("next_ncbi_timesinceload="+q)}else{e.length>0&&p.push("ncbi_phid="+encodeURIComponent(e));q!==null&&p.push("ncbi_timesinceload="+q)}ncbi.sg.getInstance()._sendAl(p.join("&"),null,i,r)}var l=window.onerror;window.onerror=function(e,i,r){if(!ncbi.sg.hasNotedErrorEvent){ncbi.sg.getInstance().noteEventData("jserror",{jserror:e,jserrorlocation:i,jserrorline:r,
SELF_URL:window.location.href},["ncbi_sessionid","ncbi_phid"]);ncbi.sg.hasNotedErrorEvent=true;if(typeof l==="function")return l(e,i,r)}};var u=window.onbeforeunload;window.onbeforeunload=function(e){a();if(typeof u==="function")return u(e)};var z=[],A=[],h=[],k=false;ncbi.sg.sendElementEvent=function(e,i,r){c(e,i,r,false)};ncbi.sg.clickTimers=[];if(typeof ncbi.sg.isClickEnabled==="undefined"){for(var n=document.getElementsByTagName("meta"),o=n.length-1,w=true;o>=0;){if(n[o].name.toLowerCase()===
"ncbi_clickdisabled"){w=n[o].content.toLowerCase()==="false";break}o--}ncbi.sg.isClickEnabled=w}setClickEvent=function(){var e=function(j){return(j=typeof j.parentNode!=="undefined"?j.parentNode:null)?p(j)?j:e(j):false},i=function(j){j=j.target||j.srcElement;if(typeof j=="undefined"||j==null)return null;if(j.nodeType==3)j=target.parentNode;p(j)||(j=e(j));return j},r=function(j){return ncbi.sg.getInstance().isInLinkObjs(j)},p=function(j){var t=typeof j.tagName!=="undefined"?j.tagName.toLowerCase():
null,y=false,C=false;if(typeof jQuery!=="undefined")y=jQuery(j).is("button, input[type=button], input[type=submit], input[type=reset]");else if(t==="input"){y=inp.type;y=y=="button"||y=="submit"||y=="reset"}else y=t==="button"?true:false;y||(C=t=="a"||t=="area");return C?"link":y?"button":r(j)?"linkObjs":null},q=function(j,t,y,C){if(!(C&&C=="click"&&j.which&&j.which==3))if(!(!t||p(t)==null)){ncbi.sg.getInstance().setCookie("ncbi_prevPHID",ncbi.sg.getInstance().currentPageHitId);j=f(j);j.iscontextmenu=
C=="contextmenu"?"true":"false";t={evt:j,link:t,tstamp:(new Date).getTime(),floodTstamp:(new Date).getTime()};b(t);ncbi.sg.clickTimers&&window.clearTimeout(ncbi.sg.clickTimers);ncbi.sg.clickTimers=window.setTimeout(function(){ncbi.sg.clickTimers=null},300)}};if(window.addEventListener){window.addEventListener("click",function(j){q(j,i(j),[],"click")});window.addEventListener("contextmenu",function(j){q(j,i(j),[],"contextmenu")},false)}else if(window.attachEvent){document.attachEvent("onclick",function(j){q(j,
i(j),[],"click")});document.attachEvent("oncontextmenu",function(j){q(j,i(j),[],"contextmenu")},false)}if(Event.prototype.stopPropagation){var s=Event.prototype.stopPropagation;Event.prototype.stopPropagation=function(){var j=i(this);if(p(j)!=null)if(this.type=="click")q(this,j,[],"click");else this.type=="contextmenu"&&q(this,j,[],"contextmenu");return s.apply(this,arguments)}}};setClickEvent();ncbi.sg.scanLinks=function(e){var i=ncbi.sg.getInstance();if(e){var r=typeof jQuery!=="undefined"&&jQuery?
e instanceof jQuery:false;if(typeof e==="object"&&!(e instanceof Array)&&!r)e=[e];i.addLinkObjs(e)}};ncbi.sg._ping=function(e,i,r,p,q){if(typeof q==="undefined"||q===null)q=true;typeof e==="undefined"||e===null||(typeof e==="object"&&e.nodeName!==undefined?g(e,i,r,p,q):m(e,i,q))};ncbi.sg.ping=function(e,i,r,p){ncbi.sg._ping(e,i,r,p,false)};ncbi.sg.loadTime=new Date;ncbi.sg.pingsFired=[];ncbi.sg.prevPingsFired=null;ncbi.sg.outstandingPings=0;ncbi.sg._isGBPage=false})();if(!Array.prototype.indexOf)Array.prototype.indexOf=
function(a,b){var d=this.length>>>0;b=Number(b)||0;b=b<0?Math.ceil(b):Math.floor(b);if(b<0)b+=d;for(;b<d;b++)if(b in this&&this[b]===a)return b;return-1};(function(){function a(){ncbi_onloadTime=new Date}ncbi_onloadTime=null;if(typeof jQuery!=="undefined")jQuery(window).load(a);else{var b=window.onload;window.onload=function(d){b&&b(d);a()}}})()}})();
(function(){ncbi.sg._ajaxRequestIndex=1;if(typeof XMLHttpRequest!=="undefined"){var a=function(h){if(typeof h==="string"||typeof DOMString!=="undefined"&&h instanceof DOMString||typeof String!=="undefined"&&h instanceof String){var k={};h=h.split("&");for(var n=0;n<h.length;n++)if(h[n].length){var o=h[n].split("="),w=o[0],e="";if(o.length==1)e="";else{e=o[1];if(o.length>2)for(n=2;n<o.length;n++)e+="="+o[n]}k[w]=e}return k}else return false},b=function(h){if(typeof h!=="undefined"&&h){h=h.split("?");
if(h.length<=2)return h;else{for(var k=h[1],n=2;n<h.length;n++)k+="?"+h[n];return[h[0],k]}}return""},d=function(h){h=b(h);return h.length>1?typeof h[1]!=="undefined"&&h[1]?h[1]:"":null},c=function(h){h=b(h);return h.length>0?typeof h[0]!=="undefined"&&h[0]?h[0]:"":""},f=function(h){if(typeof h==="object"&&!(h instanceof Array)){var k=[];for(var n in h)k.push(n+"="+h[n]);return k.join("&")}else return false},g=function(h,k,n){var o=ncbi.sg.getInstance();if(typeof h==="undefined"||!h)return k;if(typeof k!==
"undefined"&&k){o=o.getVal(h);k[h]=o?o:"unknown";if(typeof n!=="undefined"&&n)k[h]+=".0"+ncbi.sg._ajaxRequestIndex}return k},m=function(h,k,n){var o=c(k);k=d(k);k=k!==null?k:"";k=l(h,k,n);return o+"?"+k},l=function(h,k,n){k=a(k);ncbi.sg.getInstance();k=g(h,k,n);return f(k)},u,z=function(h,k,n,o){if(typeof u==="undefined"||!u)u=ncbi.sg.getInstance();k=(k=u.getVal(k))?k:"unknown";if(typeof o!=="undefined"&&o)k+=".0"+ncbi.sg._ajaxRequestIndex;h.setRequestHeader(n,k)},A=XMLHttpRequest.prototype.open;
XMLHttpRequest.prototype.open=function(){var h;if(typeof this._ncbi_skipOverride!=="undefined"&&this._ncbi_skipOverride)h=A.apply(this,arguments);else{h=arguments&&arguments.length>1&&typeof arguments[1]!=="undefined"?arguments[1]:null;if(h!==null&&ncbi.sg._isGBPage){h=m("ncbi_phid",h,true);h=m("ncbi_sessionid",h);arguments[1]=h}h=A.apply(this,arguments);z(this,"ncbi_phid","NCBI-PHID",true);z(this,"ncbi_sessionid","NCBI-SID",false);ncbi.sg._ajaxRequestIndex+=1}return h}}})();
typeof jQuery!=="undefined"&&jQuery(function(){if(ncbi.sg.isHoverEnabled){var a=function(c,f,g){var m=[];if(typeof f==="undefined")f=true;if(typeof c==="object"&&!(c instanceof Array))for(var l in c)m.push(l+"="+encodeURIComponent(c[l]));else if(typeof c==="string")m.push(c);else m=c;c=ncbi.sg.getInstance().currentPageHitId||"";l=null;if(typeof ncbi.sg.loadTime!=="undefined")l=new Date-ncbi.sg.loadTime;m.join("&");c.length>0&&m.push("next_phid="+encodeURIComponent(c));l!==null&&m.push("next_ncbi_timesinceload="+
l);ncbi.sg.getInstance()._sendAl(m.join("&"),null,f,g)},b=ncbi.sg.getInstance().getCookie("hovernext");if(b){a(b);ncbi.sg.getInstance().setCookie("hovernext","")}var d=function(c){var f={};if(c){if(c.clientX||c.clientY){var g=ncbi.sg.getInstance()._getScrollXYPx();f.evt_coor_x=c.clientX+g[0];f.evt_coor_y=c.clientY+g[1]}else if(c.pageX||c.pageY){f.evt_coor_x=c.pageX;f.evt_coor_y=c.pageY}f.jseventms=ncbi.sg.getInstance().getMillisecondsSinceSunday()}return f};jQuery(document).on("mousedown","a[ref*='itool=Abstract-nondef'], #disc_col a",
function(c){c=c.originalEvent;var f=d(c);jQuery(c.target);(new Date).getTime();(new Date).getTime();var g=jQuery(c.target),m=g.id||"",l=g.name||"",u=g.sid||"",z=g.href||"",A=g.innerText||g.textContent||"";if(A.length>50)A=A.substr(0,50);var h=g.getAttribute?g.getAttribute("ref")||g.ref||"":"",k=g.className?g.className.replace(/^\s?/,"").replace(/\s?$/,"").split(/\s/g).join(",")||"":"";c=[];var n=[],o=g.parentNode;if(o)for(var w=0;w<6&&o!==null;w++){(parId=o.id)&&c.push(parId);if(parClassName=o.className)n=
n.concat(parClassName.split(/\s/));o=o.parentNode}o=ncbi.sg.getInstance();var e=o.currentPageHitId||"";e=[];m.length>0&&e.push("link_id="+encodeURIComponent(m));l.length>0&&e.push("link_name="+encodeURIComponent(l));u.length>0&&e.push("link_sid="+encodeURIComponent(u));z.length>0&&e.push("link_href="+encodeURIComponent(z));A.length>0&&e.push("link_text="+encodeURIComponent(A));k.length>0&&e.push("link_class="+encodeURIComponent(k));w=o.getBrowserWidthHeight();w.width!==null&&e.push("browserwidth="+
encodeURIComponent(w.width));w.height!==null&&e.push("browserheight="+encodeURIComponent(w.height));for(var i in f){w=f[i];w!==undefined&&e.push(i.toLowerCase()+"="+w.toString())}e.push("jsevent=hovernext");h.length>0&&e.push(h);if(typeof jQuery!=="undefined")if(f=jQuery(g).attr("sg")){f=f.split(/\}\s*,\s*\{/);for(w=0;w<f.length;w++){g=f[w].match(/name\s*:\s*'(.+)',\s*selector\s*:\s*'(.+)'/);if(g.length===3){i="cust_"+g[1];g=jQuery(g[2]).val();e.push(i+"="+encodeURIComponent(g))}}}c.length>0&&e.push("ancestorId="+
c.join(","));n.length>0&&e.push("ancestorClassName="+n.join(",").replace(/\s+/g," ").replace(/(^\s|\s$)/g,""));o.addScrollDetailsAr(e,1800-e.join("&").length,true);c=e.join("&");e=ncbi.sg.getInstance().currentPageHitId||"";c+="&ncbi_phid="+e;ncbi.sg.getInstance()._storeNext("hovernext",c,null)});jQuery(document).on("mouseup mousedown",function(c){!jQuery(c.target).is("a[ref*='itool=Abstract-nondef']")&&jQuery(c.target).parents("a[ref*='itool=Abstract-nondef']").length==0&&!jQuery(c.target).is("#disc_col a")&&
jQuery(c.target).parents("#disc_col a").length==0&&ncbi.sg.getInstance().setCookie("hovernext","")})}});

;
// This code creates window.console if it doesn't exist.
// It also creates stub functions for those functions that are missing in window.console.
// (Safari implements some but not all of the firebug window.console methods--this implements the rest.)
(function() {
    var names = [ "log", "debug", "info", "warn", "error", "assert", "dir", "dirxml", "group",
                  "groupEnd", "time", "timeEnd", "count", "trace", "profile", "profileEnd" ];

    if (typeof(console) === 'undefined' || typeof console === "function" ) {
      //"typeof function" is needed see PP-769 
      console = {};
    }

    for (var i = 0; i < names.length; ++i) {
       if (typeof(console[names[i]]) === 'undefined') {
          console[names[i]] = function() { return false; };
       }
    }
    ncbi.sg.getInstance().init();                          
})();
