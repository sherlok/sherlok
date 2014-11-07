jQuery(function(){
    jQuery('#rss_createfeed').bind('click',createRssFeed);
    function createRssFeed (e){
        e.preventDefault();
        var oThis = jQuery(this);
	   	var args = {
            'QueryKey': oThis.data('qk'),
            'Db': oThis.data('db'),
            'RssFeedName': jQuery('#rss_name').val(),
            'RssFeedLimit': jQuery('#rss_results').val(),
            'HID': oThis.data('hid')
        };
        Portal.$send('CreateRssFeed',args);
    }  
});

;
(function($){

    $(function() {    

        var theSearchInput = $("#term");
        var originalTerm = $.trim(theSearchInput.val());
        var theForm = jQuery("form").has(theSearchInput);
        var dbNode = theForm.find("#database");
        var currDb = dbNode.val();
        var sbConfig = {};
        try{
            sbConfig = eval("({" + theSearchInput.data("sbconfig") + "})");
        }catch(e){}
        var defaultSubmit =  sbConfig.ds == "yes";
        var searched = false;
        var dbChanged = null; //since db.change is triggered as a work around for JSL-2067 
        var searchModified = false; //this is used to allow searching when something esle changed on the page with out the term changing
    
        if(!$.ncbi)
            $.extend($,{ncbi:{}});
        if(!$.ncbi.searchbar)
            $.extend($.ncbi,{searchbar:{}});
            
        $.extend($.ncbi.searchbar,
            (function(){
                //*****************private ******************/
               function doSearchPing() {
                   try{
                    var cVals = ncbi.sg.getInstance()._cachedVals;
                    var searchDetails = {}
                    searchDetails["jsEvent"] = "search";
                    var app = cVals["ncbi_app"];
                    var db = cVals["ncbi_db"];
                    var pd = cVals["ncbi_pdid"];
                    var pc = cVals["ncbi_pcid"];
                    var sel = dbNode[0];
                    var searchDB = sel.options[sel.selectedIndex].value;
                    var searchText = theSearchInput[0].value;
                    if( app ){ searchDetails["ncbi_app"] = app.value; }
                    if( db ){ searchDetails["ncbi_db"] = db.value; }
                    if( pd ){ searchDetails["ncbi_pdid"] = pd.value; }
                    if( pc ){ searchDetails["ncbi_pcid"] = pc.value; }
                    if( searchDB ){ searchDetails["searchdb"] = searchDB;}
                    if( searchText ){ searchDetails["searchtext"] = searchText;}
                    ncbi.sg.ping( searchDetails );
                   }catch(e){
                       console.log(e);
                   }
                }
                function getSearchUrl(term){
                    var url = "";
                    if (typeof(NCBISearchBar_customSearchUrl) == "function") 
                            url = NCBISearchBar_customSearchUrl();
                    if (!url) {
                        var searchURI = dbNode.find("option:selected").data("search_uri");
                        url = searchURI ?  searchURI.replace('$',term) : 
                             "/" + dbNode.val() + "/" + ( term !="" ? "?term=" + term : "");
                        }
                    return url;
                }
            
                return {
                    //*****************exposed attributes and functions ******************/
                    'theSearchInput':theSearchInput,
                    'theForm':theForm,
                    'dbNode':dbNode,
                    'searched':searched,
                    'setSearchModified':function(){searchModified=true;},
                    'searchModified':function(){return searchModified;},
                    'doSearch':function(e){
                           e.stopPropagation();
                           e.preventDefault();
                           //checking for the searched flag is necessary because the autocompelete control fires on enter key, the form submit also fires on enter key
                           if(searched == false){
                               searched = true;
                               theForm.find('input[type="hidden"][name^="p$"]').attr('disabled', 'disabled');
                               //$("input[name]").not(jQuery(".search_form *")).attr('disabled', 'disabled');
                               if (defaultSubmit)
                                   $.ncbi.searchbar.doSearchPing();
                               else {
                                   var term = $.trim(theSearchInput.val());
                                   if (dbChanged || searchModified || term !== originalTerm){
                                       $.ncbi.searchbar.doSearchPing();
                                       var searchUrl = $.ncbi.searchbar.getSearchUrl(encodeURIComponent(term).replace(/%20/g,'+'));
                                       var doPost = (term.length  > 2000) ? true : false; 
                                       if (doPost){
                                           if (e.data.usepjs){
                                               Portal.$send('PostFrom',{"theForm":theForm,"term":term,"targetUrl":searchUrl.replace(/\?.*/,'')});
                                           }
                                           else{
                                               theForm.attr('action',searchUrl.replace(/\?.*/,''));
                                               theForm.attr('method','post');
                                           }
                                       }
                                       else {
                                           window.location = searchUrl;
                                       }
                                   }
                                   else{ //if (term !== originalTerm){
                                       searched = false;
                                   }
                               }
                           }
                    },
                    'onDbChange':function(e){
                         if (dbChanged === null)
                             dbChanged = false;
                         else
                             dbChanged = true;
                         var optionSel = $(e.target).find("option:selected");
                         var dict = optionSel.data("ac_dict");
                         if (dict){
                             theSearchInput.ncbiautocomplete("option","isEnabled",true).ncbiautocomplete("option","dictionary",dict);
                             theSearchInput.attr("title","Search " + optionSel.text() + ". Use up and down arrows to choose an item from the autocomplete.");
                         }
                         else{
                           theSearchInput.ncbiautocomplete("turnOff",true);
                           theSearchInput.attr("title", "Search " + optionSel.text());
                         }
                         if (defaultSubmit)
                            theForm.attr('action','/' + dbNode.val() + '/');  
                    },
                    'doSearchPing':function(){
                        doSearchPing();
                    },
                    'getSearchUrl':function(term){
                        return getSearchUrl(term);
                    }
                    
                };//end of return 
             })() //end of the self executing anon
        );//end of $.extend($.ncbi.searchbar
    
         function initSearchBar(usepjs){
         	//enable the controls for the back button
            theForm.find('input[type="hidden"][name^="p$"]').removeAttr('disabled');
             if (usepjs)
                 portalSearchBar();
         }
         
        
    
        function portalSearchBar(){
            
            Portal.Portlet.NcbiSearchBar = Portal.Portlet.extend ({
                init:function(path,name,notifier){
                    this.base (path, name, notifier);
                },
                send:{
                    "Cmd":null,
                    "Term":null
                },
                "listen":{
                    "PostFrom":function(sMessage,oData,sSrc){
            		    this.postForm(oData.theForm,oData.term,oData.targetUrl);
            		}
                },
                "postForm":function(theForm,term,targetUrl){
                       //console.log('targetUrl = ' + targetUrl);
                       theForm.attr('action',targetUrl);
                       theForm.attr('method','post');
                       this.send.Cmd({
                	        'cmd' : 'Go'
                        });
                           this.send.Term({
                	        'term' : term
                        });
                        Portal.requestSubmit();
                },
                'getPortletPath':function(){
                    return this.realpath + '.Entrez_SearchBar';
                }
            });
    
        }//portalSearchBar
        


         //portal javascript is required to make a POST when the rest of the app uses portal forms 
         var usepjs = sbConfig.pjs == "yes"; 
         //console.log('sbConfig',sbConfig);
         initSearchBar(usepjs);
         
         dbNode.on("change",$.ncbi.searchbar.onDbChange);
        
        theForm.on("submit",{'usepjs':usepjs},$.ncbi.searchbar.doSearch);
        theSearchInput.on("ncbiautocompleteenter ncbiautocompleteoptionclick", function(){theForm.submit();});
        //a work around for JSL-2067
        dbNode.trigger("change");
        //iOS 8.02 changed behavior on autofocus, should probably check other mobile devices too
        if (sbConfig.afs == "yes" && !/(iPad|iPhone|iPod)/g.test(navigator.userAgent) ){ 
            window.setTimeout(function(){
                try{
                    var x = window.scrollX, y = window.scrollY;
                    var size= originalTerm.length;
                    if (size == 0 || /\s$/.test(originalTerm))
                        theSearchInput.focus()[0].setSelectionRange(size, size);
                    else
                        theSearchInput.focus().val(originalTerm + " ")[0].setSelectionRange(size+1, size+1);
                     window.scrollTo(x, y);
                }
                catch(e){} //setSelectionRange not defined in IE8
            },1);
        }
        
        //set the query changed flag true after a few seconds, still prevents scripted clicking or stuck enter key
        window.setTimeout(function(){$.ncbi.searchbar.setSearchModified();},2000);
         
     });//End of DOM Ready

})(jQuery);

/*
a call back for the 'Turn off' link at the bottom of the auto complete list
*/
function NcbiSearchBarAutoComplCtrl(){
    jQuery("#term").ncbiautocomplete("turnOff",true);
    if (typeof(NcbiSearchBarSaveAutoCompState) == 'function')
        NcbiSearchBarSaveAutoCompState();
 }

 



;
jQuery(function () {
    Portal.Portlet.Entrez_SearchBar = Portal.Portlet.NcbiSearchBar.extend ({
        init:function(path,name,notifier){
            this.base (path, name, notifier);
            var oThis = this;
            jQuery("#database").on("change", function(){
                oThis.send.DbChanged({'db' : this.value});
            });
        },
        send:{
            "Cmd":null,
            "Term":null,
            "DbChanged":null
        },
        'listen':{
            "PostFrom":function(sMessage,oData,sSrc){
        	    this.postForm(oData.theForm,oData.term,oData.targetUrl);
        	    },
            "ChangeAutoCompleteState": function(sMessage, oData, sSrc) {
        	    this.ChangeAutoCompleteState(sMessage, oData, sSrc);
                },
            'CreateRssFeed':function(sMessage,oData,sSrc){
                this.createRssFeed(sMessage,oData,sSrc);
            },
            'AppendTerm': function(sMessage, oData, sSrc) {
    		    this.ProcessAppendTerm(sMessage, oData, sSrc);
    		},
    		// to allow any other portlet to clear term if needed  
    		'ClearSearchBarTerm': function(sMessage, oData, sSrc) {
    			jQuery("#term").val("");
    		},
    		// request current search bar term to be broadcast  
    		'SendSearchBarTerm': function(sMessage, oData, sSrc) {
    			this.send.Term({'term' : jQuery("#term").val()});
    		}
        },
        'createRssFeed':function(sMessage,oData,sSrc){
            
            var site = document.forms[0]['p$st'].value;
    	   	var portletPath = this.getPortletPath();
    	   	
            try{
                var resp = xmlHttpCall(site, portletPath, 'CreateRssFeed', oData, receiveRss, {}, this);
            }
            catch (err){
                alert ('Could not create RSS feed.');
            }
            function receiveRss(responseObject, userArgs) {
        	    try{
            	    //Handle timeouts 
            	    if(responseObject.status == 408){
            	        //display an error indicating a server timeout
            	        alert('RSS feed creation timed out.');
            	    }
            	    
            	    // deserialize the string with the JSON object 
            	    var response = '(' + responseObject.responseText + ')'; 
            	    var JSONobject = eval(response);
            	    // display link to feed
            	    jQuery('#rss_menu').html(JSONobject.Output,true);
            	    //jQuery('#rss_dropdown a.jig-ncbipopper').trigger('click');
            	    jQuery('#rss_dropdown a.jig-ncbipopper').ncbipopper('open');
            	    //document.getElementById('rss_menu').innerHTML = JSONobject.Output;
                }
                catch(e){
                    alert('RSS unavailable.');
                }
            }
                
        },
        'getPortletPath':function(){
            return this.realpath + '.Entrez_SearchBar';
        },
        "ChangeAutoCompleteState": function(sMessage, oData, sSrc){
            var site = document.forms[0]['p$st'].value;
            var resp = xmlHttpCall(site, this.getPortletPath(), "ChangeAutoCompleteState", {"ShowAutoComplete": 'false'}, function(){}, {}, this);
        },
        "ProcessAppendTerm" : function(sMessage, oData, sSrc){
            var theInput = jQuery("#term");
    	    var newTerm = theInput.val();
    	    if (newTerm != '' && oData.op != ''){
    	        newTerm = '(' + newTerm + ') ' + oData.op + ' ';
    	    }
    	    newTerm += oData.term;
    	    theInput.val(newTerm); 
    	    
    	    theInput.focus();
    	}
    }); //end of Portlet.extend
}); //end of jQuery ready

function NcbiSearchBarSaveAutoCompState(){
    Portal.$send('ChangeAutoCompleteState');
}


;
jQuery(function () {
Portal.Portlet.Pubmed_SearchBar = Portal.Portlet.Entrez_SearchBar.extend ({
  
	init: function (path, name, notifier) {
		this.base (path, name, notifier);
	},
	
	/* ######### this is a hack. See detailed comment on same function in base */
	"getPortletPath" : function(){
	    return (this.realname + ".Entrez_SearchBar");
	}
});
});


;
Portal.Portlet.Pubmed_PageController = Portal.Portlet.extend({

	init: function(path, name, notifier) {
		var oThis = this;
		this.base(path, name, notifier);
    },
    listen:{
        'SpecialPageName':function(sMessage, oData, sSrc){
			this.setValue("SpecialPageName", oData.SpecialPageName);
		}
    }
});

;
/* I hate to do this, but it is just faster to create the version in Common Components now. Later settle on the interface and remove the versions from PMH 
This will likely be moved to jig anyway
*/

/********
Add a class 'ncbi_share' to your anchor to get the NCBI share popup. The URL shared will be the href of the anchor or the URL of the page if href is empty or '#'
If html attached after render, call $.ncbi.share.scanNcbiSocial(rootNode);

By default the widget attaches a popup with the sharing options. To get an inplace list of sharing buttons set the popup option to false as follows

<a href="#" class="ncbi_share" style="visibility:hidden" data-ncbi_share_config="popup:false">Share1</a>

The anchor <a> will be replaced with an unordered list <ul> containing the icons to the social media sites (twitter,facebook,google+)

To override behavior in js

    $.extend($.ncbi.share,{
        'sharePopupHtml': 'Your Share popup html';
        'shareLabel':'Your share text / label',
        'urlLabel':'Your url text / label'
    });
    
To do that on the server side, which also allows to have different share and url texts / labels on each poupup, 
add a data attribute to the anchor triggering the pop up e.g. 
<a href="/pubmed/" class="ncbi_share" data-ncbi_share_config="share_label:'Share',url_label:'URL'">Share1</a>
<a href="/pubmed/" class="ncbi_share" data-ncbi_share_config="share_label:'Spread the truth',url_label:'Permalink'">Share2</a>

Other configuration parameters (data-ncbi_share_config):
- shortern:true : false by default. If set to true, the URL to be shared is shortened by the bit.ly service

The value of the parameters passed to the sites can be overriden as follows

    $.ncbi.share.setProcessShareParamFunc(function (shareName,paramKey,paramValue,node){
        if (shareName == 'twitter' && paramKey == 'text'){
            return 'New Text';
        }
        else
            return paramValue;
    });

After customization, you may have to adjust the corresponding css

**/

(function ($){

        // Create a "namespace" for our use:
        var socialButtons = {
        };
        
        socialButtons.shareServices = {
            facebook: {
                postUrl: 'http://www.facebook.com/sharer.php',
                shareParameters: {
                    u: 'shareURLPlaceholder'
                    /*p[url] and p[title]*/
                },
                width: 655,
                height: 430
            },
            twitter: {
                postUrl: 'https://twitter.com/intent/tweet',
                shareParameters: {
                    url: 'shareURLPlaceholder',
                    text: jQuery('meta[property="og:title"]').length ?
                        jQuery('meta[property="og:title"]').attr('content'): document.title,
                    related: jQuery('meta[name="twitter:site"]').length ? jQuery('meta[name="twitter:site"]').attr('content').substring(1): ''
                },
                width: 600,
                height: 450
            },
            google: {
                postUrl: 'https://plus.google.com/share',
                shareParameters: {
                    url: 'shareURLPlaceholder',
                    hl: 'en-US'
                },
                width: 600,
                height: 600
            },
            reddit: {
                postUrl: 'http://reddit.com/submit',
                shareParameters: {
                    url: 'shareURLPlaceholder',
                    hl: 'en-US'
                },
                width: 600,
                height: 600
            }
        };
        
        socialButtons.socialShare = function (shareName, shareURL,node) {
            var postUrlTuple = socialButtons.buildUrl(shareName, shareURL,node);
            if(socialButtons.readShareWidgetConfig(node.closest('.social-buttons').data('ncbi_share_config')).shorten == true){
                //console.log('postUrl',postUrlTuple);
                socialButtons.shortenAndOpenShareWindow(shareName,postUrlTuple);
            }
            else{
                socialButtons.openShareWindow(shareName,postUrlTuple);
            }
        };
        
        socialButtons.shortenAndOpenShareWindow = function(shareName,postUrlTuple){
            var paramsArr = postUrlTuple[1];
            var len = paramsArr.length,urlInd=0,urlParamName,urlVal;
            for (var i=0 ; i<len; i++){
                var res = paramsArr[i].match(/^(u|url)=(.*)/);
                if (res){
                    urlInd = i;
                    urlParamName = res[1];
                    urlVal = res[2];
                    break;
                }
            }
            var longUrl = decodeURIComponent(urlVal),shortUrl = longUrl;
            $.ajax({
                    url:'http://api.bit.ly/v3/shorten',
                    dataType:'jsonp',
                    timeout: 5000,
                    async:false,//doesn't work for jsonp
                    data:{
                        longUrl:longUrl,
                        apiKey:'R_bdcabe9f3ec74d680ad066b4c49dd90f',
                        login:'abebaw'
                    }
                }
            ).done(function(res){
                //console.log('socialButtons.shorten.done',res,res.data.url);
                if(res.status_code == 200){
                    shortUrl = res.data.url;
                    //console.log('shortUrl - 1 - ',shortUrl);
                    paramsArr[urlInd]= urlParamName + '=' + encodeURIComponent(shortUrl);
                }     
            }).fail(function(e){
                //console.log('socialButtons.shorten.fail',e);
            }).always(function(){
                socialButtons.openShareWindow(shareName,postUrlTuple);
            });
            
            
        }
        
        socialButtons.openShareWindow = function(shareName,postUrlTuple){
            var defaultWidth = 655;
            var defaultHeight = 600;
            var options = socialButtons.shareServices[shareName];
            var width = options.width ? options.width: defaultWidth;
            var height = options.height ? options.height: defaultHeight;
            var postUrl = postUrlTuple[0] + socialButtons.paramChar(postUrlTuple[0]) + postUrlTuple[1].join('&amp;');
            window.open(postUrl, shareName + 'Share', 'toolbar=0,status=0,height=' + height + ',width=' + width + ',scrollbars=yes,resizable=yes');
        }
        
        socialButtons.buildUrl = function (shareName, shareURL,node) {
            var options = socialButtons.shareServices[shareName];
            var urlToShare = shareURL && shareURL !== '' && shareURL !== '#' ? shareURL: window.location.href.replace(/#.*/, '');
            var parameters =[];
            var postUrl = options.postUrl;
            
            // collect the query string parameters (url, etc.):
            jQuery.each(options.shareParameters, function (paramKey, paramValue) {
                // walk through parameters
                paramValue = socialButtons.processShareParam(shareName,paramKey,paramValue,node);
                if (paramKey == 'url' || paramValue == 'shareURLPlaceholder'){
                    paramValue = paramValue == 'shareURLPlaceholder' ? urlToShare: paramValue;
                }
                if (paramValue !== '') {
                    parameters.push(paramKey + '=' + encodeURIComponent(paramValue));
                }
            });
            // assemble the parameters:
            //parameters = parameters.join('&amp;');
            // append them to the URL:
            //postUrl = postUrl + socialButtons.paramChar(postUrl) + parameters;
            //return postUrl;
            return [postUrl,parameters];
        };
        
        //override to change the default values of the params
        socialButtons.processShareParam = function (shareName,paramKey,paramValue,node){
            return paramValue;
        };
        
        socialButtons.paramChar = function (paramString) {
            return paramString.indexOf('?') !== -1 ? '&amp;': '?';
        };
        
        socialButtons.readShareWidgetConfig = function (configStr){
            var shareConfigJ = {}
            if (configStr) {
                try{
                    shareConfigJ = eval('({' + configStr + '})');
                }catch(e){}
            }
            return shareConfigJ;
        }

         if(!$.ncbi)
            $.extend($,{ncbi:{}});
        if(!$.ncbi.share)
            $.extend($.ncbi,{share:{}});
            
        $.extend($.ncbi.share,
            (function(){
                /*******************private attributes / functions ********************/                
                var _shareLabel = 'Share';
                var _urlLabel = 'URL';
                var _shareUL = '<ul class="social-buttons inline_list">' +
                                '<li><button data-share="Facebook" title="Share on Facebook" class="share_facebook">Share on Facebook</button></li>' +
                                '<li><button data-share="Twitter" title="Share on Twitter" class="share_twitter">Share on Twitter</button></li>' +
                                '<li><button data-share="Google" title="Share on Google+" class="share_google">Share on Google+</button></li>' +
                           '</ul>';
                var _sharePopup = '<div style="display:none;" id="ncbi_share_box"> ' +
                       '<label id="ncbi_share_label">Share</label>' +
                        _shareUL + 
                       '<span class="clearfix"/>' +
                       '<label for="ncbi_share_inp" id="ncbi_share_url_label">URL</label>' +
                       '<input type="text" id="ncbi_share_inp"/>' +
                  '</div>';
                return {
                    //*****************exposed attributes / functions ******************/
                    'sharePopupHtml':_sharePopup,
                    'shareLabel':_shareLabel,
                    'urlLabel':_urlLabel,
                    'shareUL':_shareUL,
                    'setProcessShareParamFunc':function(func){socialButtons.processShareParam=func;},
                    'beforeShare':function(node){},
                    'scanNcbiSocial':function(root){
                        root = root || document;
                        //append the popup if not already there
                        if (!$('#ncbi_share_box')[0]){
                              $($.ncbi.share.sharePopupHtml).insertAfter($('body').children().last());
                        }
                        var allShareNodes = $(root).find('.ncbi_share');
                        //attach the popups
                        var sharePNodes = allShareNodes.not('.jig-ncbipopper').filter(function(ind){
                            return socialButtons.readShareWidgetConfig($(this).data('ncbi_share_config')).popup !== false;
                        });
                        sharePNodes.addClass('jig-ncbipopper').data('jigconfig',"destSelector: '#ncbi_share_box',closeEvent : 'click', openEvent: 'click', " +
                                "adjustFit: 'autoAdjust', openAnimation: 'none',addCloseButton:true, hasArrow:true, destPosition: 'top left', " + 
                                "triggerPosition: 'bottom center', arrowDirection: 'top'");
                        $.ui.jig.scan(sharePNodes);
                        ncbi.sg.scanLinks(sharePNodes.get());
                        
                        //now the inplace share buttons
                        var shareInPNodes = allShareNodes.filter(function(ind){
                            return socialButtons.readShareWidgetConfig($(this).data('ncbi_share_config')).popup === false;
                        });
                        shareInPNodes.each(function(index,elem){
                           var self = $(this);
                           var dataConfig = self.data('ncbi_share_config');
                           self.replaceWith($($.ncbi.share.shareUL).data('ncbi_share_config',dataConfig));
                        });
                        
                        if(isTouchDevice){
                            allShareNodes.each(function(index,elem){
                               var self = $(this);
                               var url = self.attr('href');
                               self.attr('href','#');
                               self.data('share_link',url);
                            });
                        }
                    }
                };//end of return 
             })() //end of the self executing anon
        );//end of $.extend($.ncbi.share
        
     var isTouchDevice = !!('ontouchstart' in window);

     //DOM Ready begin
     $(function(){
        
        //scan for social button links
        $.ncbi.share.scanNcbiSocial();
        
        //event listners
        var $doc = $(document);
        $doc.on('click','.social-buttons button',function(e){
            e.preventDefault();
            var self = $(this);
            $.ncbi.share.beforeShare(self);
            var shareURL = self.closest('#ncbi_share_box').find('#ncbi_share_inp').val();
            socialButtons.socialShare(self.data('share').toLowerCase(), shareURL,self);
        });
        
        $doc.on('ncbipopperopen','.ncbi_share',function(e){
            var self = $(this);
            var shareConfig = self.data('ncbi_share_config');
            var shareConfigJ = socialButtons.readShareWidgetConfig(shareConfig);
            var shareURL = isTouchDevice ? self.data('share_link') : self.attr('href');
            shareURL = shareURL !== '' && shareURL !== '#' ? shareURL: window.location.href;
            $('#ncbi_share_label').text(shareConfigJ.share_label ? shareConfigJ.share_label : $.ncbi.share.shareLabel);
            $('#ncbi_share_url_label').text(shareConfigJ.url_label ? shareConfigJ.url_label : $.ncbi.share.urlLabel);
            $('#ncbi_share_box button').attr('ref',self.attr('ref') + '&link_href=' + shareURL);
            $('#ncbi_share_box .social-buttons').data('ncbi_share_config',shareConfig);
            var txtInput = $('#ncbi_share_inp').val(shareURL);
            window.setTimeout(function(){
                txtInput.select();
            },10);
        }) 
        
        if(isTouchDevice){
            $(document).on('touchstart', function (e) {
                if (!$(e.target).closest('#ncbi_share_box').length) {
                    $('.ncbi_share').ncbipopper('close');
                }
            });
        }
        
        
    });//DOM ready
})(jQuery);




;

(function($){

    $(function(){
    
        //fix for popper - JSL-2324
        $.ui.ncbipopper.prototype._removeSharingDetails = function () {
            var popper = this.getDestElement();
            var sharedPopper = popper.data("popperTriggers");
            var domTrigger = this.element[0];
            for (var i = 0; i < sharedPopper.length; i++) {
                if (sharedPopper[i][0] == domTrigger) {
                    sharedPopper.splice(i, 1);
                    break;
                }
            }
        
            if (sharedPopper.length === 0) {
                popper.removeData("popperTriggers");
                console.info("removing popperTriggers");
            } else {
                popper.data("popperTriggers", sharedPopper);
            }
        };
        
        if (!window.JSON) {
            window.JSON = {
                parse: function (sJSON) { return eval("(" + sJSON + ")"); }
            }
        }
    
        if(!$.ncbi)
            $.extend($,{ncbi:{}});
        if(!$.ncbi.authorpreview)
            $.extend($.ncbi,{authorpreview:{}});

        $.extend($.ncbi.authorpreview,
            (function(){
                /*******************private attributes********************/
                var _timeout = 5000;
                var _ncbi_phid=$('meta[name=ncbi_phid]')[0].content;
                function _getUrlEndPoint(){
                    return '/myncbi/comments/';
                }
                function _makeReqPL(reqParams){
                    var refReqPL = reqParams.Server == 2 ? {p$rq:'CommL.CommServer2:com','ncbi_phid':_ncbi_phid} : 
                        {p$rq:'CommL.CommServer:com','ncbi_phid':_ncbi_phid};
                    return $.extend(refReqPL,reqParams);
                }
                function _ajax(reqParams,dataType,async,timeout,type){
                    async = (typeof async == 'undefined') ? true : async;
                    return $.ajax({
                        url:_getUrlEndPoint(),
                        timeout:timeout || _timeout,
                        dataType:dataType || 'json',
                        async:async,
                        type: type == 'POST' ? 'POST' : 'GET',
                        data:_makeReqPL(reqParams)
                    });
                }
                function _ajaxFailHandler(e){
                    e = e || '';
                    console.error('$.ncbi.authorpreview.ajaxFailHandler - ',e);
                }
                return {
                    //*****************exposed attributes******************/
                    //*****************exposed functions******************/
                    'getAuthorPreview':function(userName,cmId,placeHolder){
                        _ajax({cmd:'author_preview','UserName':userName,'cmid':cmId,'Server':2
                        },'html',false).done(function(data){
                            data = JSON.parse(data);
                            if (data.s == 'y')
                                placeHolder = data.preview;
                            else
                                $.ncbi.authorpreview.ajaxFailHandler('data fetch error');
                        }).fail(function(e){
                            $.ncbi.authorpreview.ajaxFailHandler(e);
                        });
                        return placeHolder;
                    },
                    'ajax':function(reqParams,dataType,async,timeout,type){
                      return _ajax(reqParams,dataType,async,timeout,type);  
                    },
                    'ajaxFailHandler':function(e){
                        _ajaxFailHandler(e);
                    },
                    'scanAuthorLinks':function(root){
                        if (!isTouchDevice){
                            //append the author_preview popup if not already there
                            if (!$('#author_preview')[0]){
                                var popup = '<div id="author_preview" style="display:none;"><p id="ap_content">Loading ... </p></div>';
                                $(popup).insertAfter($('body').children().last()).css('z-index',1005);
                            }
                            root = root || document;
                            //attach the author preview popups
                            var apNodes = $(root).find('.ncbi_author').not('.jig-ncbipopper');
                            apNodes.addClass('jig-ncbipopper').ncbipopper({destSelector: '#author_preview',width:'300px',closeEvent : 'mouseout', openEvent: 'mouseenterap'
                                ,adjustFit: 'autoAdjust', openAnimation: 'none',closeAnimation: 'none',isDocumentCloseClick:true,hasArrow:true});    
                            $.ui.jig.scan(apNodes);
                        }
                    }
    
                };//end of return 
             })() //end of the self executing anon
        );//end of $.extend($.ncbi.authorpreview
      
       
       //execute on DOM ready
       var isTouchDevice = !!('ontouchstart' in window);
        if (!isTouchDevice){
            $.ncbi.authorpreview.scanAuthorLinks();
            
            //event handlers
            var ncbiAuthorSel = '.ncbi_author';
            var $doc = $(document);
            function getAPContent(self){
                var userName = '';
                try{
                    userName = decodeURIComponent(self.attr('href').match('/myncbi/([^/]*)')[1]);
                }catch(e){}
                var authorPreview = '<p id="ap_content">Preview currently not available</p>';
                if (userName){
                    //find the cm if there is one
                    //var cmId = self.closest('.comm_item:not(:has(.comm_ref))').data('cmid'); 
                    var cmId = undefined;
                    if (!self.parent().is('.comm_ref'))
                        cmId = self.closest('.comm_item').not(':has(.not_appr)').data('cmid');
                    var previewFetched = $.ncbi.authorpreview.getAuthorPreview(userName,cmId,authorPreview);
                    window.setTimeout(function(){
                        ncbi.sg.scanLinks($('#ap_content').html(previewFetched).find("a").get());
                    },1);
                    return previewFetched != authorPreview ? true : false;
                }
                else
                    return false;
            }
            $doc.on('ncbipopperopen',ncbiAuthorSel,function(e){
                ncbi.sg.ping(this, e, 'popperopen', ['popper=authorpreview']);
            }).on('click',ncbiAuthorSel,function(e){
               window.location = $(this).attr('href');   
            }).on('mouseenter',ncbiAuthorSel,function(e){
                var self = $(this);
                apCallBackHandle = window.setTimeout(function(){
                    self.trigger('appopperfetch');
                },apIntentTimeout);
            }).on('appopperfetch',ncbiAuthorSel,function(e){
                var self = $(this);
                if (getAPContent(self) == true)
                    self.trigger('mouseenterap');
            }).on('mouseleave',ncbiAuthorSel,function(e){
                clearApCallBack();
            }).on('ncbipopperclose',ncbiAuthorSel,function(e){
                ncbi.sg.ping(this, e, 'popperclose', ['popper=authorpreview']);
            });
            
            var apIntentTimeout = 1000;;
            var apCallBackHandle = null;
            function clearApCallBack(){   
                try{
                    window.clearTimeout(apCallBackHandle);
                }catch(e){}
            }
            
            $doc.on('mouseleave','#author_preview',function(){
               $('.ncbi_author').ncbipopper('close'); 
            });
        }//if (!isTouchDevice){
        
    });//End DOM ready
})(jQuery);






;
(function($){

    $(function(){
    
        if (!window.JSON) {
            window.JSON = {
                parse: function (sJSON) { return eval("(" + sJSON + ")"); }
            }
        }
    
        if(!$.ncbi)
            $.extend($,{ncbi:{}});
        if(!$.ncbi.comments)
            $.extend($.ncbi,{comments:{}});
        $.extend($.ncbi.comments,
            (function(){
                var _recid = '';
                var _pcid = '';
                var _rsys = '';
                var _timeout = 5000;
                var _emailRegexp = /^[A-Za-z0-9._\'%-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,4}$/;
                var _ncbi_phid=$('meta[name=ncbi_phid]')[0].content;
                function _getPostUrl(){
                    return '/myncbi/comments/';
                }
                function _makeReqPL(reqParams){
                    var refReqPL = reqParams.Server == 2 ? {p$rq:'CommL.CommServer2:com','recid':_recid,'rsys':_rsys,'pcid':_pcid,'ncbi_phid':_ncbi_phid} : 
                        {p$rq:'CommL.CommServer:com','recid':_recid,'rsys':_rsys,'pcid':_pcid,'ncbi_phid':_ncbi_phid};
                    return $.extend(refReqPL,reqParams);
                }
                function _ajax(reqParams,dataType,async,timeout){
                    async = (typeof async == 'undefined') ? true : async;
                        return $.ajax({
                            url:_getPostUrl(),
                            timeout: timeout || _timeout,
                            dataType:dataType || 'json',
                            async:async,
                            type:'POST',
                            data:_makeReqPL(reqParams)
                        });
                }
                function _logJsError(e,ajaxRes){
                    var logTxt = ''; //e.toString();
                    function getProperties(obj){
                        var txt = '';
                        for(var x in obj){
                              var p = obj[x];
                              if (typeof p!= 'function')
                                  txt = txt +';' + x + '=' + p;
                        } 
                        return txt;
                    }
                    logTxt = logTxt + getProperties(e) + getProperties(ajaxRes);
                    _ajax({'cmd':'logjserror','JsError':logTxt});
                }
                function _truncateComments(){
                    $('.comm_nlines').each(function(a,b){
                        var cut_off = 3; //IE returns a slightly higher value for scrollHeight when none is needed
                        if (this.scrollHeight > (this.clientHeight + cut_off)){ 
                            var self = $(this);
                            var fullComment = self.html();
                            function getCommentTeaser(htmlText){
                                var cuttoffLen = 180;
                                var thresholdLen = 500;
                                try{
                                    //if the length is big don't bother with the regexs, ff hangs otherwise
                                    if(htmlText.length > thresholdLen){
                                        return htmlText.substr(0,cuttoffLen);
                                    }
                                    else{
                                        htmlText = htmlText.replace(/title="[^"]+"/,'');//start with title now, possibly replace other html attrs
                                        var matchedLines =htmlText.match(/[^\n]*(\n)+[^\n]*(\n)+[^\n]*(\n)+/);
                                        return matchedLines ? matchedLines[0].substr(0,cuttoffLen) : htmlText.substr(0,cuttoffLen);
                                    }
                                }catch(e){
                                    return htmlText;
                                }
                            }
                            self.html('<span>' + getCommentTeaser(fullComment) + '</span><a href="#" class="comm_clipped_more" ref="discoId=ecommons"> ... more</a>').data('full',fullComment).css('max-height','none');
                        }
                    });
                }
                function _collapseBadComments(){
                    $('.comm_nlines').each(function(i,n){
                        var self = $(this);
                        var commItem = self.closest('.comm_item');
                        if (commItem.is(':visible')){
                            var authorNode = commItem.find('.comm_f_name');
                            var authorLink = authorNode.attr('href');
                            //find all comments by the same author
                            var search = true,currItem = commItem, itemsToHide = [];
                            while (search){
                                var currItem = currItem.next('.comm_item');
                                if (currItem.has('.comm_nlines') && currItem.find('.comm_f_name').attr('href') == authorLink){
                                    search = true;
                                    itemsToHide.push(currItem);
                                }
                                else{
                                    search = false;
                                }
                            }
                            var len = itemsToHide.length;
                            if (len > 0){
                                var hideId = 'h_' + commItem.data('cmid');
                                $('<a href="#" class="comm_hide_bad" id="' + hideId +'">More from ' + authorNode.text() + '</a>').insertAfter(commItem);
                                for(var i=0; i<len; i++){
                                    itemsToHide[i].addClass(hideId).hide().find('.comm_nlines').removeClass('comm_nlines');
                                }
                            }        
                        }
                    });
                }
                function _collapseRecipComments(){
                    $('.recip_ineligible').each(function(i,n){
                        var self = $(this);
                        if( self.is(':visible')){
                            var authorNode = self.find('.ncbi_author');
                            var authorLink = authorNode.attr('href');
                            var allSameAuthor = $('.recip').has('.ncbi_author[href*='+ authorLink.split('/')[2] +']');
                            if (allSameAuthor.size() > 1){
                                $('<li class="comm_item recip recip_ineligible">This article was mentioned in comments by <a href="' + authorLink + '" class="ncbi_author">' + authorNode.text() + '</a></li>').insertAfter(self); 
                                allSameAuthor.remove();
                            }
                        }

                    });
                }
            
                return {
                    //*****************exposed attributes******************/
                    'jCP':'', //the comments panel,
                    'jCL':'', //the ul containing the comments list (both on embeded and full pages)
                    'emailRegexp':_emailRegexp,
                     //***************utility methods*****************/
                    'setRecid':function(recid){
                        _recid = recid;
                    },
                    'getRecId':function(){
                        return _recid;  
                    },
                    'setPcId':function(pcid){
                        _pcid = pcid;  
                    },
                    'setRsys':function(rsys){
                        _rsys = rsys;
                    },
                    'page_type':'', //emb or ds
                    'ajax':function(reqParams,dataType,async,timeout){
                            return _ajax(reqParams,dataType,async,timeout);
                    },
                    //'ajaxFailHandler':function(e,noRfrsh,noMsg){
                    'ajaxFailHandler':function(args){
                        var e = args.e || '';
                        e.src = args.src || '';
                        var noRfrsh = args.noRfrsh || false;
                        var noMsg = args.noMsg || false;
                        var ajaxRes = args.ajaxRes || {};
                        if (e == 'blocked_rate'){
                            $('#com_cancel').trigger('click');
                            alert('You have exceeded the limit on number of comments.');
                        }
                        else if (e == 'blocked_ban'){
                            $('#com_cancel').trigger('click');
                            alert('You have exceeded the limit on number of comments.');
                        }
                        else if (noMsg !== true) {
                            _logJsError(e,ajaxRes);
                            alert('Error occured. Please try again.');
                        }
                        else{
                            _logJsError(e,ajaxRes);
                        }
                        if(noRfrsh !== true && !(/^blocked_.+/.test(e)))
                            this.refreshComments(e);
                    },
                    'refreshComments':function(e){
                        if (/^blocked_.+/.test(e)){
                            window.location.reload();
                        } 
                        else if($('#comment_list').data('page') == 'emb')
                            this.fetchComments(null,null,'no'); // the issue with invitee list being empty, so only refresh the list of comments
                        else
                            window.location.reload();
                    },
                    'getUpdatedCommentCountText':function(oldVal,num,set){
                        //if set=true set count, otherwise add
                        var count = 0;
                        if (set == true){
                            count = num;
                        }
                        else{
                            var numC = oldVal.match(/\d+/);
                            count = numC ? parseInt(numC[0])+parseInt(num) : num ;
                        }
                        var title = count == 1 ? 'comment' : 'comments';
                        return [count,title];
                    },
                    'getUserInfo':function(){
                        var user = {};
                        this.ajax({cmd:'user_info'},'json',false).done(function(data){
                            console.dir(data);
                            user = data;
                        });
                        return user;
                    },
                    'convert2Md':function(txt){
                        this.ajax({cmd:'tomd','cmstr':encodeURIComponent(txt)},'json',false).done(function(data){
                            txt = data.md;
                        }).fail(function(e){
                            txt = '';
                            console.log(e);
                            alert('Please check your markup text');
                        });
                        return txt;
                    },
                    'jsonParseComment':function(data){
                        try{
                            data = JSON.parse(data);
                        }catch(e){
                            console.log('JSON parsing error', e);
                            console.dir(e);
                            data = eval('(' + data + ')');
                        }
                        return data;
                    }
                    
                    //***********utility functions manipulating DOM*******************/
                    ,'renderComments':function(node){
                        node.find('a').each(function(){
                            var self = jQuery(this);
                            var oref = self.attr('ref');
                            self.attr('ref',oref ? ( oref.match(/discoId=ecommons/) ? oref : oref + '&discoId=ecommons') :  'discoId=ecommons');
                            });
                        window.setTimeout(function(){
                            if (!location.hash)
                                _collapseBadComments();
                            _truncateComments();
                            _collapseRecipComments();
                            ncbi.sg.scanLinks($.ncbi.comments.jCP.find('a').get());
                            $.ncbi.share.scanNcbiSocial($.ncbi.comments.jCP);
                            $.ncbi.authorpreview.scanAuthorLinks($.ncbi.comments.jCP);
                            $.ui.jig.scanned=false;
                            $.ui.jig.scan($.ncbi.comments.jCP);
                        },10);
                        return node;
                    },
                    'fetchComments':function(sortd,sortf,rfsh){
                        var sortElem = $('#com_sort a.comm_sort');
                        sortd = sortd ||  sortElem.data('sortd') || 'd';
                        sortf = sortf || sortElem.data('sortf') || 'date';
                        rfsh = rfsh || 'no';
                        var ajaxCall = this.ajax({cmd:'get','sortd':sortd,'sortf':sortf,'rfsh':rfsh},'html'); 
                        ajaxCall.done( function(data){
                            data = $.ncbi.comments.jsonParseComment(data);
                            if (data.rfsh == 'yes'){
                                $.ncbi.comments.jCP.replaceWith(data.comments);
                                $('a#show_comments').ncbitoggler('open');
                            }
                            else{
                                $("#comment_list").replaceWith(data.comments);
                            }
                            $.ncbi.comments.jCP.data("loaded",'yes');
                            $.ncbi.comments.renderComments($('#comment_list'));
                            $.ncbi.comments.jCP.trigger('loaded');
                            $.ncbi.comments.setPcId($("#comment_list").data('pcid'));
                            $.ncbi.comments.updateCommentCount(data.ct || 0,true);

                        });
                        ajaxCall.fail(function(e){
                            $.ncbi.comments.ajaxFailHandler({'e':e,'noRfrsh':true,'noMsg':true,'src':'fetchComments.ajaxFail'});
                        });
                    },
                    'updateCommentCount':function(num,set){ 
                       try{
                           var jComList = $('#comment_list');
                           if (jComList.data('page')=='emb'){
                               var jCommCount = this.jCP.find('.num_comments .count');
                               var updatedVal = this.getUpdatedCommentCountText(jCommCount.text(),num,set);
                               jCommCount.text(updatedVal[0]);
                               this.jCP.find('.num_comments')[0].childNodes[1].textContent = ' ' + updatedVal[1];
                           }
                           else if(num == -1 && jComList.data('page')=='ds'){
                               window.location.reload();
                           }
                       }catch(e){
                           console.log(e);
                       }
                    },
                    'hideCommentEditor':function(){
                        $('#com_new').hide().insertAfter($('#comment_list'));
                        $.ncbi.comments.jCL.find('.comm_edit,.comm_delete').css('visibility','visible');
                    },
                    //mode = edit / new / reply 
                    'showCommentEditor':function (opts){//nodeBefore,mode,comment
                        //hide the edit and delete links
                        $.ncbi.comments.jCL.find('.comm_edit,.comm_delete').css('visibility','hidden');
                        opts = $.extend({'nodeBefore':null,'mode':null,'comment':undefined,'pos':'first','parent_id':''},opts);
                        
                        var editor = $('#com_new');
                        if (editor.is(':visible')){
                            if (confirm('Are you sure you want to leave the open editor?')){
                                $('#com_cancel').trigger('click');
                            }
                            else{
                                return;
                            }
                        } 
                        
                        if (typeof opts.comment != 'undefined'){
                            try{
                                opts.comment = decodeURIComponent(opts.comment);
                            }catch(e){
                                console.log(e);
                            }
                        }
                        opts.mode = opts.mode ? opts.mode : 'new';
                        editor.removeClass('reply_indent').css('border','none'); 
                        if(opts.mode == 'edit')
                            editor.css('border','1px solid #E2E2E2');
                            
                        if (opts.nodeBefore)
                            editor.insertAfter(opts.nodeBefore).show().data('mode',opts.mode);
                        else if (opts.pos == 'last')
                            $('#comment_list').append(editor.show().data('mode',opts.mode));
                        else 
                            $('#comment_list').prepend(editor.show().data('mode',opts.mode));
                            
                        var addCommentBox = editor.closest('.add_comment_box').css('width','98%');    
                        if (opts.mode != 'reply'){
                            editor.data('parent-id','');
                        }
                        else{
                            if (!addCommentBox[0])
                                editor.addClass('reply_indent');
                            editor.data('parent-id',opts.parent_id);
                        } 
                        
                        //clear the character limit stuff
                        limitMsgNode.removeClass().addClass('in_limit').text('');
                        saveBtn.ncbibutton('enable');
                        
                        $('#com_new_edit').val(opts.comment).focus();
                    },
                    'updateEditedComment':function(currNode,comment){
                        $.ncbi.comments.renderComments(currNode.replaceWith(comment.find('li.comm_item')));
                    },
                    'saveComment':function(){
                        var editor = $('#com_new');
                        var cmd='add',cmid='',parent_id='',emode=editor.data('mode'),currComm;
                        var cmtxt = $.trim($('#com_new_edit').val());
                        
                        if (cmtxt != ''){
                            var currComm = editor.closest('li.comm_item'); 
                            
                            var commItemIds = getIdsFromCommItem(currComm);
                            
                            
                            var rsys = commItemIds.rSys;
                            if ( emode == 'edit'){
                                cmd = 'update';
                                cmid = commItemIds.cmId;
                            }
                            else if (emode = 'reply'){
                                parent_id = editor.data('parent-id');
                            }
                            var args = {'cmd':cmd,'cmtxt':encodeURIComponent(cmtxt),'cmid':cmid,'rsys':rsys,'parent_id':parent_id,'pcid':commItemIds.pcId,'recid':commItemIds.recId};
                            var ajaxCall = $.ncbi.comments.ajax(args,'html'); 
                            $('#com_cancel').trigger('click');
                            ajaxCall.done( function(data){
                                data = $.ncbi.comments.jsonParseComment(data);
                                if (data.b != 'no'){
                                     $.ncbi.comments.ajaxFailHandler({'e':data.b,'ajaxRes':data,'src':'saveComment.blocked'});
                                }
                                else if(data.success == 'yes'){
                                    //don't just refresh, insert at the right place, Greg has conviced me to do otherise at least for now
                                    if ($('#com_new').data('mode') == 'new')
                                        $.ncbi.comments.updateCommentCount(1);
                                    $.ncbi.comments.refreshComments();
                                }
                                else{
                                     $.ncbi.comments.ajaxFailHandler({'e':'not saved','ajaxRes':data,'src':'saveComment.nosuccess'});
                                }
                                
                            });
                            ajaxCall.fail(function(e){
                                $.ncbi.comments.ajaxFailHandler({'e':e,'src':'saveComment.ajaxFail'});
                            });
                        }
                        else{
                            alert('Please type your comment');
                        }
                    },
                    'deleteComment':function(commItemIds){
                        var ajaxCall = $.ncbi.comments.ajax({'cmd':'del','cmid':commItemIds.cmId,'recid':commItemIds.recId,'rsys':commItemIds.rSys,'pcid':commItemIds.pcId},'json',false); 
                        ajaxCall.done( function(data){
                            if(data.success == 'yes'){
                            }
                            else{
                                 $.ncbi.comments.ajaxFailHandler({'e':'not deleted','ajaxRes':data,'src':'deleteComment.nosucess'});
                            }
                        });
                        ajaxCall.fail(function(e){
                            $.ncbi.comments.ajaxFailHandler({'e':e,'src':'deleteComment.ajaxFail'});
                        });
                    },
                    'abuseDlgSubmitting':function (){
                        $('#abuse_msg').html('Report being submitted ...');
                        $('#abuse_submit').hide();
                        $('#abuse_cancel').hide();
                        $('#report_abuse').find('.ui-ncbipopper-close-button').hide();
                    },
                    'abuseDlgAlreadyReported':function (decisionMade){
                        if(decisionMade){
                            $('#abuse_msg').html('Moderators have determined that this comment does not violate PubMed Commons\' Comment Guidelines. For more information on PubMed Commons Guidelines,' + 
                                ' or to provide further feedback, please see <a href="/pubmedcommons/">http://www.ncbi.nlm.nih.gov/pubmedcommons/</a>. Thank you!');
                        }
                        else
                            $('#abuse_msg').html('You have already reported this comment.');
                        $('#abuse_cancel').show().find('span').text('Close');
                        $('#report_abuse').find('.ui-ncbipopper-close-button').show();
                    },
                    'abuseDlgSuccess':function (){
                        $('#abuse_msg').html('Your report has been submitted for review by the moderators');
                        $('#abuse_cancel').show().find('span').text('Close');
                        $('#report_abuse').find('.ui-ncbipopper-close-button').show();
                    },
                    'abuseDlgFail':function (){
                        $('#abuse_msg').html('Error occured. Please try again');
                        $('#abuse_submit').show();
                        $('#abuse_cancel').show().find('span').text('Cancel');
                        $('#report_abuse').find('.ui-ncbipopper-close-button').show();
                    },
                    'abuseDlgRestore':function (){
                        $('#abuse_submit').show();
                        $('#abuse_cancel').show().find('span').text('Cancel');
                        $('#report_abuse').find('.ui-ncbipopper-close-button').show();
                        $('#abuse_msg').html('');
                    },
                    'abuseDlgValidate':function (email){
                        var sendBtn = $('#abuse_submit');
                        if (!$.ncbi.comments.emailRegexp.test(email)){
                            sendBtn.ncbibutton('disable');
                            return false;
                        }else{
                            sendBtn.ncbibutton('enable');
                            return true;
                        }

                    },
                    'processInvite':function(){
                        if(document.getElementById('cpi_auths').selectedIndex == -1){
                            alert('Select an author first');
                            return;
                        }
                        var email = $.trim($('#comm_invite_email').val());
                        if (!$.ncbi.comments.emailRegexp.test(email)){
                            alert('Please provide a valid email');
                            return;
                        }
                        var selName = $('#cpi_auths').val();
                        
                         this.ajax({cmd:'invite',InviteeEmail:email,InviteeName:selName,
                             InviteeMessage:encodeURIComponent($.trim($('#comm_invite_msg').val()))},'json',false
                         ).fail(function(e){
                             $.ncbi.comments.ajaxFailHandler({'e':e,'noRfrsh':true,'src':'processInvite.ajaxFail'});
                         }).success(function(d){
                             if (d.b != 'no'){
                                 $.ncbi.comments.ajaxFailHandler({'e':d.b,'ajaxRes':d,'src':'processInvite.blocked'});
                             }
                             else if(d.s == 'y'){
                                 //alert('Your invitation has been sent');
                                 //$('.comm_invite').ncbipopper('close');
                                 $('#comm_invite_sys_msg').html('Your invitation has been sent.');
                                 $('#comm_invite_send').find('span').text('OK');  
                             }
                             else{
                                 $.ncbi.comments.ajaxFailHandler({'e':'error creating an invitation','noRfrsh':true,'ajaxRes':d,'src':'processInvite.nosucess'});
                             }
                         });
                    },
                    'checkUnsavedComment':function(){
                        if ($('#com_new').is(':visible') && $('#com_new_edit').val() != '')
                            return true;
                        else
                            return false;
                    },
                    'getStaticContent':function(cpath){
                        var content = '';
                        this.ajax({cmd:'get_content','cpath':cpath},'json',false).fail(function(e){
                             //don't do anything
                         }).success(function(d){
                            content = d.content
                         });
                         return content;
                    }
                    
                    
                };//end of return 
             })() //end of the self executing anon
        );//end of $.extend($.ncbi.comments
    
        //utility functions and variables
        var jCP = $('#comments_panel');
        $.ncbi.comments.jCP = jCP;
        $.ncbi.comments.setRecid(jCP.data('recid'));
        $.ncbi.comments.setRsys(jCP.data('rsys'));
        $.ui.jig.requiresLoginURL = "/account/signin/?inlinelogin=true&p$debugoutput=off";
        $.ncbi.comments.jCL = $('.comment_list');
        $.ncbi.comments.page_type = $('#comment_list').data('page');
        
        //override share behaviour
        $.ncbi.share.setProcessShareParamFunc(function (shareName,paramKey,paramValue,node){
            try{
                if (shareName == 'twitter' && paramKey == 'text'){
                    var popUp = node.closest('#ncbi_share_box');
                    if (popUp[0]){
                        var commItem = popUp.data('attachedTo').closest('.comm_item');
                        var author = commItem.find('.comm_header').not('.comm_ref').find('.comm_f_name').text();
                        if ($.ncbi.comments.page_type == 'ds'){
                            var jr = commItem.find('.cit_line2 span:nth(1)').text();
                            paramValue = author + ' commented on ' + jr;
                        }
                        else if ($.ncbi.comments.page_type == 'emb'){
                            var jrNode = $('.abstract .cit')
                            var jr = jrNode[0].childNodes[0].textContent + ' ' + $.trim(jrNode[0].childNodes[1].textContent).match(/\S*/)[0];
                            paramValue = author + ' commented on ' + jr;
                        }
                    }
                }
            }catch(e){}
            
            return paramValue;
        });
    
 
        //event handlers
        
        var $doc = $(document);
        
        $("a#show_comments").live("ncbitoggleropen",function(e){
            if (jCP.data("loaded") != 'yes')
                $.ncbi.comments.fetchComments();            
        });
        
        $('a#show_comments').live('ncbitoggleropen ncbitogglerclosed',function(e){
            $.post('/myncbi/session-state/', { section_name: 'show_comments', new_section_state: '' + jQuery(e.target).hasClass('ui-ncbitoggler-open') });
        });
        
        
        $('#comm_sort_options a').live('click',function(e){
           e.preventDefault();
           var self = $(this);
           $('#com_sort a.comm_sort').data('sortf',self.data('sortf')).html(self.text() + '<span/>').ncbipopper('close');
           $.ncbi.comments.fetchComments();
        });
        
        $(".add_comment").live("click",function(e){
            e.preventDefault();
            var addBtn = $(this).hide();
            var opts = {};
            opts['nodeBefore'] = addBtn;
            if (addBtn.data('rcmid')){
                opts['parent_id'] = addBtn.data('rcmid');
                opts['mode'] = 'reply';
            }
            $.ncbi.comments.showCommentEditor(opts);
        });
        
        $(".comm_reply").live("click",function(e){
            e.preventDefault();
            //get the containing comment
            var self = $(this);
            var commItem = self.closest('.comm_item');
            //check if there is are replies already
            var replies = commItem.next('.reply_indent');
            if (replies[0]){
                replies.find('.add_comment').trigger('click');
            }
            else{
                commItem.find('.reply_cont').insertAfter(commItem).show().addClass('reply_indent').find('.add_comment').trigger('click')[0].scrollIntoView(true);
            }
            //$.ncbi.comments.showCommentEditor({'nodeBefore':$(this),'mode':'reply','parent_id':$(this).data('rcmid')});
        });
        
        $('#com_cancel').live('click',function(e){
            e.preventDefault();
            var editor = $('#com_new');
            if (editor.data('mode') == 'edit'){
                editor.siblings('.comm_content').show();
            }
            else{
                editor.closest('.add_comment_box').find('.add_comment').show().closest('.add_comment_box').css('width','40%');
            }
            
            $('#com_new_edit').show().val('');
            $('#com_new_view').hide().html('');
            $('#com_preview').text('Preview');
            $.ncbi.comments.hideCommentEditor();
        });
        
        $('#com_preview').live('click',function(e){
            e.preventDefault();
            var self = $(this);
            var editTA = $('#com_new_edit');
            var previewDiv = $('#com_new_view');
            
            if (editTA.is(':visible')){
                self.text('Edit');
                var text = editTA.hide().val();
                var html = $.ncbi.comments.convert2Md(text);
                previewDiv.show().html(html);
            }
            else{
                self.text('Preview');
                previewDiv.hide();
                editTA.show();
            }
            
        });
        
        $('#com_save').live('click',function(e){
            var self = $(this);
            e.preventDefault();
            self.ncbibutton('disable');
            $.ncbi.comments.saveComment();
            window.setTimeout(function(){self.ncbibutton('enable');},1000);
        });
        
        $.ncbi.comments.jCL.find('.comm_edit').live('click',function(e){
            e.preventDefault();
            var self = $(e.target);
            var jCom = self.closest('.comm_item');
            var jComContent = jCom.find('.comm_content').hide();
            $.ncbi.comments.showCommentEditor({'nodeBefore':jComContent,'mode':'edit','comment':jComContent.data('md')});
        });
        
        $.ncbi.comments.jCL.find('.comm_delete').live('click',function(e){
            e.preventDefault();
            if (confirm('Are you sure you want to delete the comment?')){
                var self = $(e.target);
                var currComm = self.closest('.comm_item');
                var commItemIds = getIdsFromCommItem(currComm);
                $.ncbi.comments.deleteComment(commItemIds);
                $.ncbi.comments.updateCommentCount(-1);
                currComm.remove();
                //Doing a gloabl refresh fetches new comments created in the mean time
                $.ncbi.comments.refreshComments();
                $('#ncbi_share_box').hide();
            }

        });
        
        $('.hlpf_yes, .hlpf_no').live('click',function(e){
            e.preventDefault();
            var self = $(e.target);
            var currComm = self.closest('.comm_item');
            //var cmid = currComm.data('cmid');
            var commItemIds = getIdsFromCommItem(currComm);
            var likeValue = self.hasClass('hlpf_yes') ? 1 : -1;
            $.ncbi.comments.ajax({'cmd':'set_like','cmid':commItemIds.cmId,'recid':commItemIds.recId,'rsys':commItemIds.rSys,'pcid':commItemIds.pcId,'like_val':likeValue}).done(function(data){
                if (data.b != 'no'){
                     $.ncbi.comments.ajaxFailHandler({'e':data.b,'ajaxRes':data,'src':'like.blocked'});
                }
                else if(data.s == 'y'){
                    var totalCount = (parseInt(data.uc,10) + parseInt(data.dc,10));
                    var upText = '';
                    if (totalCount != 0)
                        upText = data.uc + ' of ' + totalCount + ' people found this helpful';
                    if (likeValue == 1)
                        self.parent().html('<a href="#" class="hlpfv_yes2no" title="Click to change your vote">you found this helpful</a>').closest('.comm_item').find('.comm_votes').html(upText);
                    else
                        self.parent().html('<a href="#" class="hlpfv_no2yes" title="Click to change your vote">you didn\'t find this helpful</a>').closest('.comm_item').find('.comm_votes').html(upText);
                }
                else{
                    $.ncbi.comments.ajaxFailHandler({'e':'vote not successful','ajaxRes':data,'src':'like.nosuccess'});
                }
                
            }).fail(function(e){
                $.ncbi.comments.ajaxFailHandler({'e':e,'src':'like.ajaxFail'});
            });
            
        });
        
        $('a[class*=hlpfv]').live('click',function(e){
           e.preventDefault();
           var self = $(this);
            var currComm = self.closest('.comm_item');
            //var cmid = currComm.data('cmid');
            var commItemIds = getIdsFromCommItem(currComm);
            
            $.ncbi.comments.ajax({'cmd':'set_like','cmid':commItemIds.cmId,'recid':commItemIds.recId,'rsys':commItemIds.rSys,'pcid':commItemIds.pcId,'like_val':0}).done(function(data){
                if (data.b != 'no'){
                     $.ncbi.comments.ajaxFailHandler({'e':data.b,'ajaxRes':data,'src':'like.blocked'});
                }
                else if(data.s == 'y'){
                    var totalCount = (parseInt(data.uc,10) + parseInt(data.dc,10));
                    var upText = '';
                    if (totalCount != 0)
                        upText = data.uc + ' of ' + totalCount + ' people found this helpful';
                    self.parent().html('Was this helpful? <a href="#" class="hlpf_yes">yes</a>|<a href="#" class="hlpf_no">no</a>').closest('.comm_item').find('.comm_votes').html(upText);
                }
                else{
                    $.ncbi.comments.ajaxFailHandler({'e':'vote not successful','ajaxRes':data,'src':'like.nosuccess'});
                }
                
            }).fail(function(e){
                $.ncbi.comments.ajaxFailHandler({'e':e,'src':'like.ajaxFail'});
            });
        });
        
        $('#comments_panel .comm_permalink, #comments_panel .comm_date_d').live('click',function(e){
            var currentAnchor = jCP.find('.comm_item.highlight');         
            if (currentAnchor){
                currentAnchor.removeClass('highlight');
            
            }
            var currentComm = $(this).closest('.comm_item');
            if (currentComm){
                currentComm.addClass('highlight')[0].scrollIntoView(true);;
            
            }
        });
        
        $('.replies_more').live('click',function(e){
            e.preventDefault();
            $(this).hide().next('ul').find('.comm_item').removeClass('reply_hide');  
        });
        
        
        $('#abuse_submit').live('click',function(e){
            e.preventDefault();
            var abuseDialog = $('#report_abuse');
            var cmid = abuseDialog.data('cmid');
            var recId = abuseDialog.data('recid');
            var rSys = abuseDialog.data('rsys');
            var pcId = abuseDialog.data('pcid');
            var email =  $.trim($('#abuse_email').val());
            var abuseReport = $.trim($('#abuse_text').val());
            var commNode = $('.comm_item[data-cmid=' + cmid + ']');
            var cmText = commNode.find('.comm_content').text();
            var trigger = commNode.find('.comm_report');
            if ($.ncbi.comments.abuseDlgValidate(email)){
                $.ncbi.comments.abuseDlgSubmitting();
                var cmd = 'abuse'; 
                var args = {'cmd':cmd,'cmid':cmid,'AbuseReport':abuseReport,'rtime':new Date().toUTCString(),'IssueType':$('#abuse_issue').val(),
                    'UserAgent':navigator.userAgent,'MyNcbiUserEmail':email,'CommentText':cmText};
                args = recId ? $.extend(args,{'recid':recId}) : args;
                args = rSys ? $.extend(args,{'rsys':rSys}) : args;
                args = pcId ? $.extend(args,{'pcid':pcId}) : args;
                
                var ajaxCall = $.ncbi.comments.ajax(args,'json',true,15000); 
                ajaxCall.done( function(data){
                    if (data.b != 'no'){
                        $.ncbi.comments.ajaxFailHandler({'e':data.b,'ajaxRes':data,'src':'abuse.blocked'});
                    }
                    else if (data.abuse_decision_made == 'yes'){
                        //$.ncbi.comments.abuseDlgAlreadyReported(true);
                        $.ncbi.comments.abuseDlgSuccess();
                    }
                    else if(parseInt(data.abuse_reported,10) > 0 && data.abuse_reported_by_user == 'yes'){
                        $.ncbi.comments.abuseDlgAlreadyReported();
                    }
                    else if(data.success != 'no'){
                        $.ncbi.comments.abuseDlgSuccess();
                    }
                    else{
                        $.ncbi.comments.abuseDlgFail();
                    }
                });
                ajaxCall.fail(function(e){
                    $.ncbi.comments.ajaxFailHandler({'e':e,'src':'abuse.ajaxFail'});
                });
            }
        });
        
        $('#abuse_email').live('keyup',function(e){
            $.ncbi.comments.abuseDlgValidate($.trim($('#abuse_email').val()));
        });
        
        $('#abuse_cancel').live('click',function(e){
            e.preventDefault();
            var cmid = $('#report_abuse').data('cmid');
            $('.comm_item[data-cmid=' + cmid + ']').find('.comm_report').ncbipopper('close');
        });
        
        function getIdsFromCommItem(commItem){
            var cmId = commItem.data('cmid');
            var recId = commItem.data('recid');
            var rSys = commItem.data('rsys');
            var pcIdNode = commItem.data('pcid') ? commItem : commItem.prevAll('[data-recid=' + recId + ']').filter('.comm_grp');
            var pcId = pcIdNode.data('pcid');
            return {'cmId':cmId,'recId':recId,'rSys':rSys,'pcId':pcId};
        }
        
        $('#comment_list .comm_report').live('ncbipopperopen',function(e){
            var commItem = $(this).closest('.comm_item');
            var abuseDialog = $('#report_abuse');
            var commItemIds = getIdsFromCommItem(commItem);
            
            abuseDialog.data('cmid',commItemIds.cmId);
            if(commItemIds.recId)
                abuseDialog.data('recid',commItemIds.recId);
            else
                abuseDialog.data('recid',null);
            if (commItemIds.rSys)
                abuseDialog.data('rsys',commItemIds.rSys);
            else
                abuseDialog.data('rsys',null);
            
            if (commItemIds.pcId)
                abuseDialog.data('pcid',commItemIds.pcId);
            else
                abuseDialog.data('pcid',null);
            
            $.ncbi.comments.abuseDlgRestore();
            
        });
        
        $('#comment_list .comm_report_nl').live('click',function(e){
            e.preventDefault();
            var cmid = $(this).closest('.comm_item').data('cmid');
            $.ui.jig.requiresLogin( function(name, requiredLogin ){ 
                console.log('logged in');
                var jComList = $('#comment_list');
                if (jComList.data('page')=='emb'){
                    $.ncbi.comments.fetchComments(null,null,'no'); // the issue with invitee list being empty, so only refresh the list of comments
                    jCP.live('loaded',function(e){
                        jCP.find('li.comm_item[data-cmid=' + cmid + ']').find('a.comm_report').ncbipopper('open');
                    });
                }
                else if(jComList.data('page')=='ds')
                    window.location.reload();
                console.log('logged in comment uped -  success');
            });
        });
        
        $('#not_logged_help_signin').live('click',function(e){
            e.preventDefault();
            $('#comment_who,#comment_who2').ncbipopper('close');
            $.ui.jig.requiresLogin( function(name, requiredLogin ){ 
                console.log('logged in');
                var userInfo = $.ncbi.comments.getUserInfo();
                console.log('userInfo', userInfo);
                if(userInfo.c == 'yes')
                    $.ncbi.comments.fetchComments(null,null,'yes');
                else
                    window.location = '/pubmedcommons/join/';
            });
            
        });
        
        
        $('.twitter-share-button-tmp').live('click',function(e){
            e.preventDefault();
            alert('This feature will be available after the closed pilot');
        });
        
        $('#cpi_auths').live('change',function(e){
            $('#comm_invite_name').text($(this).val());
        });
        
        $('#comm_invite_send').live('click',function(e){
            e.preventDefault();
            if ($(this).find('span').text() == 'OK')
                $('.comm_invite').ncbipopper('close');
            else
                $.ncbi.comments.processInvite();
        });
        
        $('.comm_invite').live('ncbipopperopen',function(e){
            $('#comm_invite_send').find('span').text('Send Email');
            $('#comm_invite_sys_msg').html('');
        });
        
        $(window).on('beforeunload',function(e){
            if ($.ncbi.comments.checkUnsavedComment()){
                return 'You have started commenting on this citation. ' +
                    'If you navigate away from this page without first saving, the comment will be lost.';
            }
        });
        
        //mistaken links
        $('span.num_comments, #comment_list span.comm_up,  #comment_list span.num_likes, ' + 
            'span.comm_counts span.num_comments, span.comm_counts span.num_likes').live('click',function(e){
            ncbi.sg.ping(this,e,"mistakenlink");
        });
        
        
        var charsLimit = 8000;
        var warning1CutOff = 10; //10%
        var warning1Limit = charsLimit - charsLimit / warning1CutOff;
        var saveBtn = $('#com_save');
        var limitMsgNode = $('#char_limit_msg');
        
        $('#com_new_edit').live('keyup',function(e){
            taInput = $(this);

            var currLen = taInput.val().length;
            var message = '',msgClass = '';
            if(currLen > charsLimit){
                message = 'Over the size limit - edit or link to an external document';
                msgClass = 'over_limit';
                saveBtn.ncbibutton('disable');
            }
            else if (currLen > warning1Limit){
                message = 'Reaching the size limit - ' + (charsLimit - currLen) + ' characters left';
                msgClass = 'close_limit1';
                saveBtn.ncbibutton('enable');
            }
            else{
                message = '';
                msgClass = 'in_limit';
                saveBtn.ncbibutton('enable');
            }
            limitMsgNode.removeClass().addClass(msgClass).text(message);         
        });
        
        $('.comm_cite').live('ncbipopperopen',function(){
            var textInput = $('#comm_cite_pop_inp');
            
            function getCiteText(commNode){
                var commHeader = commNode.find('.comm_header').not('.comm_ref');
                var authorName = commHeader.find('.comm_f_name').text();
                authorName = authorName.substr(authorName.lastIndexOf(' ')+1) + ' ' + authorName.substr(0,1);
                var recId = commNode.data('recid');
                recId = recId ? recId : $.ncbi.comments.getRecId();
                var title = '';
                if ($.ncbi.comments.page_type == 'emb'){
                    title = $('.abstract h1:first').text();
                }
                else{
                    var grpNode = commNode.is('.comm_grp') ? commNode : commNode.prevAll('[data-recid=' + recId + ']').filter('.comm_grp');
                    title = grpNode.find('.cit_info').find('a[class=title]').text();
                }

                var dateLink = commHeader.find('.comm_date_d');
                var dateCommented = dateLink.data('dv');
                dateCommented = new Date(dateCommented);
                
                function formatDate(dd){
                    return dd.getFullYear() + ' ' + getMonthName(dd.getMonth()) + ' ' + dd.getDate();
                    function getMonthName(num){
                        switch (num){
                            case 11: return 'Dec';
                            case 10: return 'Nov';
                            case 9: return 'Oct';
                            case 8: return 'Sept';
                            case 7: return 'Aug';
                            case 6: return 'Jul';
                            case 5: return 'Jun';
                            case 4: return 'May';
                            case 3: return 'Apr';
                            case 2: return 'Mar';
                            case 1: return 'Feb';
                            default: return 'Jan';
                        }
                    }
                }
                
                return authorName + '. Comment on PMID ' + recId + ': ' + title + ' In: PubMed Commons [Internet]. Bethesda (MD): National Library of Medicine; ' + 
                    formatDate(dateCommented) + ' [cited ' + formatDate(new Date()) + ']. Available from: ' + location.protocol + '//' + location.host + dateLink.attr('href');
            }
            
            var text = getCiteText($(this).closest('.comm_item'));
            textInput.text(text);
            window.setTimeout(function(){
                textInput.select();
            },10);
        });
        
        $doc.on('ncbipopperopen','#com_help_lk',function(e){
            var loadingContent = $('#com_help .loading');
            if (loadingContent[0]){
                var mdhelp = $.ncbi.comments.getStaticContent('mdhelpbasic');
                $('#com_help_content').replaceWith($(mdhelp).find('#com_help_content'));                
            }
        });
        $doc.on('ncbipopperopen','#com_tc_lk',function(e){
            var loadingContent = $('#com_guidlines .loading'); 
            if (loadingContent[0]){
                var mdhelp = $.ncbi.comments.getStaticContent('guidelines');
                $('#com_guidelines_content').replaceWith($(mdhelp).find('#maincontent').attr('id','com_guidelines_content').attr('class',''));    
            }
        });
        $doc.on('click','.comm_clipped_more',function(e){
            e.preventDefault();
            var self = $(e.target);
            comment = self.closest(".comm_nlines");
            comment.html(comment.data('full')).data('full','');
            window.setTimeout(function(){ncbi.sg.scanLinks(comment[0]);},5);
            self.remove();           
        });
        $doc.on('click','.comm_hide_bad',function(e){
           e.preventDefault();
           var self = $(e.target).hide();
           $('.' + self.attr('id')).show();
        });
        
        
        //end event handlers
        
      (function(){
           //check if the comments pane is to be expanded and a comment scrolled into view
           if($('#comment_list').data('page') == 'emb'){
                var hash = location.hash;
                var commAnchored = (hash.indexOf('#cm' != -1) &&  hash.substr(3).split('_').length == 2);
                var alreadyAnchored = false;
                jCP.find('a').attr('ref','discoId=ecommons');
                //$.ncbi.comments.fetchComments(); 
                if ( commAnchored || '' + jCP.data('exp') == 'true' ){
                    var cmid = hash.split('_')[1];
                    if (commAnchored)
                        ncbi.sg.ping({jsevent:'loadanchoredcomments',commentid:cmid});
                	//$('#show_comments').ncbitoggler('open')[0] || $.ncbi.comments.fetchComments();
                	$.ncbi.comments.fetchComments();
                    jCP.live('loaded',function(e){
                        if (!alreadyAnchored){
                            alreadyAnchored = true;
                            if (commAnchored){
                                try{
                                    jCP.find('.comm_item[data-cmid=' + cmid + ']').addClass('highlight')[0].scrollIntoView(true);
                                }catch(e){}
                            }
                        }
                        //show hide the anchor - See comment in PubMed Commons below
                        if ($.ncbi.comments.jCP.find('.comm_item .comm_content:not(.comm_nlines)')[0]){
                            $('#see_pmcommons').css('display','block');
                        }
                    });
                }
            }
            
      })();  
 
        
    });//End DOM ready
})(jQuery);


;
Portal.Portlet.Entrez_Facets = Portal.Portlet.extend ({
  
	init: function (path, name, notifier) 
	{ 
		this.base (path, name, notifier);
		var jFacetObj = jQuery(".facet_cont");
		if (jFacetObj[0]){
    		jFacetObj.find('.facet a').live('click',{'thisObj':this},this.filterClicked);
    		jFacetObj.find('.facet_more_apply').live('click',{'thisObj':this},this.facetMoreApplyClicked);
    		jFacetObj.find('.facet_tools a.jig-ncbipopper').bind('ncbipopperopen',{'thisObj':this},this.onMoreFilterGroups);
    		jFacetObj.find('#filter_groups_apply').bind('click',{'thisObj':this},this.filterGroupsApplyClicked);
    		jFacetObj.find('.btn_date_apply').live('click',{'thisObj':this},this.dateRangeApplyClicked);
    		jFacetObj.find('.btn_date_clear').live('click',{'thisObj':this},this.dateRangeClearClicked);
    		jFacetObj.find('.btn_range_apply').live('click',{'thisObj':this},this.rangeApplyClicked);
    		jFacetObj.find('.btn_range_clear').live('click',{'thisObj':this},this.rangeClearClicked);
    		jFacetObj.find('#facet_fields_apply').live('click',{'thisObj':this},this.facetFieldsApplyClicked);
    		
    		jFacetObj.find('.facet .more a').live('ncbipopperopen',{'thisObj':this},this.onMoreFiltersOpen);
    		jFacetObj.find('.facets_dialog').live('keypress',{'thisObj':this},this.facetDialogKeyPress);
    		jFacetObj.find('.input_date_ym').live('blur',this.autoFillDateInputs);
    		jQuery('#reset_from_message_res').live('click',{'thisObj':this},this.resetFromMessageRes);
    		
    		this.DefaultShownFacetGroups = jFacetObj.data('default_grps').split(',');
    		
    		jFacetObj.find("input[type=checkbox]").live("change",function(e){
    		   ncbi.sg.ping( this, e, "additionalFilters", { "action" : this.checked ? "checked" : "unchecked" } );
    		});
    		
    		jFacetObj.find("ul.facet li.of_sel input[type=text]").live("ncbiautocompleteoptionclick", //ncbiautocompleteenter results in multiple events
    		    {'thisObj':this},this.openFieldSelected).live("keypress",{'thisObj':this},this.openFieldKeyPress);
    		jFacetObj.find("ul.facet li.of_sel button.of_add").live("click",{'thisObj':this},this.openFieldAddClicked);
    		
    		this.jFacetObj = jFacetObj;
    	}
		
		jQuery('#reset_from_message').on('click',{'thisObj':this},this.resetFromMessage);
		
	},
	'send':{
	    'Cmd':null,
	    'SendSearchBarTerm': null,
	    'SetTimelineFilter':null,
	    'QueryKey':null,
	    'LinkName':null,
	    'IdsFromResult':null
	},
	'listen':{
	    'FacetFilterSet':function(sMessage,oData,sSrc){
		    this.handleFacetFilterSet(oData.FacetsUrlFrag,oData.BMFacets);
		},
		'FacetFiltersCleared':function(sMessage,oData,sSrc){
		    this.handleFacetFiltersCleared();
		}
	},
	'DefaultShownFacetGroups':[],
	'jFacetObj':null,
	'filterClicked':function(e){
	    e.preventDefault();
	    var oThis = jQuery(this);
	    var facetUl = oThis.closest("ul.facet");
	    var filter_id = facetUl.data('filter_id'),value_id = oThis.data('value_id');
	    var check_on = ! oThis.parent().hasClass("selected");
	    if (value_id == 'reset'  )
	        Portal.$send('FacetFilterSet',{'FacetsUrlFrag': 'fcl=all'});
	    else if (value_id == 'fetch_more'  ){
	        if (!oThis.hasClass("jig-ncbipopper"))
	            e.data.thisObj.FetchMoreOptions(filter_id,oThis);
	    }
	    else if (value_id == 'fetch_more_exp')
	        e.data.thisObj.ShowAllFacetsToggle(e);
	    else if (filter_id == 'field_search' ){
	        if (!oThis.hasClass("jig-ncbipopper"))
	            e.data.thisObj.removeFieldSelection();
	    }
	    else if (oThis.parent().hasClass('of_sel'))
	        return;
	    else if (facetUl.data('of')=='yes' && !oThis.parent().hasClass('fil_val'))
	        e.data.thisObj.removeOpenField(oThis,filter_id);
	    else if (facetUl.data('ss')=='yes')
	        e.data.thisObj.handleFilterSelection({'filterId':filter_id.toString(),'valueId':value_id.toString(),'checkOn':check_on,'replaceAll':true});
	    else if ((filter_id || value_id) && !oThis.hasClass("jig-ncbipopper") && !oThis.hasClass("facet_more_cancel") )
    	    e.data.thisObj.handleFilterSelection({'filterId':filter_id.toString(),'valueId':value_id.toString(),'checkOn':check_on,
    	        'dateSearch':facetUl.data('ds')=='yes','rangeSearch':facetUl.data('rs')=='yes'});
    	
        
	},
    'handleFilterSelection':function(opts){
	    var defOpts = {'filterId':undefined,'valueId':undefined,'checkOn':undefined,'replaceAll':undefined,'dateSearch':undefined,'rangeSearch':undefined};
	    opts = jQuery.extend(defOpts,opts);
	    
	    //when replaceAll is true, all values in that filter group are replaced, used for single select groups
	    //valueId == ''  means clear that group 
	    //var currFilterString = window.location.search.match(/filters=(.[^&]*)/);
	    var currFilterString = this.getValue('FacetsUrlFrag').match(/filters=(.[^&]*)/);
	    //var currFilterVals = currFilterString && currFilterString[1] ? currFilterString[1].split(';') : [];
	    var currFilterVals = currFilterString ? currFilterString[1].split(';') : [];
	    var possibleVals = [];
	    var facetGrpUl = this.jFacetObj.find('ul[data-filter_id = "' + opts.filterId + '"]');
	    facetGrpUl.find('li.fil_val a').each(function(){
	        var possIdVal = jQuery(this).data('value_id');
	        if (possIdVal)
	            possibleVals.push(possIdVal.toString());
	        });
	    currFilterVals = this.customFilterRead(currFilterVals,possibleVals,opts.filterId,opts.dateSearch,opts.rangeSearch);
	    
	    function removeValues(valuesArr) {
	        jQuery(valuesArr).each(function(ind,val){
	            var indexInCurr = jQuery.inArray(val,currFilterVals);
	            if (indexInCurr != -1)
	                 currFilterVals.splice(indexInCurr,1);
	        });
	    }
	    function addValues(valuesArr) {
	        jQuery(valuesArr).each(function(ind,val){
	             var indexInCurr = jQuery.inArray(val,currFilterVals);
	             if (indexInCurr == -1)
	                 currFilterVals.push(val);
	        });
	    }
	    
	    if (opts.replaceAll == true && opts.checkOn){ //single select
	        removeValues(possibleVals);
	        addValues(opts.valueId.split(';'));
	    }
	    else if (opts.valueId == ''){
	        removeValues(possibleVals);
	    }
	    else if (opts.checkOn){
	        addValues(opts.valueId.split(';'));
	    }
	    else if (!opts.checkOn){
	        removeValues(opts.valueId.split(';'));
	    }
	    var bmFacets = '';
	    if (facetGrpUl.data('bm') == 'yes' && !(opts.checkOn != true && facetGrpUl.find('li.selected').size() == 1) ){
	        bmFacets = 'bmf=' + facetGrpUl.data('filter_id') + ':' +
	            jQuery.makeArray(facetGrpUl.find('li.fil_val a').map(function(){return (jQuery(this).data('value_id'))})).join(';');
	    }
	    
	    Portal.$send('FacetFilterSet',{'FacetsUrlFrag':this.getNewUrlFrag(currFilterVals.join(';')),'BMFacets':bmFacets});
        
	},	
	'customFilterRead':function(currFilterVals,possibleVals,filterId,datesearch,rangesearch){
	    //if there is db specific filter reading override this
	    if(datesearch == true){ 
	        var rg = new RegExp(filterId + '_' + '\\d{4}\/\\d{2}\/\\d{2}_\\d{4}\/\\d{2}\/\\d{2}');
	        //for (var ind in currFilterVals){
	        for(var ind=0; ind<currFilterVals.length; ind++){
	            if (rg.exec(currFilterVals[ind]) ||
	                jQuery.inArray(currFilterVals[ind],possibleVals) != -1 ){
	                currFilterVals.splice(ind,1);
	            }
	        }
	    }
	    else if (rangesearch == true){
	        var rg = new RegExp(filterId + '_[^_]+_[^_]+');
	        for(var ind=0; ind<currFilterVals.length; ind++){
	            if (rg.exec(currFilterVals[ind]) ||
	                jQuery.inArray(currFilterVals[ind],possibleVals) != -1 ){
	                currFilterVals.splice(ind,1);
	            }
	        }
	    }
	    return currFilterVals;
	},
	'getNewUrl':function(filters,fcl,allowEmptyTerm){
	    var currUrl = window.location.pathname + window.location.search ;
        currUrl = this.replaceUrlParam(currUrl, 'filters', filters);  
        currUrl = this.replaceUrlParam(currUrl,'fcl', fcl); 
        currUrl = this.replaceUrlParam(currUrl,'querykey','');
        currUrl = this.replaceUrlParam(currUrl,'cmd','');
        currUrl = this.addTermToUrl(currUrl,allowEmptyTerm);
        //currUrl = this.appendUrlHash(currUrl);
        return currUrl;
	},
	'addTermToUrl':function(currUrl,allowEmptyTerm){
/*	    if (!currUrl.match(/term=*\/)){
	        //currUrl = this.replaceUrlParam(currUrl,'term',this.jFacetObj.data('term'));
	    } */
	    var term = jQuery.trim(jQuery("#search_term").val());
	    if (allowEmptyTerm != true)
	        term = term == '' ? 'all[sb]' : term;
	    currUrl = this.replaceUrlParam(currUrl,'term',term);
	    return currUrl;
	},
	'replaceUrlParam':function(currUrl,paramName,paramVal,allowEmpty){
	    paramVal = paramVal ? paramVal : '';
        if (paramVal != '' || allowEmpty)
            if (currUrl.indexOf(paramName + '=') == -1)
                currUrl = currUrl + (currUrl.indexOf('?') != -1 ? '&' : '?') + paramName + '=' + paramVal;
            else
                currUrl = currUrl.replace(new RegExp(paramName + '=.[^&]*'), paramName + '=' + paramVal);
         else
             if (currUrl.match(new RegExp('&' + paramName + '=.[^&]*')))
                 currUrl = currUrl.replace(new RegExp('&' + paramName + '=.[^&]*'),'');
             else if (currUrl.match(new RegExp(paramName + '=.[^&]*&')))
                 currUrl = currUrl.replace(new RegExp(paramName + '=.[^&]*&'),'');
             else
                 currUrl = currUrl.replace(new RegExp(paramName + '=.[^&]*'),'');
         return currUrl;
	},
	'getNewUrlFrag':function(filters,fcl){
	    var currUrl = this.getValue('FacetsUrlFrag');
        currUrl = this.replaceParamFrag(currUrl, 'filters', filters);
        currUrl = this.replaceUrlParam(currUrl,'fcl', fcl); 
        return currUrl;
	},
	'replaceParamFrag':function(currUrl,paramName,paramVal){//TO-DO ... poorly named, refactor
          //currUrl = currUrl.replace(new RegExp(paramName + '=[^;]*'), paramName + '=' + paramVal);
          currUrl = 'filters=' + paramVal;
          return currUrl;
	},
	'replaceUrlParamFrag':function(origFrag,paramName,paramVal,delim){ 
	    delim = delim || ';';
	    if (paramVal != '')
            if (origFrag.indexOf(paramName + '=') == -1)
                return  origFrag == '' ? paramName + '=' + paramVal : origFrag + delim + paramName + '=' + paramVal ;
            else
                return origFrag.replace(new RegExp(paramName + '=.[^' + delim + ']*'), paramName + '=' + paramVal);
         else
             if (origFrag.match(new RegExp(delim + paramName + '=.[^' + delim + ']*')))
                 return origFrag.replace(new RegExp(delim + paramName + '=.[^' + delim + ']*'),'');
             else if (origFrag.match(new RegExp(paramName + '=.[^' + delim + ']*' + delim)))
                 return origFrag.replace(new RegExp(paramName + '=.[^' + delim + ']*' + delim),'');
             else 
                 return origFrag.replace(new RegExp(paramName + '=.[^' + delim + ']*'),'');
        
	},
	'appendUrlHash':function(urlStr){
	    var hash = window.location.hash;
        if (hash != '')
            urlStr = urlStr + "#" + hash;
        return urlStr;
	},
	'FetchMoreOptions':function(filter_id,moreNode){
	    //if the moreNode param is not null, coming from a 'more' under a category, otherwise it is adding a whole group from 'choose filters'
	    var args = {"MoreFacetsGroupId":filter_id,"MoreFacetsNewGroup":(moreNode?"":"true"),"Db":this.jFacetObj.data('db'),"Term":jQuery("#term").val()};
        var site = document.forms[0]['p$st'].value;
        // ajax call
        xmlHttpCall(site, this.getPortletPath(), "GetMoreFilters", args, this.receiveMoreFilters, {"moreNode":moreNode}, this);
	},
	'receiveMoreFilters':function(responseObject, userArgs){
        try {
            // Handle timeouts
            if (responseObject.status == 408) {
                //this.showMessage("Server currently unavailable. Please check connection and try again.","error");
                 console.warn("Server currently unavailable. Please check connection and try again.");
                return;
            }
            var resp = '(' + responseObject.responseText + ')';
            var JSONobj = eval(resp);
            var allFilters = JSONobj.all_filters;
            if (userArgs.moreNode)
                this.addMoreFiltersDialog(allFilters,userArgs.moreNode);
            else
                this.addMoreFilterGroup(allFilters);
            //TO-DO: be more specific about this scan
            jQuery.ui.jig.scan();
            
        } catch (e) {
            //this.showMessage("Server error: " + e, "error");
            console.warn("Server error: " + e);
        }
	},
	'addMoreFiltersDialog':function(allFilters,targetNode){
	    targetNode.addClass("jig-ncbipopper");
	    var popper = jQuery(targetNode.attr('href'));
	    var filterId = targetNode.closest("ul.facet").data('filter_id');
	    var selFilters = this.jFacetObj.find('ul[data-filter_id = "' + filterId + '"] li a');
	    //var selFilters = this.jFacetObj.find('ul[data-filter_id = "' + filterId + '"] li.selected a');
	    allFilters = jQuery(allFilters);
	    selFilters.each(function(){
	        allFilters.find('li input[id = ' + jQuery(this).data('value_id') + ']').attr('checked','checked');
	        });   
	    popper.append(allFilters);
	    jQuery.ui.jig.scan(targetNode,['ncbipopper']);
	    targetNode.ncbipopper('open');
	},
	'getPortletPath': function(){
        return this.realname;
    },
    'facetMoreApplyClicked':function(e){
        e.preventDefault();
	    e.stopPropagation();
        var facetGroup = jQuery(e.target).closest('ul.facet');
        var groupId = facetGroup.data('filter_id');
        var selFilters = jQuery('#' + groupId + '_more').find('li input').filter('input[checked]');
        var filtersInFacet = facetGroup.find('li.fil_val a');
        var addedFacets = [], removedFacets = [], newFacets = [];
        selFilters.each(function () {
            var oThis = jQuery(this);
            var filterId = oThis.data('value_id');
            var filterName = oThis.next().text();
            addedFacets.push(filterId);
            if (filtersInFacet.filter('a[data-value_id = ' + filterId + ']').size() === 0){
                newFacets.push(filterId);
                //find the place to insert
                var insertBeforeNode;
                facetGroup.find('li.fil_val').each(function(){
                    if (jQuery(this).find('a').text() > filterName){
                        insertBeforeNode = jQuery(this);
                        return false;
                    }
                });
                if (!insertBeforeNode)
                    insertBeforeNode = facetGroup.find("li.more")
                    
                jQuery('<li class="fil_val"><a data-value_id="' + filterId + '" href="#">' + filterName + '</a></li>').insertBefore(insertBeforeNode);
            }
        });
        filtersInFacet.each(function(){
            var oThis = jQuery(this);
            var filterId = oThis.data('value_id');
            if (selFilters.filter('input[data-value_id=' + filterId + ']').size() === 0){
                removedFacets.push(filterId);
                facetGroup.find('li.fil_val').has('a[data-value_id=' + filterId + ']').remove();
            }
        });
        
        ncbi.sg.ping( e.target, e, "additionalFiltersApply", {"allChecked" : addedFacets, "newChecked" : newFacets , "newUnchecked": removedFacets} );
        
        facetGroup.find('li a[data-value_id="fetch_more"]').ncbipopper('close');
        
        function arrayToXml(arr){
            var xmlStr = '<Facets><FacetGroup '  + ' id = "' + groupId + '" >';
            for(var ind=arr.length -1; ind >=0 ; ind--)
                xmlStr = xmlStr + '<Facet>' + arr[ind] + '</Facet>';
            xmlStr = xmlStr + '</FacetGroup></Facets>';
            return xmlStr;
        }
        var args = {"UserSelectedFacetsNew":arrayToXml(addedFacets),"UserDeSelectedFacetsNew":arrayToXml(removedFacets)};
        
        
        var site = document.forms[0]['p$st'].value;
        // ajax call
        xmlHttpCall(site, e.data.thisObj.getPortletPath(), "UpdateUserAddedFacets", args, function(){}, null, this);       
    },
    'onMoreFilterGroups':function(e){
        jQuery('#filter_groups_apply').data('attachedTo',e.target.id);
        
        var loadedFgIds = [],activeFgIds = [];
        e.data.thisObj.jFacetObj.find('.facet .filter_grp a.clear').each(function(){
            var filterGrp = jQuery(this).closest('ul.facet');
            var filterId = 'fg_' + filterGrp.data('filter_id');
            loadedFgIds.push(filterId);
            if (filterGrp.find('li.selected')[0])
                activeFgIds.push(filterId);
        });
        var fgChecks = jQuery('#more_filter_groups input');
        fgChecks.each(function(){
            var oThis = jQuery(this);
            var currId = oThis.attr('id');
            oThis.attr('checked',jQuery.inArray(currId,loadedFgIds) != -1);
            oThis.attr('disabled',oThis.data('always_show') == 'yes' || jQuery.inArray(currId,activeFgIds) != -1)
        });
    },
    'filterGroupsApplyClicked':function(e){
        e.preventDefault();
	    e.stopPropagation();
        var loadedFgIds = [], fgIdsAdd = [],fgIdsRemove = [],selFgIds = [],fgUserSelIds=[];
        var defaultShownFacetGroups = e.data.thisObj.DefaultShownFacetGroups;
        e.data.thisObj.jFacetObj.find('.facet .filter_grp a.clear').each(function(){
            loadedFgIds.push('fg_' + jQuery(this).closest('ul.facet').data('filter_id'));
        });
        e.data.thisObj.jFacetObj.find('#more_filter_groups input').filter('input[checked]').each(function(){
            selFgIds.push(jQuery(this).attr('id'));
        });
        var last = selFgIds.length;
        for (var ind =0; ind <last; ind++  ){
            if(jQuery.inArray(selFgIds[ind],loadedFgIds) == -1)
                fgIdsAdd.push(selFgIds[ind].substring(3));
            if(jQuery.inArray(selFgIds[ind],defaultShownFacetGroups) == -1)
                fgUserSelIds.push(selFgIds[ind].substring(3));
        }
        last = loadedFgIds.length;
        for (var ind =0; ind <last; ind++  )
            if (jQuery.inArray(loadedFgIds[ind],selFgIds) == -1)
                fgIdsRemove.push(loadedFgIds[ind].substring(3));
        
        e.data.thisObj.updateFiltersShown(fgIdsAdd,fgIdsRemove,fgUserSelIds);
        jQuery('#' + jQuery(this).data('attachedTo')).ncbipopper('close');
    },
    'updateFiltersShown':function(fgIdsAdd,fgIdsRemove,fgUserSelIds){
        var last = fgIdsRemove.length;
        for (var ind =0; ind <last; ind++  )
            this.jFacetObj.find('ul.facet[data-filter_id = ' + fgIdsRemove[ind] + ']').remove();
        last = fgIdsAdd.length -1;
        for (var ind = last; ind >= 0; ind--  )
            this.FetchMoreOptions(fgIdsAdd[ind],null);
        //update the selection on the session variables
        this.updateUserSelectionAttrs(fgUserSelIds,fgIdsRemove);
    },
    'updateUserSelectionAttrs':function(fgUserSelIds,fgIdsRemove){
        
        function arrayToXml(arr,rootTag,tag){
            var xmlStr = '<' + rootTag + '>';
            var last = arr.length;
            for(var i=0; i<last; i++)
                xmlStr = xmlStr + '<' + tag + '>' + arr[i] + '</' + tag + '>';
            xmlStr = xmlStr + '</' + rootTag + '>';
            return xmlStr;
        }
        var rootTag = 'FacetGroups',tag='FacetGroup';
        var args = {"UserSelectedFacetGroups":arrayToXml(fgUserSelIds,rootTag,tag),"UserDeSelectedFacetGroups":arrayToXml(fgIdsRemove,rootTag,tag)};
        var site = document.forms[0]['p$st'].value;
        // ajax call
        xmlHttpCall(site, this.getPortletPath(), "UpdateUserSelectedFacetGroups", args, function(){} , {}, this);
        
    },
    'addMoreFilterGroup':function(allFilters){
	    allFilters = jQuery(allFilters);
	    
	    console.log('addMoreFilterGroup');
	    
/*	    if(!allFilters.find("ul>li")[0]){
	        alert("That wouldn't return any results");
	        return;
	    }*/
	    
	    //find the position and insert
	    var nFilterId = allFilters.data("filter_id");
	    console.log('curr filter id ', nFilterId);
	    var nFilerLi = jQuery('#more_filter_groups input').filter(function(i,j){return jQuery(j).attr("id") == "fg_" + nFilterId;}).parent();
	    console.log('curr li in more dialog',nFilerLi);
	    var selFacet = nFilerLi.nextAll("li").filter(function(i,j){return jQuery(j).find("input").is(':checked')})[0];
	    //var selFacet = nFilerLi.nextAll("li").filter(function(i,j){console.log('find next sel',jQuery(j),jQuery(j).find("input").is(':checked'),jQuery(j).find("input[checked]"),jQuery(j).find("input[checked]")[0]); return jQuery(j).find("input").is(':checked')})[0];
	    console.log('sel facet after',selFacet);
	    var facetUl;
	    if (selFacet){
	        selFacet = jQuery(selFacet);
	        var facetId = selFacet.find("input").attr("id").substring(3);
	        facetUl = jQuery("ul.facet").filter(function(i,j){return jQuery(j).data("filter_id") == facetId})
	        console.log('sel facet after ul',facetUl);
	        
	    }
	    if (facetUl && facetUl[0])
	        facetUl.before(allFilters);
	    else{
	        var resetLink = jQuery('ul.facet_reset').has('li a[data-value_id="reset"]');
	        resetLink.before(allFilters);
	    }
	    
	    var moreLink = allFilters.find("li.more");
	    if (moreLink[0]){
	        moreLink.find("a").addClass("jig-ncbipopper");
	        jQuery.ui.jig.scan(moreLink,['ncbipopper'])
	    }
	    if (allFilters.find("#facet_fileds_popup")[0])
	        jQuery.ui.jig.scan(allFilters,['ncbipopper']);
	    
	    

    },
    'rangeApplyClicked':function(e){
        e.preventDefault();
        e.stopPropagation();
        var elem = jQuery(e.target);
        var outerDiv = elem.closest('[id^=facet_range_div]');
        var valSt = outerDiv.find('[id^=facet_range_st]').val();
        var valEnd = outerDiv.find('[id^=facet_range_end]').val();
        var filterId = outerDiv.closest('ul.facet').data('filter_id');
        
        function validate(){
            var valid = true;
            try{
                var validationRE = outerDiv.data('vre') || '[^\s]+';
                var rg = new RegExp(validationRE);
                valid = valid && Boolean(rg.exec(valSt)) && Boolean(rg.exec(valEnd));
                
                //now check for value function
                var valueFun = outerDiv.data('vf');
                if (valueFun && valid){
                    valueFunEval = eval('(' + valueFun + ')');
                    if(typeof valueFunEval == 'function')
                        valid =  valueFunEval(valEnd) > valueFunEval(valSt); 
                    else{
                        var stValue = valueFun.replace('$',valSt);
                        stValue=eval('(' + stValue + ')');
                        var endValue = valueFun.replace('$',valEnd);
                        endValue = eval('(' + endValue + ')');
                        valid = endValue >= stValue;
                    }
                }
            }
            catch(e){
                alert('Check your validation regular expressions and functions in the source xml. Your user should never see this!');
                console.error(e);
                return false;
            }
            
            return valid;
        }
        
        var tryAgain = !(e.data.thisObj.validateRange(outerDiv) && validate()); 
        if (tryAgain){
	        alert('please enter a valid range');
	        return;
	    }
	    rangeValue = filterId + '_' + valSt + '_' + valEnd;
	    e.data.thisObj.handleFilterSelection({'filterId':filterId,'valueId':rangeValue,'checkOn':true,'rangeSearch':true}); 
	    outerDiv.data('attached-to').ncbipopper('close');
    },
    //this function is a callback. If you want to have extra validation of range values - override
    'validateRange':function(outerDiv){
        return true;
    },
    'dateRangeApplyClicked':function(e){
        e.preventDefault();
	    e.stopPropagation();
        var dateRange = '',dateRangeVals = [],tryAgain = false;
        
        //if (fieldSize == 4){
        var fieldSize = 4;
        //var year1 = jQuery('#facet_date_st_year');
        var outerDiv = jQuery(e.target).closest("[id^=facet_date_range_div]");
        var year1 = outerDiv.find('[id^=facet_date_st_year]');
        //var year2 = jQuery('#facet_date_end_year');
        var year2 = outerDiv.find('[id^=facet_date_end_year]');
        var year1Val = year1.ncbiplaceholder('value');
        var year2Val = year2.ncbiplaceholder('value');
        var year1Okay = year1Val.match(new RegExp('^\\d{' + fieldSize + '}$'));
        var year2Okay = year2Val.match(new RegExp('^\\d{' + fieldSize + '}$'));
        var oneYearThere = false;
        if (year1Val == '' && year2Okay){
            year1.val('0001');
            oneYearThere = true;
        }
        else if (year2Val == '' && year1Okay){
            year2.val('3000');
            oneYearThere = true;
        }
        if ( !oneYearThere  &&  !(year1Okay && year2Okay) )
            tryAgain = true;

        if (!tryAgain){
           //jQuery('#facet_date_range_div input').each(function(){
           outerDiv.find('input').each(function(){
                var oThis = jQuery(this);
                var val = oThis.ncbiplaceholder('value'); //.val();
                var fieldSize = oThis.attr('size');
                if(this.id.match('month')){
                    if (!val.match(new RegExp('^\\d{0,' + fieldSize + '}$')) )
                        tryAgain = true;
                    else if (val == '' )
                        val = this.id.match("end") ? '12' : '01' ;
                    else if (val.length == 1) 
                        val = '0' + val;
                    else if (Number(val) > 12)
                        tryAgain = true;
                }
                else if(this.id.match('day')){
                    if (!val.match(new RegExp('^\\d{0,' + fieldSize + '}$')) )
                        tryAgain = true;
                    else if (val == '' )
                        val = this.id.match("end") ? '31' : '01' ;
                    else if (val.length == 1) 
                        val = '0' + val;
                    else if (Number(val) > 31)
                        tryAgain = true;
                }
                dateRangeVals.push(val);
            });
        }
	    if (tryAgain){
	        alert('please enter a valid date range');
	        return;
	    }
	    var filterId = outerDiv.closest('ul.facet').data('filter_id');
	    dateRange = filterId + '_' + dateRangeVals[0] + '/' + dateRangeVals[1] + '/' + dateRangeVals[2] + '_' + dateRangeVals[3] + '/' + dateRangeVals[4] + '/' + dateRangeVals[5];
	    e.data.thisObj.handleFilterSelection({'filterId':filterId,'valueId':dateRange,'checkOn':true,'dateSearch':true});
	    outerDiv.data('attached-to').ncbipopper('close');
	},
	'facetFieldsApplyClicked':function(e){
	    e.preventDefault();
	    e.stopPropagation();
	    var val = jQuery('#facet_fileds_select').val();
	    //var currFilterString = window.location.search.match(/filters=(.[^&]*)/);
	    var currFilterString = e.data.thisObj.getValue('FacetsUrlFrag').match(/filters=(.[^&]*)/);
	    currFilterString = currFilterString ? currFilterString[1] : ''; 
	    if (currFilterString.match(/fld_.+/)){
	        currFilterString = currFilterString.replace(/fld_.[^;]+/,val);       
	    }
	    else
	        currFilterString = (currFilterString != '') ? currFilterString + ';' + val : val; 
	    Portal.$send('FacetFilterSet',{'FacetsUrlFrag':e.data.thisObj.getNewUrlFrag(currFilterString)});
	},
	'removeFieldSelection':function(){
	    //var currUrl = window.location.pathname + window.location.search ;
	    var currUrl = this.getValue('FacetsUrlFrag');
         if (currUrl.match(/;fld_.[^;]+/))
             currUrl = currUrl.replace(/;fld_.[^;]+/,'');
         else if (currUrl.match(/fld_.[^;]+;/))
             currUrl = currUrl.replace(/fld_.[^;]+;/,'');
         else if (currUrl.match(/fld_.[^;]+/))
             currUrl = currUrl.replace(/fld_.[^;]+/,''); 
         currUrl = this.getNewUrlFrag(currUrl);
         Portal.$send('FacetFilterSet',{'FacetsUrlFrag':currUrl});
         //window.location = currUrl;
	},
	'onMoreFiltersOpen':function(e){
	    var targetNode = jQuery(this);
	    var popper = jQuery(targetNode.attr('href'));
	    var filterId = targetNode.closest("ul.facet").data('filter_id');
	    var selFilters = e.data.thisObj.jFacetObj.find('ul[data-filter_id = "' + filterId + '"] li.fil_val a');
	    selFilters.each(function(){
	        popper.find('li input[id = ' + jQuery(this).data('value_id') + ']').attr('checked','checked');
	        }); 
	    var activeFilters = selFilters.filter(function(){return jQuery(this).parent().hasClass("selected");});
	    activeFilters.each(function(){
	        popper.find('li input[id = ' + jQuery(this).data('value_id') + ']').attr('disabled','true');
	    });
	},
	'facetDialogKeyPress':function(e){
	    e = e || utils.fixEvent (window.event);
	    if ((e.keyCode || e.which) == 13){
	        e.preventDefault();
	        e.stopPropagation();
	        jQuery(this).find('button.primary-action').trigger('click');
	    }
	},
	'autoFillDateInputs':function(e){
	    var oThis = jQuery(this);
	    var outerDiv = oThis.closest('[id^=facet_date_range_div]');
	    function updateVal(jSel,value){
	        jSel.each(function(){ var oThis = jQuery(this); if (oThis.val() == '') oThis.val(value);});
	    }
	    if (oThis.val().match(new RegExp('^\\d{' + oThis.attr('size') +'}$'))){
	        var currId = oThis.attr('id');
	        if( currId.match(/^facet_date_st_year/))
	            updateVal(outerDiv.find('[id^=facet_date_st_month], [id^=facet_date_st_day]'),'01');
	        else if (currId.match(/^facet_date_st_month/))
	            updateVal(outerDiv.find('[id^=facet_date_st_day]'),'01');    
	        else if (currId.match(/^facet_date_end_year/)){
	            updateVal(outerDiv.find('[id^=facet_date_end_month]'),'12');
	            updateVal(outerDiv.find('[id^=facet_date_end_day]'),'31');
	        }
	        else if (currId.match(/^facet_date_end_month/))
	            updateVal(outerDiv.find('[id^=facet_date_end_day]'),'31'); 
	    }
	},
	'dateRangeClearClicked':function(e){
	    e.preventDefault();
	    var self = jQuery(e.target);
	    if (self.closest('ul').has('li.daterange').find('li.selected')[0])
	        e.data.thisObj.handleFilterSelection({'filterId':self.closest('ul.facet').data('filter_id'),'valueId':'','checkOn':true,'dateSearch':true});
	    else
	        self.closest('.facets_dialog').find('input').val('');
	},
	'rangeClearClicked':function(e){
	    e.preventDefault();
	    e.data.thisObj.handleFilterSelection({'filterId':jQuery(e.target).closest('ul.facet').data('filter_id'),'valueId':'','checkOn':true,'rangeSearch':true});
	},
	'resetFromMessage':function(e){
	    e.preventDefault();
	    e.stopPropagation();
	    Portal.$send('FacetFiltersCleared',{});
	},
	'resetFromMessageRes':function(e){
	    e.preventDefault();
	    e.stopPropagation();
	    Portal.$send('FacetFilterSet',{'FacetsUrlFrag': 'fcl=all'});
	},
	'getFacetSearchData':function(){
	    var sd = {};
	    try{
	        sd = eval('({' + this.jFacetObj.data('sd') + '})');
	    }catch(e){}
	    return sd;
	},
	'handleFacetFilterSet':function(facetsUrlFrag,bMFacets){
	    var sd = this.getFacetSearchData();
	    this.setValue('FacetsUrlFrag',facetsUrlFrag);
	    this.setValue('FacetSubmitted','true');
	    this.setValue('BMFacets',bMFacets);
	    this.send.SetTimelineFilter({'TimelineYear':''});
	    if(sd.extra){
	        this.handleExtraSD(sd.extra);
	    }
	    else if (sd.op == 'search'){
	        this.send.SendSearchBarTerm();
	        this.send.Cmd({'cmd':'search'});    
	    }
	    else if (sd.op == 'link' && sd.linkname && (sd.qk || sd.idsfromresult) ){
	        this.send.LinkName({'linkname':sd.linkname});
	        this.send.QueryKey({'qk':sd.qk});
	        this.send.IdsFromResult({'IdsFromResult':sd.idsfromresult});
	        this.send.Cmd({'cmd':'Link'});    
	    }
	    else{
	        this.send.Cmd({'cmd':'HistorySearch'});
	        this.send.QueryKey({'qk':sd.qk});
	    }

	    Portal.requestSubmit();
	},
	'handleExtraSD':function(extraSD){
	    alert('Please implement the function handleExtraSD');    
	},
	'handleFacetFiltersCleared':function(){
	    this.send.Cmd({'cmd': 'removefacets'});
		Portal.requestSubmit();
	},
	'openFieldSelected':function(e){
	    //console.log('openFieldSelected');
	    e.stopPropagation();
	    e.preventDefault();
        e.data.thisObj.addOpenFieldValue(jQuery(e.target).closest('ul.facet'));
	},
	'openFieldAddClicked':function(e){
	    //console.log('openFieldAddClicked');
	    e.preventDefault();
	    e.stopPropagation();
	    e.data.thisObj.addOpenFieldValue(jQuery(e.target).closest('ul.facet'));
	},
	'openFieldKeyPress':function(e){
	e = e || utils.fixEvent (window.event);
	    if ((e.keyCode || e.which) == 13){
	        //console.log('openFieldKeyPress');
	        e.preventDefault();
	        e.stopPropagation();
	        e.data.thisObj.addOpenFieldValue(jQuery(e.target).closest('ul.facet'));
	    }
	},
	'checkSelOnlyOpenField':function(input,showAlert){
	      showAlert = showAlert || 'yes';
	      var isInDict = false;
	      var inputText = input.val().toLowerCase();
	      if(input.data('so') == 'yes'){
	          var jigOpts = input.data('jigconfig').match(/dictionary:'(\w+)'.*/);
	          var dict = jigOpts ? jigOpts[1] : null;
	          if (dict){
	              var ajaxCall = jQuery.ajax({
	                  url:'/portal/utils/autocomp.fcgi?dict=' + dict + '&q=' + inputText,
	                  async:false,
	                  dataType:'json'
	              }).always(function(data){
	                  isInDict = eval(data.responseText);
	                  //the handling function with local scope only
	                  function NSuggest_CreateData(q,matches,count){
	                      var rg = new RegExp('^' + inputText + '(@.*)?$','i');
	                      return jQuery.grep(matches,function(e,i){
	                          return rg.exec(e);
	                          }).length > 0;
	                  }
	              });
        	      if (!isInDict && showAlert == 'yes')
	                  alert('Please select one of the valid values');
	              return isInDict;
	           }
	           else
	               return true;
	       }
	       else
	           return true;
	},
	'addOpenFieldValue':function(facetUl){
	    oThis = facetUl.find("li.of_sel input[type=text]");
	    newVal = oThis.val();
	    if (newVal){
	        if(!this.checkSelOnlyOpenField(oThis)){
	            //alert('Please select one of the valid values');
	            return;
	        }
    	    //var currFilterString = window.location.search.match(/filters=(.[^&]*)/);
    	    var currFilterString = this.getValue('FacetsUrlFrag').match(/filters=(.[^&]*)/);
    	    currFilterString = currFilterString ? currFilterString[1] : ''; 
    	    //currFilterString = (currFilterString != '') ? currFilterString + ';' + oThis.attr('id') + '=' + newVal : oThis.attr('id') + '=' + newVal;
    	    var paramVal = '';
    	    var dupl = false;
    	    facetUl.find('li.selected').not(".fil_val").each(function(){
    	        //var currVal = jQuery(this).text();
    	        var currVal = jQuery(this).find('a').data('qval');
    	        if (newVal.match(new RegExp('^' + currVal + '$','i')))
    	            dupl = true;
    	        paramVal = paramVal + ( paramVal == '' ? '' : ':' ) + currVal ;
    	    });
    	    if (dupl)
    	        return;
    	    paramVal = paramVal == '' ? newVal : paramVal + ':' + newVal;
    	    currFilterString = this.replaceUrlParamFrag(currFilterString,oThis.attr('id'),paramVal,';')
    	    
    	    var bmFacets = '';
    	    if (facetUl.data('bm') == 'yes'){
    	        bmFacets = 'bmf=' + facetUl.data('filter_id') + ':' +
    	            jQuery.makeArray(facetUl.find('li.fil_val a').map(function(){return (jQuery(this).data('value_id'))})).join(';');
    	    }
    	    	    
    	    Portal.$send('FacetFilterSet',{'FacetsUrlFrag':this.getNewUrlFrag(currFilterString),'BMFacets':bmFacets});
	    }
	},
	'removeOpenField':function(elem,filterId){
	    var currFilterString = this.getValue('FacetsUrlFrag').match(/filters=(.[^&]*)/);
	    currFilterString = currFilterString ? currFilterString[1] : ''; 
	    var valueId = elem.data('value_id');

            
        var toReplace = currFilterString.match(new RegExp('of_' + filterId + '=(.[^;]*)'));
        toReplace = toReplace ? toReplace[1] : '';
        var replaceWith = '';
        if (valueId != ''){
            var toRemove = elem.data('qval');
            replaceWith = toReplace;
            var rg;
            rg = new RegExp(':' + toRemove);
            if(rg.exec(replaceWith))
                replaceWith = replaceWith.replace(rg,'');
            else{
                rg = new RegExp(toRemove + ':');
                if (rg.exec(replaceWith))
                    replaceWith = replaceWith.replace(rg,'');
                else{
                    replaceWith = replaceWith.replace(new RegExp(toRemove),'');
                }
            }
            
            
        }
        currFilterString = this.replaceUrlParamFrag(currFilterString,'of_' + filterId,replaceWith,';')
        this.setValue('FacetsUrlFrag',"filters=" + currFilterString);
        this.handleFilterSelection({'filterId':filterId,'valueId':valueId,'checkOn':true});
	},
	'ShowAllFacetsToggle':function(e){
	    var elem = jQuery(e.target);
	    if (elem.hasClass('fetch_more_exp')){
	        elem.removeClass('fetch_more_exp');
	        elem.addClass('fetch_more_exp_less');
	        if (isNaN(parseInt(elem.data("sz"),10)))
	            elem.data("sz",elem.parent().parent().find("li.fil_val:visible").size());
	        var moreFacets = elem.next('ul').find('li');
	        moreFacets.insertBefore(elem.parent());
	    }
	    else{
	        elem.removeClass('fetch_more_exp_less');
	        elem.addClass('fetch_more_exp');
	        var sz = parseInt(elem.data("sz"),10);
	        moreFacets = elem.parent().parent().find("li.fil_val").filter(function(i){return i >= sz;});
	        elem.next().append(moreFacets);
	    }
	}
}
);
;
Portal.Portlet.Pubmed_Facets = Portal.Portlet.Entrez_Facets.extend ({
  
	init: function (path, name, notifier) 
	{ 
		this.base (path, name, notifier);		
	},
	send:{
        'Cmd':null,
	    'SendSearchBarTerm': null,
	    'SetTimelineFilter':null,
	    'QueryKey':null,
	    'LinkName':null,
	    'IdsFromResult':null,
	    'SpecialPageName':null
	},
	'handleExtraSD':function(extraSD){
	    this.send.SpecialPageName({'SpecialPageName':extraSD.split('=')[1]});       
	}
}
);


;
Portal.Portlet.Entrez_DisplayBar = Portal.Portlet.extend({

	init: function(path, name, notifier) {
		console.info("Created DisplayBar");
		this.base(path, name, notifier);
		
		// for back button compatibility reset values when page loads
		if (this.getInput("Presentation")){
		    this.setValue("Presentation", this.getValue("LastPresentation"));
		    Portal.Portlet.Entrez_DisplayBar.Presentation = this.getValue("LastPresentation");
		}
		if (this.getInput("Format")){
		    this.setValue("Format", this.getValue("LastFormat"));
		    Portal.Portlet.Entrez_DisplayBar.Format = this.getValue("LastFormat");
		}
		if (this.getInput("PageSize")){
		    this.setValue("PageSize", this.getValue("LastPageSize"));
		    Portal.Portlet.Entrez_DisplayBar.PageSize = this.getValue("LastPageSize");
		}
		if (this.getInput("Sort")){
		    this.setValue("Sort", this.getValue("LastSort"));
		    Portal.Portlet.Entrez_DisplayBar.Sort = this.getValue("LastSort");
		}
		this.ResetDisplaySelections();
		this.ResetSendToSelection();
		
    	jQuery( 
            function(){
        
                var animationTime = jQuery("#sendto2").ncbipopper("option","openAnimationTime");
                var currentCnt = 0;
                var expTimer;
        
                function testPosition(){
                    jQuery(window).trigger("ncbipopperdocumentresize");
                    currentCnt+=10;
                    if (currentCnt<animationTime) {
                        expTimer = window.setTimeout(testPosition,10);
                    }
                }
        
                jQuery("#send_to_menu2 input").on("change click", 
                    function(){
                        currentCnt = 0;
                        if(expTimer) window.clearTimeout(expTimer);
                        testPosition();
                    } 
                );
        
            }
        );
		        
	},
	
	
	send: {
		'Cmd': null, 
		'PageSizeChanged': null,
		'ResetSendTo': null,
		'ResetCurrPage': null
	},
	
	
	
	listen: {
		
		/* browser events */
			
		"sPresentation<click>": function(e, target, name){
		    this.PresentationClick(e, target, name); 
		},
		
		"sPresentation2<click>": function(e, target, name){
		    this.PresentationClick(e, target, name); 
		},
		
		"sPageSize<click>": function(e, target, name){	
		    this.PageSizeClick(e, target, name);
		},
		
		"sPageSize2<click>": function(e, target, name){	
		    this.PageSizeClick(e, target, name);
		},
		
		"sSort<click>": function(e, target, name){
		    this.SortClick(e, target, name);
		},
		
		"sSort2<click>": function(e, target, name){
		    this.SortClick(e, target, name);
		},
		
		"SetDisplay<click>": function(e, target, name){
			this.DisplayChange(e, target, name); 
		},
		
		"SendTo<click>": function(e, target, name){
			var sendto = target.value;
            var idx = target.getAttribute('sid') > 10? "2" : "";
			this.SendToClick(sendto, idx, e, target, name); 
		},
		
		"SendToSubmit<click>": function(e, target, name){
		    e.preventDefault();
		    var cmd = target.getAttribute('cmd').toLowerCase();
		    var idx = target.getAttribute('sid') > 10? "2" : "";
			this.SendToSubmitted(cmd, idx, e, target, name); 
		},
		
		/* messages from message bus*/
		
		'ResetSendTo' : function(sMessage, oData, sSrc) {
		    this.ResetSendToSelection();
		}
	
	}, // end listen
	
	
	
	/* functions */
	
	'PresentationClick': function(e, target, name){
		Portal.Portlet.Entrez_DisplayBar.Presentation = target.value;
		Portal.Portlet.Entrez_DisplayBar.Format = target.getAttribute('format');
	},
	
	'PageSizeClick': function(e, target, name){ 
		Portal.Portlet.Entrez_DisplayBar.PageSize = target.value;
	},
	
	'SortClick': function(e, target, name){
		Portal.Portlet.Entrez_DisplayBar.Sort = target.value;
	},
	
	'DisplayChange': function(e, target, name){
	    var submit = false;
	    var extractdb = window.location.pathname.match(/\/([A-Za-z]+)\/?/); 
	    var db = (extractdb[1] && extractdb[1] != '') ? extractdb[1] : "";
	    
	    if (db != '' && getEntrezSelectedItemCount() == 1){
	        //get id, attach db and report, and link	        
	        var URL = '/' + db + '/' + getEntrezSelectedItemList() + '?report=' + Portal.Portlet.Entrez_DisplayBar.Presentation
	        + (Portal.Portlet.Entrez_DisplayBar.Format.toLowerCase() == 'text' ? '&format=text' : '');
	        window.location = URL;
	    }
	    else if (db != '' && getEntrezResultCount() == 1 && window.location.href != ""){   
	        //remove report= from URL and insert new report= into URL
	        if ((window.location.pathname != '' && window.location.pathname.match(/\/[A-Za-z]+\/\w*\d+\w*/))
	            || window.location.href.match(/\/[A-Za-z]+\/??.*term=[^&\s]+/)
	        ){
	            var URL = window.location.href.replace(/&?report=\w+/, "").replace(/\?&/, "?");
	            var hashtagindex = URL.indexOf("#");
	            if (hashtagindex >= 0){
	                URL = URL.substring(0, hashtagindex);
	            }
	            URL += (URL.match(/\?/) ? (URL.match(/\?[^\s]+/) ? "&" : "") : "?") 
	                + "report=" + Portal.Portlet.Entrez_DisplayBar.Presentation
	                + (Portal.Portlet.Entrez_DisplayBar.Format.toLowerCase() == 'text' ? '&format=text' : '');
	            window.location = URL;    
	        }
	        else {
	            submit = true;
	        }
	    }
	    else{
            submit = true;
        }
        
        if (submit){
            this.send.Cmd({'cmd': 'displaychanged'});
            
    	    this.SetPresentationChange(e, target, name);
    	    this.SetPageSizeChange(e, target, name);
    	    this.SetSortChange(e, target, name);
    	    
    	    Portal.requestSubmit();
	    }
	},
	
	'SetPresentationChange': function(e, target, name){
        this.setValue("Presentation", Portal.Portlet.Entrez_DisplayBar.Presentation);
	    this.setValue("Format", Portal.Portlet.Entrez_DisplayBar.Format);
	},
	
	'SetPageSizeChange': function(e, target, name){
	    this.setValue("PageSize", Portal.Portlet.Entrez_DisplayBar.PageSize);
		if (this.getValue("PageSize") != this.getValue("LastPageSize")){
    		//send PageSizeChanged
    		this.send.PageSizeChanged({
    			'size': this.getValue("PageSize"),
                'oldsize': this.getValue("LastPageSize")
    		});	
		}
	},
		
	'SetSortChange': function(e, target, name){
	    if (this.getInput("Sort")){
	        this.setValue("Sort", Portal.Portlet.Entrez_DisplayBar.Sort);
            if (this.getValue("Sort") != this.getValue("LastSort")){
                // ask to reset CurrPage 
    		    this.send.ResetCurrPage();
    		}
        }    	
	},
		
	'SendToClick': function(sendto, idx, e, target, name) {
		if(sendto.toLowerCase() == 'file'){
			this.SendToFile(sendto, idx);
		}
		else if(sendto.toLowerCase() == 'addtocollections'){
			this.SendToCollections(sendto, idx);
		}
		else if(sendto.toLowerCase() == 'addtoclipboard'){
		    this.SendToClipboard(sendto, idx);
		}
	},
	
	'SendToSubmitted': function(cmd, idx, e, target, name){
	    if (cmd == 'file'){
	         this.SendToFileSubmitted(cmd, idx, target);
	    }
	    else if (cmd == 'addtocollections'){
	    	this.SendToCollectionsSubmitted(cmd, idx, target);
	    }
	    this.send.Cmd({'cmd': cmd});
	    Portal.requestSubmit();
	},
	
	'ResetSendToSelection': function(){
	    var SendToInputs = this.getInputs("SendTo");
	    for (var j = 0; j < SendToInputs.length; j++){
		    if (SendToInputs[j].checked){
		        SendToInputs[j].checked = false;
			}
		}
	},
	
	'SendToFile': function(name, idx){
	    // generate content
	    var count = this.getItemCount();
		var content = 'Download ' + count + ' items.';
		this.addSendToHintContent(name, idx, content);
	},
	
	'SendToCollections': function(name, idx){
	    // generate content
        var count = this.getItemCount();
        var content= 'Add ';
        var optionNode = document.getElementById("coll_start_option" + idx);
        if (count > Portal.Portlet.Entrez_DisplayBar.CollectionsUpperLimit){
            content += Portal.Portlet.Entrez_DisplayBar.CollectionsUpperLimitText;
            if (optionNode){
            	optionNode.className = '';
            }
        }
        else{
            content += count;
            if (optionNode){
            	optionNode.className = 'hidden';
            }
        }
        content += " items.";
        this.addSendToHintContent(name, idx, content);	
	},
	
	'SendToClipboard': function(name, idx){
	    // generate content
	    var count = this.getItemCount();
        var content= 'Add ';
        if (count > Portal.Portlet.Entrez_DisplayBar.ClipboardLimit){
            content += "the first " + Portal.Portlet.Entrez_DisplayBar.ClipboardLimit;
        }
        else{
            content += count;
        }
        content += " items.";
        this.addSendToHintContent(name, idx, content);
	},
	
	'getItemCount': function(){
	    // ask for selected items count from DbConnector
	    var selectedItemCount = getEntrezSelectedItemCount();
	    if (selectedItemCount > 0){
	        return selectedItemCount;
	    }
	    else{
	        // ask for result count from Entrez_ResultsController
	        return getEntrezResultCount();
	    }
	},
	
	'addSendToHintContent': function(name, idx, content){
	    var hintNode = document.getElementById("submenu_" + name + "_hint" + idx);
	    if (hintNode){
	        hintNode.innerHTML = content;
	        hintNode.className = 'hint';
	    }
	},
	
	'AddSendToSubmitEvent': function(){
	    // add event for SendTo submit button click. 
	    // This call is needed if the position of the submit button node has changed in relation to its parent node. 
        this.addEvent("SendToSubmit", "click", function(e, target, name) {
            var cmd = target.getAttribute('cmd');
            this.SendToSubmitted(cmd, e, target, name); 
        }, false);
    },
    
    'SendToFileSubmitted': function(cmd, idx, target){
         if (this.getInput("FFormat" + idx)){
             this.setValue("FileFormat", this.getValue("FFormat" + idx));
         }
         if (this.getInput("FSort" + idx)){
             this.setValue("FileSort", this.getValue("FSort" + idx));
         }
    },
    
    'SendToCollectionsSubmitted': function(cmd, idx, target){
         if (document.getElementById("coll_start" + idx)){
             document.getElementById("coll_startindex").value = document.getElementById("coll_start" + idx).value;
         }
    },
    
    'ResetDisplaySelections': function(){
        if (this.getInput("Presentation")){
            var selection = this.getValue("Presentation").toLowerCase() + this.getValue("Format").toLowerCase();
            if (document.getElementById(selection)){
                document.getElementById(selection).checked = true;
            }
            // bottom display bar
            if (document.getElementById(selection + "2")){
                document.getElementById(selection + "2").checked = true;
            }
            
        }
        if (this.getInput("PageSize")){
            var selection = 'ps' + this.getValue("PageSize");
            if (document.getElementById(selection)){
                document.getElementById(selection).checked = true;
            }
            // bottom display bar
            if (document.getElementById(selection + "2")){
                document.getElementById(selection + "2").checked = true;
            }
        }
        if (this.getInput("Sort")){
            var selection = this.getValue("Sort") || 'none'; 
            if (document.getElementById(selection)){
                document.getElementById(selection).checked = true;
            }
            // bottom display bar
            if (document.getElementById(selection + "2")){
                document.getElementById(selection + "2").checked = true;
            }
        }
    }
	
},
{
    Presentation: '',
    Format: '',
    PageSize: '',
    Sort: '',
    CollectionsUpperLimit: 1000,
	CollectionsUpperLimitText: '1,000',
	ClipboardLimit: 500
});


;
Portal.Portlet.Pubmed_DisplayBar = Portal.Portlet.Entrez_DisplayBar.extend({

	init: function(path, name, notifier) {
		this.base(path, name, notifier);
		this.AddListeners(notifier);
		if (jQuery("#file_sort").find("option[value^=AC]")[0]){
		    var caNode = {};
    		jQuery("#sendto, #sendto2").bind("ncbipopperopen",function(){
    		    var selectedItemCount = getEntrezSelectedItemCount() || 0;
    		    //if (jQuery("input[type=checkbox]").filter('[name$=uid]:checked')[0]){
    		    if (selectedItemCount > 0){
    		        jQuery('#file_sort, #email_sort, #file_sort2, #email_sort2').each(function(ind,item){
    		            var oThis = jQuery(this);
    		            //oThis.find('option[value^=AC]').addClass('hide'); //this approach fails in IE
    		            if (oThis.find('option[value^=AC]')[0])
    		                caNode[item.id] = oThis.find('option[value^=AC]').remove();
    		        });
    		    }
    		    else{
    		        jQuery('#file_sort, #email_sort, #file_sort2, #email_sort2').each(function(ind,item){
        		        var oThis = jQuery(this);
        		        //oThis.find('option[value^=AC]').removeClass('hide');
        		        if (!(oThis.find('option[value^=AC]')[0]))
        		            oThis.prepend(caNode[item.id]);
        		        oThis[0].selectedIndex = 0;
    		        });
    		    }
    		});
    	}
	},
	
	send: {
		'Cmd': null, 
		'PageSizeChanged': null,
		'ResetSendTo': null,
		'ResetCurrPage': null,
		'SendMail': null
	},
	
	/* functions */
	"AddListeners": function(notifier){
	    var oThis = this;
	    
    	notifier.setListener(this, 'SubmitSort', 
        	function(oListener, custom_data, sMessage, oNotifierObj) {
        	    if (oThis.getInput("Sort")){
                    oThis.setValue("Sort", custom_data.sort);
                    oThis.send.Cmd({'cmd': 'displaychanged'});
                    Portal.requestSubmit();
                }
            }
    	, null);
	},
	
	'SendToClick': function(sendto, idx,  e, target, name) {
	    if (sendto.toLowerCase() == 'order'){
	    }
	    else if (sendto.toLowerCase() == 'mail'){
	        this.SendToMail(sendto, idx);
	    }
	    else if (sendto.toLowerCase() == 'addtobibliography'){
	        this.SendToBib(sendto, idx);
	    }
	    else if (sendto.toLowerCase() == 'citationmanager'){
	        this.SendToCitManager(sendto, idx);
	    }
	    else
	        this.base(sendto, idx, e, target, name);
	},
	
	'SendToMail': function(sendto, idx){
	    // hide any previous alert messages 
	    var alertnode = document.getElementById("email_alert" + idx);
	    alertnode.className = 'hidden';
	    
	    // ask for selected items count from DbConnector
	    var selectedItemCount = getEntrezSelectedItemCount() || 0;
	    
        // if ids are selected, save old description & subject, and create new description & subject
        var descNode = document.getElementById("email_desc" + idx);
	    var subjNode = document.getElementById("email_subj" + idx);
        if (selectedItemCount > 0){
            if (Portal.Portlet.Pubmed_DisplayBar.Description == '')
	            Portal.Portlet.Pubmed_DisplayBar.Description = descNode.innerHTML;
	        descNode.innerHTML = selectedItemCount + " selected item" + (selectedItemCount > 1? "s" : "");
	        if (Portal.Portlet.Pubmed_DisplayBar.Subject == '')
	            Portal.Portlet.Pubmed_DisplayBar.Subject = subjNode.value;
	        subjNode.value = selectedItemCount + " selected item" + (selectedItemCount > 1? "s" : "") + " - PubMed";
	    }
	    // if ids are not selected, and an old description or subject are present, restore those
	    else{
	        if (Portal.Portlet.Pubmed_DisplayBar.Description != '')
	            descNode.innerHTML = Portal.Portlet.Pubmed_DisplayBar.Description;
            if (Portal.Portlet.Pubmed_DisplayBar.Subject != '')
	            subjNode.value = Portal.Portlet.Pubmed_DisplayBar.Subject;
        }
        
        // get total number of items about to be sent
        var count = this.getItemCount();
        
        // don't show email count and start options if less than 5 items are in search result, or user has selected some items
        if (document.getElementById("email_count_option" + idx)){
            if (count <= 5 || selectedItemCount > 1){
                document.getElementById("email_count_option" +  idx).style.display = "none";
            }
            else {
                document.getElementById("email_count_option" + idx).style.display = "list-item";
            }
        }
        if (document.getElementById("email_start_option" + idx)){
            if (count <= 5 || selectedItemCount > 1){
                document.getElementById("email_start_option" +  idx).style.display = "none";
            }
            else {
                document.getElementById("email_start_option" + idx).style.display = "list-item";
            }
        }
        
         // don't show sort option if 1 item is selected
        if (document.getElementById("email_sort_option" + idx)){
            if (count == 1){
                document.getElementById("email_sort_option" + idx).style.display = "none";
            }
            else {
                document.getElementById("email_sort_option" + idx).style.display = "list-item";
            }
        }
        
        // captcha is not shown if user is signed in to myncbi
        if (document.getElementById("captcha-segment" + idx)){
            this.GetCaptcha(idx);
        }
	},
	
	'SendToBib': function(name, idx){
	    // generate content
        var count = this.getItemCount();
        var content= 'Add ';
        if (count > Portal.Portlet.Pubmed_DisplayBar.BibUpperLimit){
            content += "the first " + Portal.Portlet.Pubmed_DisplayBar.BibUpperLimit;
        }
        else{
            content += count;
        }
        content += " items.";
        this.addSendToHintContent(name, idx, content);	
	},
	
	'SendToCitManager': function(name, idx){
	    var count = this.getItemCount();
	    var selectedItemCount = getEntrezSelectedItemCount() || 0;
	    
	    // don't show number to send and start options if less than 5 items are in search result, or user has selected some items
        if (document.getElementById("citman_count_option" + idx)){
            if (count <= 5 || selectedItemCount > 1){
                document.getElementById("citman_count_option" +  idx).style.display = "none";
            }
            else {
                document.getElementById("citman_count_option" + idx).style.display = "list-item";
            }
        }
        if (document.getElementById("citman_start_option" + idx)){
            if (count <= 5 || selectedItemCount > 1){
                document.getElementById("citman_start_option" +  idx).style.display = "none";
            }
            else {
                document.getElementById("citman_start_option" + idx).style.display = "list-item";
            }
        }
	    
	    if (count <= 5 || selectedItemCount > 1) {
		    var s = '';
		    if (count > 1){ s = 's';} 
		    var content = 'Download ' + count + ' citation' + s + '.';
		    this.addSendToHintContent(name, idx, content);
		}
	},
	
	'SendToSubmitted': function(cmd, idx, e, target, name){
	    if (cmd == 'mail'){
	         this.SendToEmailSubmitted(cmd, idx, target);
	    }
	    else if (cmd == 'citationmanager'){
	         this.SendToCitManagerSubmitted(cmd, idx, target);
	    }
	    else{
	        this.base(cmd, idx, e, target, name);
	    }
	},
	
	'SendToEmailSubmitted': function(cmd, idx, target){
	    var alertnode = document.getElementById("email_alert" + idx);
	    alertnode.className = 'hidden';
	    
	    var email = document.getElementById("email_address" + idx).value.replace(/^\s*|\s*$/g,'');
	    if (email == ''){
	        alertnode.innerHTML = 'Please provide an email address.';
	        alertnode.className = 'email_alert';
	    }
	    else {
    	    var emailRegexp = /^[A-Za-z0-9._\'%-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,4}$/;
    		if (emailRegexp.test(email)){
    		    // captcha is not shown if user is signed in to myncbi
                if (document.getElementById("captcha-segment" + idx)){
    		        this.ValidateCaptcha(cmd, idx, target, email);    
    		    }
    		    else{
    		        this.SendMailInfo(cmd, idx, target, email);    
    		    }
    		}
    		else {
    			alertnode.innerHTML = 'The email address is invalid!';
	            alertnode.className = 'email_alert';
    		}
        }    		
	    
	},
	
	'SendMailInfo': function(cmd, idx, target, email, captchakey, captchaanswer){
	    // collect options, description and extra text
	    var emailFormat = document.getElementById("email_format" + idx);
	    var report = emailFormat.value;
	    var format =  emailFormat.options[emailFormat.selectedIndex].getAttribute('format');
	    var sort = document.getElementById("email_sort" + idx)? document.getElementById("email_sort").value : "";
	    var count = document.getElementById("email_count" + idx)? document.getElementById("email_count" + idx).value : "5";
	    var start = document.getElementById("email_start" + idx)? document.getElementById("email_start" + idx).value : "1";
	    var text = document.getElementById("email_add_text" + idx).value;
	    var subj = document.getElementById("email_subj" + idx).value;
	    var querykey = target.getAttribute('qk');
	    var querydesc = document.getElementById("email_desc" + idx).innerHTML;
	    var suppData = jQuery('#chkSupplementalData' + idx).attr('checked');
	    if (suppData && report == 'abstract' && format != 'text' ) report = 'AbstractWithSupp';
	    
	    // send message to email portlet with data
	    this.send.SendMail({
	        'report' : report,
	        'format' : format,
	        'count' : count,
	        'start' : start,
	        'sort' : sort,
	        'email' : email,
	        'text' : text,
	        'subject' : subj,
	        'querykey': querykey,
	        'querydesc': querydesc,
	        'captchakey' : (captchakey ? captchakey : ""), 
	        'captchaanswer' : (captchaanswer ? captchaanswer : "")
	        /*'suppData' : suppData */        
	    });
	    
	    this.send.Cmd({'cmd': cmd});
	    Portal.requestSubmit();
    },
    
    'GetCaptcha':function(idx){
        //call backend
        this.CallCaptcha('create', '', '', idx, {"idx" : idx, "type" : "create"});
        // update page to display the new image
    },
    
    'ValidateCaptcha': function(cmd, idx, target, email){
        //call backend
        var key = jQuery('#captcha-segment' + idx).find('img').attr('id');
        var answer = jQuery('#captcha-text' + idx).val();
        if (answer){
            target.disabled = true;
            this.CallCaptcha(
                'verify', key, answer, idx, 
                {"cmd" : cmd, "idx" : idx, "target" : target, "email" : email, "key" : key, "answer" : answer, "type" : "verify" }
            );
        }
        else {
            //display an error telling user captcha is required
            jQuery("#email_alert" + idx).html("Please type the characters you see in the picture below, in the box provided under the picture.").attr('class', 'email_alert');
        }
        
    },
    
    'CallCaptcha': function(type, key, answer, idx, userArgs){
        var oThis = this;
        var args = {
            "Type": type,
            "Key": key,
            "Answer": answer,
            "Idx": idx,
            "p$rq": oThis.realname + ":XmlHttpCaptcha" 
        };
        
        jQuery.ajax({
            url: "/pubmed/",
            type: "POST",
            timeout: 1000,
            dataType: "text",
            data: args,
            async: false
        }).done(function(response, status){
            if(status == 408){
                //display an error indicating a server timeout
                jQuery("#email_alert" + idx).html("We are experiencing some problems with our server. Please try again later.").attr('class', 'email_alert');
                if (userArgs.target){
                    userArgs.target.disabled = false;
                }
            }
            
            var JSONobject = eval('(' + response + ')');
            
            if (type == 'create'){
                if (document.getElementById("captcha-segment" + idx)){
                    jQuery("#captcha-segment" + idx).replaceWith(JSONobject.CaptchaSegment);  
                    jQuery("#CaptchaReload" + idx).bind("click",function(e){
                        e.preventDefault();
                        e.stopPropagation(); 
                        jQuery("#email_alert" + idx).html("").attr('class', 'hidden');
                        oThis.GetCaptcha(idx);
                    });
                }
            }else if(type == 'verify'){
                // if successful, send email
                if (JSONobject.VerificationStatus == 'pass'){
                    //console.info("cmd: " + userArgs.cmd + " idx: " + userArgs.idx + " target: " + userArgs.target + " email: " + userArgs.email + " key: " +  userArgs.key + " answer: " + userArgs.answer);	
                    oThis.SendMailInfo(userArgs.cmd, idx, userArgs.target, userArgs.email, key, answer);	
                }
                // otherwise show another captcha
                else {
                    // display error message
                    jQuery("#email_alert" + idx).html("The characters you entered did not match the image. Please try again.").attr('class', 'email_alert');
                     if (document.getElementById("captcha-segment" + idx)){
                        jQuery("#captcha-segment" + idx).replaceWith(JSONobject.CaptchaSegment);
                        jQuery("#CaptchaReload" + idx).bind("click",function(e){
                            e.preventDefault();
                            e.stopPropagation(); 
                            jQuery("#email_alert" + idx).html("").attr('class', 'hidden');
                            oThis.GetCaptcha(idx);
                        });
                    }
                    if (userArgs.target){
                        userArgs.target.disabled = false;
                    }
                }
            } 
            
            
        }).fail(function(){
            jQuery("#email_alert" + idx).html("We are experiencing some problems with our server. Please try again later.").attr('class', 'email_alert');
            if (userArgs.target){
                userArgs.target.disabled = false;
            }
        });
    },
    
    'SendToCitManagerSubmitted': function(cmd, idx, target){
         this.setValue("FileFormat", 'nbib');
         this.setValue("FileSort", this.getValue("LastSort"));
         if (document.getElementById("citman_count_option" + idx) && document.getElementById("citman_count_option" + idx).style.display == "list-item"){
             document.getElementById("citman_customrange").value = "true";
             this.setValue("PageSize", document.getElementById("citman_count" + idx).value);
	         document.getElementById("citman_startindex").value = document.getElementById("citman_start" + idx).value;
	     }
         this.send.Cmd({'cmd': 'file'});
         Portal.requestSubmit();
    },
    'PresentationClick': function(e, target, name){
        this.base(e,target,name);
        this.instantApply();
	},	
	'PageSizeClick': function(e, target, name){ 
		this.base(e,target,name);
		this.instantApply();
	},
	'SortClick': function(e, target, name){
		this.base(e,target,name);
		this.instantApply();
	},
	'instantApply':function(){
	    if (jQuery('.results_settings').hasClass('new6'))
	        this.DisplayChange();
	}
	
},
{
    BibUpperLimit: 500,
    Description: '',
    Subject: ''
});

jQuery(document).ready(function(){
    jQuery('#email_format').bind('change',emailFormatChanged);
    jQuery('#email_format2').bind('change',emailFormatChanged);
    //emailFormatChanged(); //initial state
    jQuery('#email_format').trigger('change');
    jQuery('#email_format2').trigger('change');
});

function emailFormatChanged(){ 
   var sel = jQuery(this); 
   var chkSpan = sel.next(); 
   var visibility =  ( sel.val() == 'abstract' && 
                    sel.find("option:selected").attr('format') != 'text' ) ? "visible" : "hidden";
   chkSpan.css('visibility', visibility); 
}






;
Portal.Portlet.Entrez_Messages = Portal.Portlet.extend({

	init: function(path, name, notifier) {
		this.base(path, name, notifier);
		
		this.setMsgAreaClassName();
	},
	
	listen: {
	   /* messages from message bus*/
		
		'AddUserMessage' : function(sMessage, oData, sSrc) {
		    // create new message node
		    var msgnode = document.createElement('li');
		    if (oData.type != ''){
		        msgnode.className = oData.type + ' icon'; 
		    }
		    if (oData.name != ''){
		        msgnode.id = oData.name; 
		    }
		    msgnode.innerHTML = "<span class='icon'>" + oData.msg + "</span>";
		    
		    // add new node as first message in message block (not ads that look like messages)
		    var parent = document.getElementById('msgportlet');
		    if (parent){
    		    var oldnode = document.getElementById(oData.name);
    		    if (oldnode){
    		        parent.removeChild(oldnode);
    		    }
    		    var firstchild = parent.firstChild;
    	        if (firstchild){
                    parent.insertBefore(msgnode, firstchild);
                }
                else{
                    parent.appendChild(msgnode);
                }
                this.setMsgAreaClassName('true');
            }
            //if there was no ul, create one, then insert the li
            else {
                var msgarea = document.getElementById('messagearea');
                if (msgarea){
                    var msgportlet = document.createElement('ul');
                    msgportlet.className = 'messages';
                    msgportlet.id = 'msgportlet';
                    msgportlet.appendChild(msgnode);
                    if (msgarea.firstChild){
                         msgarea.insertBefore(msgportlet, msgarea.firstChild);
                    }
                    else{
                        msgarea.appendChild(msgportlet);
                    }
                    this.setMsgAreaClassName('true');
                }
            }
		},
		
		'RemoveUserMessage' : function(sMessage, oData, sSrc) {
		    var msgnode = document.getElementById(oData.name);
		    if (msgnode){
		        var parent = document.getElementById('msgportlet'); 
		        if (parent){
    		        parent.removeChild(msgnode);
    		        this.setMsgAreaClassName();
    		        // if the parent ul has no children then remove the parent
    		        if (parent.firstChild){}
    		        else {
    		            if (document.getElementById('messagearea')) {
    		                document.getElementById('messagearea').removeChild(parent);
    		            }
    		        }
    		    }
		    }
		}
	}, // end listen
	
	'setMsgAreaClassName' : function(hasMsg){
        var msgarea = document.getElementById('messagearea');
	    if (msgarea){
	        var msgclass = "empty";
	        
    	    // if a message was added, hasMsg is set to true at call time to avoid checks. 
    	    // by default, hasMsg is false.
    	    if (hasMsg == 'true'){
    	        msgclass = "messagearea";
    	    }
    	    else if (msgarea.getElementsByTagName('li').length > 0){
                msgclass = "messagearea"; 
        	}
        	
            msgarea.className = msgclass;
        }
	} // end setMsgAreaClassName
});
		
		
;
Portal.Portlet.Entrez_LimitsTab = Portal.Portlet.extend ({
  
	init: function (path, name, notifier) 
	{ 
		this.base (path, name, notifier);
		
		var limApplyBtn = jQuery("button[name$=ApplyLimits]");
		if(limApplyBtn[0]){
    		jQuery(document.forms[0]).off("submit").on("submit",function(e){
    		    e.preventDefault();
    		    limApplyBtn.trigger("click")
    		});
		}
	},

    /* If you have to OVERRIDE the send and listen sections, please make sure to include the code in 
    those sections here before your code. Same for init if you need to override it. All other functions
    will be carried over by Portal.Portlet.My_LimitsTab = Portal.Portlet.Entrez_LimitsTab.extend .
    */
    
	send: {
		"Cmd": null,
		"SendSearchBarTerm": null
	},
	
	listen: {
	
	    /* actions from the Limits activated message area */
		
		"ChangeLimits<click>": function(e, target, name) {
		    e.preventDefault();
	        e.stopPropagation();
		    this.ProcessChange (e, target, name);
		},
		
		"RemoveLimits<click>": function(e, target, name) {
		    e.preventDefault();
	        e.stopPropagation();
		    this.ProcessRemove (e, target, name);
		},
		
		"Term": function(sMessage, oData, sSrc) {
		    this.ProcessTerm (sMessage, oData, sSrc);
		},
		
		/* actions from the LimitsPage */
		
		"ClearAllLimits<click>": function(e, target, name){
		    e.preventDefault();
	        e.stopPropagation();
		    this.ProcessClearAll(e, target, name);
		},
		
		"ApplyLimits<click>": function(e, target, name){
		    e.preventDefault();
	        e.stopPropagation();
		    this.ProcessApply(e, target, name);
		},
		
		"CancelLimits<click>": function(e, target, name){
		    e.preventDefault();
	        e.stopPropagation();
		    this.ProcessCancel(e, target, name);
		}
		
	}, //end listen
	
	/* actions from the Limits activated message area */
	
	"ProcessChange" : function(e, target, name){
		Portal.Portlet.Entrez_LimitsTab.WaitingForSearchTerm = true;	
		Portal.Portlet.Entrez_LimitsTab.Link = target.href;
		this.send.SendSearchBarTerm();
	},
	
	"ProcessRemove" : function(e, target, name){
		this.send.Cmd({'cmd': 'removelimits'});
		Portal.requestSubmit();
	},
	
	"ProcessTerm": function(sMessage, oData, sSrc) {
        if (Portal.Portlet.Entrez_LimitsTab.WaitingForSearchTerm) { // make sure ProcessChange was clicked
            Portal.Portlet.Entrez_LimitsTab.WaitingForSearchTerm = false; 

		    if (oData.term){
		        Portal.Portlet.Entrez_LimitsTab.Link += "?term=" + escape(oData.term);
	        }
		    window.location = Portal.Portlet.Entrez_LimitsTab.Link;        
        }       
    },
   
    /* actions from the LimitsPage */
    
    "ProcessCancel" : function(e, target, name){
		history.back();
    },
    
	"ProcessApply" : function(e, target, name){
	    this.send.SendSearchBarTerm();
	    this.send.Cmd({'cmd': 'search'});
		Portal.requestSubmit();
	},
	
	// Implementation will have to change depending on database. All Limit options will have to be cleared.
	"ProcessClearAll" : function(e, target, name){
		this.ClearTagTerms();
	},
	
	"ClearTagTerms": function(){
	    this.getInput("LimitsField").options[0].selected = true;
	},
	
	// Provide implementation for this function if you need to collect some data before the form is submitted.
	// This is necessary if you cannot directly depend on getting form element values from this
	// page into portlet attributes in your Limits portlet after submit
	"beforesubmit": function(){
	    return false;
	}

},

{
    'WaitingForSearchTerm': false,
    'Link': ''
});


// Clear all checkboxes inside target node
function setAll(nodeName, value) {
   if (!document.getElementById) return false;
   var node= document.getElementById(nodeName);

   if (node) {
      var cbs = node.getElementsByTagName("INPUT");
      for (var i = 0; i < cbs.length; i++) {
         var cb = cbs[i];
         if (cb.getAttribute("TYPE").toUpperCase() == "CHECKBOX") {
            cb.checked = value;
         } else {
             cb.value = ""; 
		 }
      }
   }
   return false;
}

;
(function( $ ){ // pass in $ to self exec anon fn

    // on page ready
    $( function() {
    
        $( 'div.portlet' ).each( function() {

            // get the elements we will need
            var $this = $( this );
            var anchor = $this.find( 'a.portlet_shutter' );
            var portBody = $this.find( 'div.portlet_content' );

            // we need an id on the body, make one if it doesn't exist already
            // then set toggles attr on anchor to point to body
            var id = portBody.attr('id') || $.ui.jig._generateId( 'portlet_content' );
            portBody.attr('id', id );
            anchor.attr('toggles', id );

            // initialize jig toggler with proper configs, then remove some classes that interfere with 
            // presentation
            var togglerOpen = anchor.hasClass('shutter_closed')? false : true; 
            anchor.ncbitoggler({
                isIcon: false,
                initOpen: togglerOpen 
            }).
                removeClass('ui-ncbitoggler-no-icon').
                removeClass('ui-widget');

            // get rid of ncbitoggler css props that interfere with portlet styling, this is hack
            // we should change how this works for next jig release
            anchor.css('position', 'absolute').
                css('padding', 0 );

            $this.find( 'div.ui-helper-reset' ).
                removeClass('ui-helper-reset');

            portBody.removeClass('ui-widget').
                css('margin', 0);

            // trigger an event with the id of the node when closed
            anchor.bind( 'ncbitogglerclose', function() {
                anchor.addClass('shutter_closed');
            });

            anchor.bind('ncbitoggleropen', function() {
                anchor.removeClass('shutter_closed');
            });

        });  // end each loop          
    });// end on page ready
})( jQuery );
/*
jQuery(document).bind('ncbitogglerclose ncbitoggleropen', function( event ) {
           var $ = jQuery;
           var eventType = event.type;
           var t = $(event.target);
           
          alert('event happened ' + t.attr('id'));
   
           if ( t.hasClass('portlet_shutter') || false ) { // if it's a portlet
               // get the toggle state
               var sectionClosed = (eventType === 'ncbitogglerclosed')? 'true' : 'false';
               alert ('now call xml-http');

            }
        });
*/

Portal.Portlet.NCBIPageSection = Portal.Portlet.extend ({
	init: function (path, name, notifier){
		this.base (path, name, notifier);
		
		this.AddListeners();
	},
    
	"AddListeners": function(){
        var oThis = this;
        
		jQuery(document).bind('ncbitogglerclose ncbitoggleropen', function( event ) {
            var $ = jQuery;
            var eventType = event.type;
            var t = $(event.target);
            
            // proceed only if this is a page section portlet {
            if ( t.hasClass('portlet_shutter')){
                var myid = '';
                if (oThis.getInput("Shutter")){
                    myid = oThis.getInput("Shutter").getAttribute('id');
                }
    
                // if the event was triggered on this portlet instance
                if (t.attr('id') && t.attr('id') == myid){
                    // get the toggle state
                    var sectionClosed = (eventType === 'ncbitogglerclose')? 'true' : 'false';
                    // react to the toggle event
                    oThis.ToggleSection(oThis.getInput("Shutter"), sectionClosed);
                }
            } // if portlet            
        });
	},
	
	"ToggleSection": function(target, sectionClosed){
	   // if remember toggle state, save the selection and log it
	   if (target.getAttribute('remembercollapsed') == 'true'){
	       this.UpdateCollapsedState(target, sectionClosed);
	   }else {
	       this.LogCollapsedState(target, sectionClosed);
	   }
	},
	
	"UpdateCollapsedState": function(target, sectionClosed){
	    var site = document.forms[0]['p$st'].value;
	    var args = { "PageSectionCollapsed": sectionClosed, "PageSectionName": target.getAttribute('pgsec_name')};
	    // Issue asynchronous call to XHR service
        var resp = xmlHttpCall(site, this.getPortletPath(), "UpdateCollapsedState", args, this.receiveCollapse, {}, this);  
	},
	
	"LogCollapsedState": function(target, sectionClosed){
	    var site = document.forms[0]['p$st'].value;
	    // Issue asynchronous call to XHR service
        var resp = xmlHttpCall(site, this.getPortletPath(), "LogCollapsedState", {"PageSectionCollapsed": sectionClosed}, this.receiveCollapse, {}, this);  
	},
	
	'getPortletPath': function(){
        return this.realname;
    }, 
    
    receiveCollapse: function(responseObject, userArgs) {
    }
	
});
		 
;
Portal.Portlet.SensorPageSection = Portal.Portlet.NCBIPageSection.extend ({
	init: function (path, name, notifier){
		this.base (path, name, notifier);
	}
});

(function( $ ){ // pass in $ to self exec anon fn

    // on page ready
    $( function() {
    
        $( 'div.sensor' ).each( function() {

            // get the elements we will need
            var $this = $( this );
            var anchor = $this.find( 'a.portlet_shutter' );
            var portBody = $this.find( 'div.sensor_content' );

            // we need an id on the body, make one if it doesn't exist already
            // then set toggles attr on anchor to point to body
            var id = portBody.attr('id') || $.ui.jig._generateId( 'sensor_content' );
            portBody.attr('id', id );
            anchor.attr('toggles', id );

            // initialize jig toggler with proper configs, then remove some classes that interfere with 
            // presentation
            var togglerOpen = anchor.hasClass('shutter_closed')? false : true; 
            anchor.ncbitoggler({
                isIcon: false,
                initOpen: togglerOpen 
            }).
                removeClass('ui-ncbitoggler-no-icon').
                removeClass('ui-widget');

            // get rid of ncbitoggler css props that interfere with portlet styling, this is hack
            // we should change how this works for next jig release
            anchor.css('position', 'absolute').
                css('padding', 0 );

            $this.find( 'div.ui-helper-reset' ).
                removeClass('ui-helper-reset');

            portBody.removeClass('ui-widget').
                css('margin', 0);

            // trigger an event with the id of the node when closed
            anchor.bind( 'ncbitogglerclose', function() {
                anchor.addClass('shutter_closed');
            });

            anchor.bind('ncbitoggleropen', function() {
                anchor.removeClass('shutter_closed');
            });

        });  // end each loop          
    });// end on page ready
})( jQuery );
;
Portal.Portlet.SmartSearch = Portal.Portlet.SensorPageSection.extend ({
	init: function (path, name, notifier){
		this.base (path, name, notifier);
	}
});


;
Portal.Portlet.Entrez_ResultsController = Portal.Portlet.extend({

	init: function(path, name, notifier) {
		console.info("Created Entrez_ResultsController");
		this.base(path, name, notifier);
	},	
		
	send: {
	    'Cmd': null
	},
		
	listen: {
	
	    /* page events */
	    
	    "RemoveFromClipboard<click>": function(e, target, name){
            this.RemoveFromClipboardClick(e, target, name);
	    },
	    
		/* messages */
		
		'Cmd': function(sMessage, oData, sSrc){
		    this.ReceivedCmd(sMessage, oData, sSrc);
		},
		
		'SelectedItemCountChanged' : function(sMessage, oData, sSrc){
		    this.ItemSelectionChangedMsg(sMessage, oData, sSrc);
		},
		
		// currently sent by searchbox pubmed in journals 
		'RunLastQuery' : function(sMessage, oData, sSrc){
			if (this.getInput("RunLastQuery")){
				this.setValue ("RunLastQuery", 'true');
			}
		}
		
	},//listen
	
	'RemoveFromClipboardClick': function(e, target, name){
	    if(confirm("Are you sure you want to delete these items from the Clipboard?")){
	        this.send.Cmd({'cmd': 'deletefromclipboard'});
		    Portal.requestSubmit();  
    	}
	},
	
	// fix to not show remove selected items message when Remove from clipboard was clicked directly on one item
	'ReceivedCmd': function(sMessage, oData, sSrc){
	    if (oData.cmd == 'deletefromclipboard'){
	        Portal.Portlet.Entrez_ResultsController.RemoveOneClip = true;
	    }
	},
	
	'ItemSelectionChangedMsg': function(sMessage, oData, sSrc){
	    // do not show any messages if one item from clipbaord was removed with direct click.
	    if (Portal.Portlet.Entrez_ResultsController.RemoveOneClip){
	        Portal.Portlet.Entrez_ResultsController.RemoveOneClip = false;
	    }
	    else{
    		this.SelectedItemsMsg(oData.count);
    	    this.ClipRemoveMsg(oData.count);
    	}
	},
	
	'SelectedItemsMsg': function(count){
	    SelMsgNode = document.getElementById('result_sel');
	    if (SelMsgNode){
	        if (count > 0){
	            SelMsgNode.className = 'result_sel';
 	            SelMsgNode.innerHTML = "Selected: " + count;
 	        }
 	        else {
 	            SelMsgNode.className = 'none';
 	            SelMsgNode.innerHTML = "";
 	        }
	    }
	},
	
	'ClipRemoveMsg': function(count){
	    ClipRemNode = document.getElementById('rem_clips');
 	    if (ClipRemNode){
 	        if (count > 0){
 	            ClipRemNode.innerHTML = "Remove selected items";
 	        }
 	        else {
 	            ClipRemNode.innerHTML = "Remove all items";
 	        }
 	    }
	},
	
	'ResultCount': function(){
	    var totalCount = parseInt(this.getValue("ResultCount"));
	    totalCount = totalCount > 0 ? totalCount : 0;
	    return totalCount;
	}

},
{
    RemoveOneClip: false
});

function getEntrezResultCount() {
    var totalCount = document.getElementById("resultcount") ? parseInt(document.getElementById("resultcount").value) : 0;
	totalCount = totalCount > 0 ? totalCount : 0;
	return totalCount;
}

;
Portal.Portlet.Pubmed_ResultsController = Portal.Portlet.Entrez_ResultsController.extend({

	init: function(path, name, notifier) {
		this.base(path, name, notifier);
	}
});

function getEntrezResultCount() {
    return $PN('Pubmed_ResultsController').ResultCount();
}
;
Portal.Portlet.Entrez_RVBasicReport = Portal.Portlet.extend({
	
	init: function(path, name, notifier) {
		console.info("Created report portlet");
		this.base(path, name, notifier);
	},
	
	send: {
		'ItemSelectionChanged': null,
		'ClearIdList': null,
		'Cmd': null
	},
	
	listen: {
		"uid<click>" : function(e, target, name){
		    this.UidClick(e, target, name);
		},
		
		"RemoveClip<click>" : function(e, target, name){
		    this.ClipRemoveClick(e, target, name);              
		}
	},
	
	'UidClick': function(e, target, name){	
		this.send.ItemSelectionChanged( { 'id': target.value,
		                                  'selected': target.checked });
	},
	
	'ClipRemoveClick': function(e, target, name){
	    this.send.ClearIdList();
		this.send.Cmd({'cmd': 'deletefromclipboard'});
		this.send.ItemSelectionChanged( { 'id': target.getAttribute('uid'),
		                                  'selected': true });
		Portal.requestSubmit();
	}
});
   

;
(function ( $, window, document, undefined ) {
    $(function(){
    //$(window).on('load',function(){
        $('.ncbi_carousel').each(function(){
            var self = $(this),
                options = {},
                optionsStr = self.data('ncbicarousel-config');
            try{
               options = eval('({' + optionsStr + '})');
            }catch(e){};
            self.ncbicarousel(options);
        });
    });
    
    //assume it is all in px for now
    //return the length without the px
    var parseCssLength = function (inpLen,defaultLen){
        defaultLen = defaultLen ? defaultLen : 0;
        var lenInPx = parseFloat(inpLen);
        lenInPx = isNaN(lenInPx) ? parseFloat(defaultLen) : lenInPx;
        return lenInPx;
    }

    var pluginName = 'ncbicarousel',
        defaultOptions = {
            imageWidth:'100px',
            numItemsVisible:7,
            toggler:false
        };

    function Plugin( element, options ) {
        this.element = $(element);
        this._ncItems = this.element.find('.nc_item');
        this._ncContent = this.element.find('.nc_content');
        this.options = $.extend( {}, defaultOptions, options) ;
        this._defaultOptions = defaultOptions;
        this._name = pluginName;
        this._imageWidth = parseCssLength(this.options.imageWidth);
        this._itemSpacing = 20; 
        this._numItems = this._ncItems.size();
        
        this.init();
    }

    Plugin.prototype.init = function () {
        this._currentStartImage = 0;
        this._setCarouselWidthAndNumItems();
        this._positionItems();
        this._insertArrows();
        this._attachScrollEvents();
        this._attachResizeEvents();
        if (this.options.toggler === true)
            this._addToggler();
        this._attachHoverEffects();
        //this._setCarouselHeight();
        var that = this;
        window.setTimeout(function(){that._updateCarouselHeight();},200);
    };
    
    Plugin.prototype._setCarouselWidthAndNumItems = function (){
        this.options.numItemsVisible = this._numItems < this.options.numItemsVisible ? this._numItems : this.options.numItemsVisible;
        this._originalNumItemsVisible = this.options.numItemsVisible;
        
        var parentWidth =  this.element.parent().width();
        var calculatedWidth = (this._imageWidth + this._itemSpacing) * this.options.numItemsVisible + 40;
        if (parentWidth < calculatedWidth){
            var numSpaceAllowing = Math.floor((parentWidth - 40) / (this._imageWidth + this._itemSpacing));
            this.options.numItemsVisible = this._originalNumItemsVisible > numSpaceAllowing ? numSpaceAllowing : this._originalNumItemsVisible;
            this._carouselWidth = (this._imageWidth + this._itemSpacing) * this.options.numItemsVisible + 40;
        }else{
            this._carouselWidth = calculatedWidth;
        }
        this.element.width(this._carouselWidth);
    }
    
    Plugin.prototype._updateCarouselWidthAndNumItems = function(){
        var parentWidth =  this.element.parent().width();
        var numSpaceAllowing = Math.floor((parentWidth - 40) / (this._imageWidth + this._itemSpacing));
        this.options.numItemsVisible = this._originalNumItemsVisible > numSpaceAllowing ? numSpaceAllowing : this._originalNumItemsVisible;
        this._carouselWidth = (this._imageWidth + this._itemSpacing) * this.options.numItemsVisible + 40;
        this.element.width(this._carouselWidth);
    }
    
    
    Plugin.prototype._setCarouselHeight = function(){
        var maxImageHeight = 0;
        var that = this;
        this._ncItems.each(function(){
           currImage = $(this).find('img');
           var imageHeight = currImage.height();
           //console.log('imageHeight',imageHeight);
           if(imageHeight == 0)
                that._updateHeight = true;
           maxImageHeight = maxImageHeight > imageHeight ? maxImageHeight : imageHeight;
        });
        this._ncContent.height(maxImageHeight + 20);
    }
    
    Plugin.prototype._updateCarouselHeight = function(){
        var maxImageHeight = 0;
        this._ncItems.each(function(){
           var self = $(this);
           currImage = self.find('img');
           var imageHeight = currImage.height();
           if(imageHeight == 0){
               self.css('left','-1000px').css('display','inline-block'); 
               imageHeight = currImage.height();
               self.css('display','none');
           }
           maxImageHeight = maxImageHeight > imageHeight ? maxImageHeight : imageHeight;
        });
        var newHeight = maxImageHeight + 20;
        this._ncContent.height(newHeight);
        this.element.find('.nc_arrow').height(newHeight);
    }
    
    
    Plugin.prototype._setNumItemsVisible = function(){
        this.options.numItemsVisible = this._numItems < this.options.numItemsVisible ? this._numItems : this.options.numItemsVisible;
        this._originalNumItemsVisible = this.options.numItemsVisible;
    }
    
    Plugin.prototype._insertArrows = function (){
        var contentHeight = this._ncContent.height();
        var leftArrow = $('<a href="#" class="nc_arrow nc_arrow_left nc_arrow_inactive"/>').
            insertBefore(this._ncContent).height(contentHeight);
        var rightArrow = $('<a href="#" class="nc_arrow nc_arrow_right nc_arrow_inactive"/>').
            insertAfter(this._ncContent).height(contentHeight);
        this._updateArrowsStatus(rightArrow.add(leftArrow));
    }
    
    Plugin.prototype._updateArrowsStatus = function (arrows){
        var arrows = arrows || this.element.find('.nc_arrow');
        if (this.options.numItemsVisible < this._numItems)
            arrows.removeClass('nc_arrow_inactive').addClass('nc_arrow_active');
        else
            arrows.addClass('nc_arrow_inactive').removeClass('nc_arrow_active');
    }
    
    Plugin.prototype._positionItems = function () {
        var startLeft = 30,
            that = this,
            currImagePos = 0;
        this._ncItems.each(function(){
           var self = $(this);
           if((currImagePos >= that._currentStartImage && 
               currImagePos < that._currentStartImage + that.options.numItemsVisible) ||
               (currImagePos < that._currentStartImage &&
               currImagePos + that._numItems < that._currentStartImage + that.options.numItemsVisible)){
               
               var factor = currImagePos < that._currentStartImage ? that._numItems : 0;
               var currLeft = startLeft + (that._imageWidth + that._itemSpacing) * ((currImagePos + factor - that._currentStartImage) % that.options.numItemsVisible );
               self.css('left',currLeft + 'px').css('display','inline-block');  
           }else{
               self.css('display','none');
           }
           self.find('img').width(that._imageWidth);
           currImagePos += 1;
        });
    }
    
    Plugin.prototype._attachScrollEvents = function(){
        var that = this;
        this.element.find('.nc_arrow').on('click',function(e){
            e.preventDefault();
            var self = $(this);
            var pingData = {};
            if (!self.hasClass('nc_arrow_inactive')){
                pingData.Scrolled = "Yes";
                if (self.hasClass('nc_arrow_right')){
                    pingData.ScrollSide = "Right";
                    that._moveRight();
                }
                else{
                    pingData.ScrollSide = "Left";
                    that._moveLeft();                    
                }
            }
            else{
                pingData.Scrolled = "No";
                pingData.ScrollSide = self.hasClass('nc_arrow_right') ? "Right" : "Left";
            }
            if (ncbi && ncbi.sg && ncbi.sg.ping)
                ncbi.sg.ping(pingData);
        });
    }
    
    Plugin.prototype._moveLeft = function (){
        if (this._currentStartImage == 0)
            this._currentStartImage = this._numItems-1;
        else
            this._currentStartImage -=1;
        this._positionItems();
    }
    
    Plugin.prototype._moveRight = function (){
        if (this._currentStartImage == (this._numItems-1))
            this._currentStartImage = 0;
        else
            this._currentStartImage +=1;
        this._positionItems();
    }
    
    Plugin.prototype._attachResizeEvents = function(){
        var that = this;
        var resizeAction;
        function doResize(){
                that._updateCarouselWidthAndNumItems();
                that._positionItems();
                that._updateArrowsStatus();
                that._attachHoverEffects();
            }
        $(window).on('resize',function(e){
              window.clearTimeout(resizeAction);
              resizeAction = setTimeout(doResize, 200);
        });
    }
    
    Plugin.prototype._addToggler = function (){
        var that = this;
        that.element.find('.nc_header').append('<a href="#" class="nc_toggler">&nbsp;</a>')
            .find('.nc_toggler')
            .on('click',function(e){
                e.preventDefault();
                $(this).toggleClass('nc_toggler_closed');
                that._ncContent.toggle();
            });
    }
    
    Plugin.prototype._attachHoverEffects = function(){
        var that = this;
        var activeArrows = this.element.off('hover').find('.nc_arrow_active');
        if (activeArrows.size() > 0){
            this.element.hover(function(){
                activeArrows.addClass('nc_arrow_highlight');
            },function(){
                activeArrows.removeClass('nc_arrow_highlight');
            });
        }

    }

    $.fn[pluginName] = function ( options ) {
        return this.each(function () {
            if (!$.data(this, 'plugin_' + pluginName)) {
                $.data(this, 'plugin_' + pluginName, 
                new Plugin( this, options ));
            }
        });
    }

})(jQuery, window, document);
;
Portal.Portlet.Pubmed_RVAbstract = Portal.Portlet.Entrez_RVBasicReport.extend({
	init: function(path, name, notifier) {
		this.base(path, name, notifier);
		
		jQuery(".abstract .other_lang a").on("click",function(e){
		    e.preventDefault();
		    var oThis = jQuery(this);
		    if (oThis.hasClass("sel_lang"))
		        return;
		    oThis.siblings("a").removeClass("sel_lang");
		    var lang_id_class = jQuery.trim(oThis.attr('class')).substr(2);
		    oThis.addClass("sel_lang");
		    var outerAbstrDiv = oThis.closest(".abstr");
		    var otherAbstrsDiv = oThis.closest(".other_lang");
		    var currAbstr = outerAbstrDiv.find("div[class^=abstr_]:visible");
		    var selAbstr = outerAbstrDiv.find("." + lang_id_class);
		    otherAbstrsDiv.append(currAbstr.hide());
		    outerAbstrDiv.append(selAbstr.show());
		});
	},
	
	send: {
		'ItemSelectionChanged': null,
		'ClearIdList': null,
		'Cmd': null,
        'AppendTerm': null
	},
	
	listen: {
		"img_strip_Closed<click>" : function(e, target, name){
		    this.saveImageStripStateLocal(e, target, name);
		},
		"uid<click>" : function(e, target, name){
		    this.UidClick(e, target, name);
		},
		
		"RemoveClip<click>" : function(e, target, name){
		    this.ClipRemoveClick(e, target, name);              
		}
	},
	"saveImageStripStateLocal":function(e, target, name){
	    var site = document.forms[0]['p$st'].value;
	    var args = {"ImageStripClosed": this.getValue("img_strip_Closed")};
	    // Issue asynchronous call to XHR service
        var resp = xmlHttpCall(site, this.realname, "SaveImageStripState", args, this.receiveImageStripState, {}, this);    
	},
    'receiveImageStripState':function(responseObject, userArgs){
        var resp = responseObject.responseText;
        try {
            // Handle timeouts
            if (responseObject.status == 408) {
                //this.showMessage("Server currently unavailable. Please check connection and try again.","error");
                alert("Server currently unavailable. Please check connection and try again.");
                return;
            }
/*            console.log("response = " + resp);
            resp = '(' + resp + ')';
            var JSONobj = eval(resp);
            console.dir(JSONobj); */           
 
        } catch (e) {
            //this.showMessage("Server error: " + e, "error");
            alert("Server error: " + e);
        }
    }
	
});


(function($){
    $(function(){
        $("span.status_icon").click(
            function(e){
                e = e || window.event;
                ncbi.sg.ping (this, e, "mistakenlink");
            }
        );
        
/*        $.ncbi.share.beforeShare = function(node){
            console.log('beforeShare',node);
        }*/
    
    });//End DOM ready
})(jQuery);

function HistViewTerm(term, op, num) {
    Portal.$send('AppendTerm', {'op': op, 'term': term.replace(/%22/g,"\"")})
}

;
jQuery(function(){

    var osH = XMLHttpRequest.prototype.setRequestHeader;
    XMLHttpRequest.prototype.setRequestHeader = function(){
        var headerName = arguments[0];
        if (headerName !== 'NCBI-PHID' && headerName !== 'NCBI-SID')
            return osH.apply(this,arguments);
    }

    jQuery('a[abstractLink=yes]').each( function(){
        var elem = jQuery(this);
        var sec=elem.attr('alsec');
        var term=elem.attr('alterm'); //db, base, sec, term,json
        elem.ncbilinksmenu({hasArrow:true,localJSON : function(){return AbstractLink.getMenu({db:'pubmed',base:1,sec:sec,term:term,json:true});}});
        elem.ncbilinksmenu('option','preventDefault','*[href="#"]');
    });
});
;
(function($){
    $(function(){
       $("span.img_strip_title").bind("click",function(e) {ncbi.sg.ping(this,e,"mistakenlink");}); 
    });
})(jQuery);



;
var AbstractLink = function (){

	var eUtils = "//eutils.ncbi.nlm.nih.gov/entrez/eutils";
	
	var secDb = {
		"mesh": ["PubMed", "MeSH", "Add to Search"],
		"jour": ["PubMed", "NLM Catalog", "Add to Search"],
		"subs": ["PubMed", "MeSH", "Add to Search"],
		"supp": ["PubMed", "MeSH", "Add to Search"],
		"ssid": ["PubMed", "Nucleotide", "Protein", "OMIM", "Structure", "ClinicalTrials.gov", "GEO", "ISRCTN Controlled Trials", "PubChem Substance", 
		        "PubChem BioAssay", "PubChem Compound","ANZCTR","BioProject","ChiCTR","CRiS","CTRI","dbGaP","dbSNP","dbVar","Dryad","DRKS","EudraCT",
		        "figshare","IRCT","JPRN","NTR","PACTR","ReBec","RPCEC","SLCTR","SRA","UniMES","UniParc","UniProtKB","UniRef"],
		"grnt": ["PubMed"],
		"ptyp": ["PubMed", "MeSH", "Add to Search"],
		"pnam": ["PubMed"]
	};
	
	var secProc = {
		"mesh": procMesh,
		"jour": procJour,
		"subs": procSubs,
		"supp": procSupp,
		"ssid": procSsid,
		"grnt": procGrnt,
		"pnam": procDefault,
		"ptyp": procDefault
	};
	
	var secToField = {
		"jour": "[jour]",
		"subs": "[nm]",
		"supp": "[nm]",
		"grnt": "[Grant Number]",
		"pnam": "[PS]",
		"ptyp": "[pt]"
	};
	
	var pcDbTrans = {
		"PubMed":"pubmed",
        "NLM Catalog":"nlmcatalog",
		"PubChem Substance":"pcsubstance",
    	"PubChem BioAssay":"pcassay",
    	"PubChem Compound":"pccompound"
	};
	
	var pcFieldTrans = {
		"PubChem Substance":"[Synonym]",
    	"PubChem BioAssay":"[All]",
    	"PubChem Compound":"[Synonym]"
	};
	
	var baseList = ["/entrez/query.fcgi", "/entrez/"];
	
	
	function getDbTrans(db){
	    var dbTrans = pcDbTrans[db];
	    return dbTrans ? dbTrans : db;
	}
	
	function procJour(currDb, term, sec){
		term = term.replace(/\.\s*$/,'');
		var field = secToField[sec];
		if (currDb.match(/nlmcatalog/i))
		    field = "[Title Abbreviation]";
		else if (currDb.match(/pubmed/i))
		    field = "[jour]";
  		//return "db=" + currDb + "&term=%22" + term + "%22" + field; 
  		return "/" + currDb.toLowerCase() + "?term=%22" + term + "%22" + field;
	}
	
	function procDefault(currDb, term, sec){
		//return "db=" + currDb + "&term=" + term + secToField[sec]; 
		var modifier = secToField[sec];
		currDb = currDb.toLowerCase();
		if (currDb == 'pubmed' && modifier == '[pt]')
		    return "/" + currDb + "?term=%22" + term + "%22" + modifier;
		else if (currDb == 'mesh' && modifier == '[pt]')
		    return "/" + currDb+ "?term=%22" + term + "%22";
		return "/" + currDb + "?term=" + term + secToField[sec];
	}
	
	function procGrnt(currDb, term, sec){
		term = term.replace(/^[A-Za-z ]*(\d{6}).*?\/Wellcome Trust/i,'$1\/Wellcome Trust');
  		//return "db=" + currDb + "&term=" + term + secToField[sec]; 
  		return "/" + currDb.toLowerCase() + "?term=" + term + secToField[sec];
	}
	
	function secMeshField(term){
		return term.match(/\*/) ? "[MAJR]" : "[MeSH Terms]";
	}
	
	function secMeshEditTerm(term, dbTo){
		term = term.replace(/\*/g, '');
		term = term.replace(/ %26 /g, ' and ');
		if (dbTo == 'MeSH') 
			term = term.replace(/\/.+$/, '');
		return term;
	}
	
	function procMesh(currDb, term, sec){
		//var field = currDb == "MeSH" ? "[mh]" : secMeshField(term);
		var field = currDb == "MeSH" ? "" : secMeshField(term);
		//return "db=" + currDb + "&term=%22" + secMeshEditTerm(term, currDb) + "%22" + field;
		return "/" + currDb.toLowerCase() + "?term=%22" + secMeshEditTerm(term, currDb) + "%22" + field;
	}
	
	function procSubs(currDb, term, sec){
		if (currDb == 'PubMed' || currDb == 'pubmed')
		    return "/" + currDb.toLowerCase() + "?term=%22" + term + "%22[nm]";
			//return "db=" + currDb + "&term=%22" + term + "%22[Substance Name]";
		if (currDb == "MeSH"){
			term = term.replace(/ %26 /g,' and ');
    		//return "db=" + currDb + "&term=%22" + term + "%22";
    		return "/" + currDb.toLowerCase() + "?term=%22" + term + "%22";
		}
		
		//TO-DO: the following code never runs ... copied from the perl as it is
		return null;
		var field = pcFieldTrans[currDb];
		currDb = pcDbTrans[currDb];
		var plusTerm = term;
		plusTerm = plusTerm.replace(/ /g,'+');
		
		//get("$eutils/esearch.fcgi?db=$db_name&term=%22$plus_term%22$field&rettype=count&tool=NCBI_AL")  =~ m|<Count>(\d+)</Count>|;
   		//return $1? "db=$db_name&term=%22$term%22$field" : undef;
		//return currDb ? "db=" + currDb + "&term=%22" + term + "%22" + field : null;
	}
	
	function procSupp(currDb, term, sec){
		if (currDb == 'PubMed' || currDb == 'pubmed')
		    return "/" + currDb.toLowerCase() + "?term=%22" + term + "%22[nm]";
			//return "db=" + currDb + "&term=%22" + term + "%22[Substance Name]";
		if (currDb == "MeSH"){
			term = term.replace(/ %26 /g,' and ');
    		//return "db=" + currDb + "&term=%22" + term + "%22";
    		return "/" + currDb.toLowerCase() + "?term=%22" + term + "%22";
		}
	}
	
	function procSsid(currDb, term, sec){
		//console.debug("procSsid");
		var rcTerm;
  	
		if (currDb == "Nucleotide" && term.match(/^GENBANK\/(.+)/i) ){
			rcTerm = RegExp.$1;
			if (rcTerm.match(/^(NP|XP|[A-Za-z]{3}\d{5}|P|Q)/i)) 
				return null;
			rcTerm = rcTerm.replace(/^(NM|NC|NT|NG|NP|XM|XP|XR)/i,'$1_');
		}
		else if (currDb == "Protein" && term.match(/^GENBANK\/([A-Za-z]{3}\d{5}|P.+|Q.+)/i) ){
     		rcTerm = RegExp.$1;
  		}
		else if( currDb == "Protein" && term.match(/^GENBANK\/(NP|XP)(.+)/i)) {
     		rcTerm = RegExp.$1 + "_" + RegExp.$2; 
  		}
		else if( currDb == "Protein" && term.match(/^RefSeq\/([ANXYZ]P)_(\d+)/i)) {
     		rcTerm = RegExp.$1 + "_" + RegExp.$2; 
  		}
		else if(currDb == "Nucleotide" && !(term.match(/^RefSeq\/([ANXYZ]P)_(\d+)/i)) && term.match(/^RefSeq\/(..)_(\d+)/i) ) {
     		rcTerm = RegExp.$1 + "_" + RegExp.$2;
  		}
		else if(currDb == "Protein" && term.match(/^(PIR|SWISSPROT)\/(.+)/i) ) {
    		rcTerm = (RegExp.$1 == "PIR")? (RegExp.$2).toLowerCase() : RegExp.$2;    
  		}
		else if(currDb == "OMIM" && term.match(/^OMIM\/(.+)/i)) {
		    rcTerm = RegExp.$1 + "[mim]";
		}  
		else if(currDb == "Structure" && term.match(/^PDB\/(.+)/i)) { 
		    rcTerm = RegExp.$1;
		}
		else if(currDb == "ClinicalTrials.gov" && term.match(/^ClinicalTrials.gov\/(.+)/i)) { 
		    return "http://clinicaltrials.gov/show/" + RegExp.$1;
		}
		else if(currDb == "GEO" && term.match(/^GEO\/(.+)/i) ) { 
    		return "http://www.ncbi.nlm.nih.gov/projects/geo/query/acc.cgi?acc=" + RegExp.$1;
  		}
		else if(currDb == "ISRCTN Controlled Trials" && term.match(/^ISRCTN\/(ISRCTN\d+)/i) ) { 
    		return "http://www.controlled-trials.com/" + RegExp.$1;
  		}
		else if(currDb == "PubMed" && term.match(/^(ClinicalTrials\.gov|GDB|GENBANK|OMIM|PIR|SWISSPROT|ISRCTN)\/([A-Za-z]?)/i) )  { 
    		rcTerm = (RegExp.$2 ? RegExp.$2 + RegExp.rightContext : RegExp.$2 + "/" + RegExp.rightContext) + "[Secondary Source ID]";
  		}
		else if(currDb == "PubMed" && term.match(/^RefSeq\/(.._\d+)/i)) {
     		rcTerm = RegExp.$1 + "[Secondary Source ID]"; 
  		}  
		else if(currDb == "PubMed" && term.match(/^(PDB|GEO)\//i)) { 
    		rcTerm = term + "[Secondary Source ID]";
  		}
/*  		else if (currDb.match(/PubChem\s+(\S+)/) && term.match(new RegExp('^PubChem-(' + RegExp.$1 + ')\/(\\d+)'))) {
			currDb = "pc" + (RegExp.$1).toLowerCase();
			if (currDb == "pcbioassay") 
				currDb = "pcassay";
			rcTerm = RegExp.$2 + "[uid]";
		}*/ 
		else if (currDb == "pcsubstance" && term.match(/^PubChem-Substance\/(\d+)/)){ //|| currDb == "pcassay" || currDb == "pccompound")
		     return "http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?sid=" + RegExp.$1;
		}
		else if (currDb == "pcassay" && term.match(/^PubChem-BioAssay\/(\d+)/)){ //|| currDb == "pcassay" || currDb == "pccompound")
		     return "http://pubchem.ncbi.nlm.nih.gov/assay/assay.cgi?aid=" + RegExp.$1;
		}
    	else if(currDb == "ANZCTR" && term.match(/^ANZCTR\/(.+)/i)){
  		    return "https://anzctr.org.au/Trial/Registration/TrialReview.aspx?ACTRN=" + RegExp.$1;
  		}
      	else if(currDb == "BioProject" && term.match(/^BioProject\/(.+)/i)){
  		    return "http://www.ncbi.nlm.nih.gov/bioproject/" + RegExp.$1;
  		}
        else if(currDb == "ChiCTR" && term.match(/^ChiCTR\/(.+)/i)){
  		    return "http://www.chictr.org/en/proj/search.aspx?regno=ChiCTR-" + RegExp.$1;
  		}
  		else if(currDb == "CRiS" && term.match(/^CRiS\/(.+)/i)){
  		    return "http://cris.nih.go.kr/cris/en/search/basic_search.jsp?searchword=" + RegExp.$1;
  		}
    	else if(currDb == "CTRI" && term.match(/^CTRI\/(.+)/i)){
  		    return "http://ctri.nic.in/Clinicaltrials/showallp.php?mid1=3298&EncHid=&userName=" + RegExp.$1;
  		}
        else if(currDb == "dbGaP" && term.match(/^dbGaP\/([^\.V]+)/i)){
  		    return "http://www.ncbi.nlm.nih.gov/gap/?term=" + RegExp.$1;
  		}
        else if(currDb == "dbSNP" && term.match(/^dbSNP\/(.+)/i)){
  		    return "http://www.ncbi.nlm.nih.gov/projects/SNP/snp_retrieve.cgi?subsnp_id=ss" + RegExp.$1;
  		}
        else if(currDb == "dbVar" && term.match(/^nstd(.+)/i)){
  		    return "http://www.ncbi.nlm.nih.gov/dbvar/studies/nstd" + RegExp.$1;
  		}
  		else if(currDb == "dbVar" && term.match(/^nsv(.+)/i)){
  		    return "http://www.ncbi.nlm.nih.gov/dbvar/variants/nsv" + RegExp.$1;
  		}
  		else if(currDb == "Dryad" && term.match(/^Dryad\/(.+)/i)){
  		    return "http://datadryad.org/resource/doi:10.5061/dryad." + RegExp.$1.toLowerCase();
  		}
  		else if(currDb == "DRKS" && term.match(/^DRKS\/(.+)/i)){
  		    return "https://drks-neu.uniklinik-freiburg.de/drks_web/navigate.do?navigationId=trial.HTML&TRIAL_ID=" + RegExp.$1.toUpperCase();
  		}
  		else if(currDb == "EudraCT" && term.match(/^EudraCT\/(.+)/i)){
  		    return "https://www.clinicaltrialsregister.eu/ctr-search/search?query=" + RegExp.$1.slice(0,4) + "-" + RegExp.$1.slice(4);
  		}
   		else if(currDb == "figshare" && term.match(/.*figshare.*/i)){
  		    return "http://dx.doi.org/" + RegExp.$1;
  		}
   		else if(currDb == "IRCT" && term.match(/^IRCT\/(.+)/i)){
  		    return "http://www.irct.ir/searchen.php?field=a&lang=en&keyword=" + RegExp.$1;
  		}
   		else if(currDb == "JPRN" && term.match(/^JPRN\/(.+)/i)){
  		    return "http://rctportal.niph.go.jp/en/detail?trial_id=" + RegExp.$1;
  		}
   		else if(currDb == "NTR" && term.match(/^NTR\/(NTR)?(.+)/i)){
  		    return "http://www.trialregister.nl/trialreg/admin/rctview.asp?TC=" + RegExp.$2;
  		}
  		else if(currDb == "PACTR" && term.match(/^PACTR\/(.+)/i)){
  		    return "http://www.pactr.org/";
  		}
  		else if(currDb == "ReBec" && term.match(/^ReBec\/(RBR)?(.+)/i)){
  		    return "http://www.ensaiosclinicos.gov.br/rg/RBR-" + RegExp.$2;
  		}
  		else if(currDb == "RPCEC" && term.match(/^RPCEC\/(.+)/i)){
  		    return "http://registroclinico.sld.cu/en/trials/RPCEC" + RegExp.$1 + "-En";
  		}
    	else if(currDb == "SLCTR" && term.match(/^SLCTR\/(.+)/i)){
  		    return "http://www.slctr.lk/";
  		}
  		else if(currDb == "SRA" && term.match(/^SRA\/(.+)/i)){
  		    return "http://www.ncbi.nlm.nih.gov/sra/" + RegExp.$1;
  		}
  		else if(currDb == "UniMES" && term.match(/^UniMES\/(.+)/i)){
  		    return "http://www.uniprot.org/";
  		}
   		else if(currDb == "UniParc" && term.match(/^UniParc\/(.+)/i)){
  		    return "http://www.uniprot.org/uniparc/" + RegExp.$1;
  		}
  		else if(currDb == "UniProtKB" && term.match(/^UniProtKB\/(.+)/i)){
  		    return "http://www.uniprot.org/uniprot/?sort=score&query=" + RegExp.$1;
  		}
  		else if(currDb == "UniRef" && term.match(/^UniRef\/(.+)/i)){
  		    return "http://www.uniprot.org/uniref/" + RegExp.$1;
  		}
		
		//return rcTerm ? "db=" + currDb + "&term=" + rcTerm : null;
		return rcTerm ? "/" + currDb.toLowerCase() + "?term=" + rcTerm : null;
  
	}
	
	
	function AddToSearch(term,termToAdd, sec){
		var field = sec == 'mesh' ? secMeshField(term) : secToField[sec];
		
	    if (termToAdd.match(/\*$/)){
	        termToAdd = termToAdd.replace(/\*$/,'');
	        field = '[MAJR]';
	    }
	    if (! field.match(/^\[/))
	        field = '[' + field;
	    if (! field.match(/\]$/))
	        field = field + ']';
	    
	    if (sec != 'subs') 
	    	termToAdd = termToAdd.replace(/ & /g, ' and ');
		
		return '{"text":"Add to Search","click":function(e,popObj){ HistViewTerm(\'%22' + termToAdd + '%22' + field + '\',\'AND\',1);}}';
		
	}
	
	var mhOut = {};
	
	
	function addElink(term, field, baseUrl) {
		//mhOut = {};
		 term = term.replace(/[\/|\*].+$/,'');
		 var outLine = mhOut[term];
		 if (! outLine){
		 	var plusTerm = term;
			plusTerm = plusTerm.replace(/ /,'+');
			var es;
			jQuery.ajax({type:"GET",
					async:false,
					url: eUtils + '/esearch.fcgi?db=mesh&term=%22' + plusTerm + '%22[' + field + ']&tool=NCBI_AL',
					dataType: "xml",
					success: function(xml){
						es = xml;
					}
			});
			//console.debug(es);
			var termNo = jQuery(jQuery(es).find('Id')[0]).text();
			//console.log('itemNo',termNo);
			
			var el;
			jQuery.ajax({type:"GET",
					async:false,
					url: eUtils + '/elink.fcgi?dbfrom=mesh&id=' + termNo + '&cmd=acheck&tool=NCBI_AL',
					dataType: "xml",
					success: function(xml){
						el = xml;
					}
			});
			//console.debug(jQuery(el));
			var rc = [];
			jQuery(el).find('LinkInfo').each( function(){
				var linkName = jQuery(this).find('LinkName').text();
				var menuTag = jQuery(this).find('MenuTag').text();
				var dbTo = jQuery(this).find('DbTo').text();
				menuTag = menuTag.replace(/\s+Links\s*$/,'');
				if (dbTo.match(/pccompound|pcsubstance/) && !linkName.match(/_noexp$/))
				    linkName = linkName + '_noexp';
				//console.log('inside',this,linkName,menuTag);
				var link = baseUrl + '?db=mesh&cmd=Display&dopt=' + linkName + '&from_uid=' + termNo;
				//var link ='db=mesh&cmd=Display&dopt=' + linkName + '&from_uid=' + termNo;
				rc.push('{"text":"' + menuTag + '","href":"' + link + '"}');
			});	
			outLine = mhOut[term] = rc.join(',');		
			
		 }
		return outLine;		
	}
	
	function AbstractLinkInner(opts){
	    var db = opts.db, base = opts.base, sec = opts.sec, term = opts.term,json = opts.json;
		
		var termToAdd = term.replace(/\'/g, "\\'");
		term = term.replace(/&/g, '%26');
		term = term.replace(/\'/g, "%27");
		
		var tmpBase = Number(base);
		var baseNo = isNaN(tmpBase) ? 0 : tmpBase < 0 || tmpBase > baseList.length ? 0 : tmpBase;
		var baseUrl = baseList[baseNo];
		//console.debug(baseNo, baseUrl);
		
		var currDbList = secDb[sec] || [];
		//console.debug(currDbList);
		
		var currFun = secProc[sec] || procDefault;
		//currFun();
		
		var retArr = [];
		
		var sizeCurrDbList = currDbList.length;
		
		//for (var ind in currDbList) {
		for(var ind=0; ind<sizeCurrDbList; ind++){
			var currDb = currDbList[ind]
			//console.debug("currDB : ", currDb);
			if (currDb == "Add to Search") {
				retArr.push(AddToSearch(term,termToAdd, sec));
			}
			else {
				var link = currFun(getDbTrans(currDb), term, sec);
				if (link) {
					link = link.replace(/ /g, '+');
					link = link.replace(/%26/g, '%2526');
					//link = link.match(/^http:/) ? link : baseUrl + '?' + link;
					retArr.push('{"text":"Search in ' + currDb + '","href":"' + link + '"}');
				}
			}
		}
		
		if (sec == "mesh" || sec == "subs"){
		    //retArr.push(addElink(term, "Multi", baseUrl));
		    var ret = addElink(term, "subs", baseUrl);
		    if (ret)
		        retArr.push();
		}
		
		
		var retStr = '{"links":[{"heading":"Actions"},' + retArr.join(',') + ']}';
		return json? eval('(' + retStr + ')') : retStr;
		
	}

	
	/* the public functions
	--------------------------------------------------------*/
	return {
		getMenu: function(opts){
		      return AbstractLinkInner(opts);
		}
	};
	
	
}();

;
(function ($){
    $(function(){
        $(document).on("disco.ajax",function(){
            $("#search_details_ph").replaceWith($("#search_details").show());
            //window.setTimeout(function(){$.ui.jig.scan(node);},2);
            window.setTimeout(function(){ncbi.sg.scanLinks();},2);
         });
         
         $("#SearchDetailsQuery").on("click",function(e){
             e.preventDefault();
             var inp = $("#search_details textarea");
             window.location = "/" + inp.data("db") + "/?term=" + encodeURIComponent(inp.val()) +
                 "&cmd=DetailsSearch";
         });
    });
})(jQuery);


;
(function($){
    
    (function(){
        if(!$.ncbi)
            $.extend($,{ncbi:{}});
        if(!$.ncbi.URL)
            $.extend($.ncbi,{URL:{}});
        $.extend($.ncbi.URL,{
            splitDiscoUrlParams:function(searchUrl){
                searchUrl = searchUrl ? searchUrl : window.location.seach;
                var params = searchUrl.split('&');
                var pObj = {};
                $.each(params,function(i,val){
                    var portlets = val.match(/(portlets)=(.*)/);
                    if(portlets){
                        pObj[portlets[1]] = portlets[2];
                    }
                    else{
                        var pair = val.split('=');
                        pObj[pair[0]] = pair[1];
                    }
                });
                return pObj;
            }
        });
        
        //utility function to append css and js from ajax fetched page
        $.extend($.ncbi.URL,{
            appendNewResources:function(newHtml){
                 function componentIdsArray(rsArray,type){
                    var comps = [];
                    var key = type == "js" ? "src" : "href";
                    rsArray.each(function(){
                        var link = this[key];
                        var stIndex = link.indexOf(type);
                        if (stIndex != -1){
                            stIndex = stIndex + type.length + 1;
                            var idsStr = link.substring(stIndex).replace("." + type,"");
                            $.each(idsStr.split("/"),function(i,v){comps.push(v)});
                        }
                    });
                    return comps;
                }
            
                //current portal css and js
                var eJs = $("script[snapshot]");
                var eCss = $("link[rel=stylesheet]").filter(function(){return !(this["xmlns"]) && this["href"].indexOf("portal3rc.fcgi") != -1 })
                
                var eJsIds = componentIdsArray(eJs,"js");            
                var eCssIds = componentIdsArray(eCss,"css");
                
                //get the new portal css and js
                var nCss = newHtml.filter("link").filter(function(i){if (this["rel"] == "stylesheet" && this["type"] == "text/css" && this["href"].indexOf("portal3rc.fcgi") != -1) return true; else return false;});
                var nJs = newHtml.filter("script[snapshot]");
                var nJsIds = componentIdsArray(nJs,"js");
                var nCssIds = componentIdsArray(nCss,"css");
                
                function arrayDiff(arr1,arr2){
                    var diffArray = [];
                    $.each(arr1,function(i,v){
                        if ($.inArray(v,arr2) == -1)
                            diffArray.push(v);
                    });
                    return diffArray;
                }
                
                var nCssDiff = arrayDiff(nCssIds,eCssIds);
                var nJsDiff = arrayDiff(nJsIds,eJsIds);
                
                var eHead = document.getElementsByTagName("head")[0];
                appendToHead("js",eHead,nJsDiff,nJs);
                appendToHead("css",eHead,nCssDiff,nCss);
                
                function appendToHead(type,head,arrayIds,srcUrls){
                    if (!srcUrls[0]) return;
                    if (type == "js"){
                        var urlBase = srcUrls[0].src.substr(0,srcUrls[0].src.indexOf(type)).replace(/https?:/,"");
                        var scrpt = document.createElement("script");
                        scrpt.type = "text/javascript";
                        scrpt.src = urlBase + "js/" + (arrayIds.join("/")) + ".js";
                        head.appendChild(scrpt);        
                    }
                    else if (type = "css"){
                        var urlBase = srcUrls[0].href.substr(0,srcUrls[0].href.indexOf(type)).replace(/https?:/,"");
                        var lnk = document.createElement("link");
                        lnk.type = "text/css";
                        lnk.rel = "stylesheet";
                        lnk.href = urlBase + "css/" + (arrayIds.join("/")) + ".css";
                        eHead.appendChild(lnk);
                    }
                }
                
                //now append if there is any non portal css/js
                var nCss = newHtml.filter("link").filter(function(i){if (this["rel"] == "stylesheet" && this["type"] == "text/css" && this["href"].indexOf("portal3rc.fcgi") == -1) return true; else return false;});
                var nJs = newHtml.filter("script").filter(function(){return this["src"].indexOf("portal3rc.fcgi") == -1});
                nCss.each(function(){
                    var lnk = document.createElement("link");
                    lnk.type = "text/css";
                    lnk.rel = "stylesheet";
                    lnk.href = this["href"];
                    eHead.appendChild(lnk);
                });
                nJs.each(function(){
                    var scrpt = document.createElement("script");
                    scrpt.type = "text/javascript";
                    scrpt.src = this["src"];
                    eHead.appendChild(scrpt);    
                });
                
          }//end appendNewResources
            
        });
        
    })();
    
    $(function(){
        var discCol = $("#disc_col");
        var fullUrl = (discCol.find("a").attr("href").replace("&page=full","") + "&ncbi_phid=" + $("meta[name=ncbi_phid]")[0].content).split('?');
        
        var ajaxCall = $.ajax({
            url: fullUrl[0] ,
            timeout:15000,
            dataType:'html',
            type:'POST',
            data:$.ncbi.URL.splitDiscoUrlParams(fullUrl[1])
        });
        ajaxCall.done( function(htmlData){
            var $htmlData = $(htmlData);
            var data = $htmlData.filter("#disc_col");
            discCol.replaceWith(data);
            $.ncbi.URL.appendNewResources($htmlData);
            $.ui.jig.scan($("#disc_col"));
            window.setTimeout(function(){ncbi.sg.scanLinks();},2);
            initPageSections(); //this is needed for the brief link page section portlets
            $(document).trigger("disco.ajax");
        });//end of ajaxCall.done
        
        ajaxCall.fail( function(data){
            discCol.remove();
            //jQuery('#disc_col a').text('click here').attr('target','blank');
        });
            
            
    
    function initPageSections(){
        //copied from the pagesectiongroup js as this was done on document ready
      discCol.find('div.portlet, div.section').each( function() {

            // get the elements we will need
            var self = $(this);
            var anchor = self.find('a.portlet_shutter');
            var content = self.find('div.portlet_content, div.sensor_content');

            // we need an id on the body, make one if it doesn't exist already
            // then set toggles attr on anchor to point to body
            var id = content.attr('id') || $.ui.jig._generateId('portlet_content');
            
            anchor.attr('toggles', id);
            content.attr('id', id);

            // initialize jig toggler with proper configs, then remove some classes that interfere with 
            // presentation
            var togglerOpen = anchor.hasClass('shutter_closed')  ?  false  :  true; 

            anchor.ncbitoggler({
                isIcon: false,
                initOpen: togglerOpen 
            }).
                removeClass('ui-ncbitoggler-no-icon').
                removeClass('ui-widget');

            // get rid of ncbitoggler css props that interfere with portlet styling, this is hack
            // we should change how this works for next jig release
            anchor.css('position', 'absolute').
                css('padding', 0 );

/*            self.find( 'div.ui-helper-reset' ).
                removeClass('ui-helper-reset');

            content.removeClass('ui-widget').
                css('margin', 0);*/

            // trigger an event with the id of the node when closed
            anchor.bind( 'ncbitogglerclose', function() {
                anchor.addClass('shutter_closed');
                
                $.post('?', { section_name: anchor.attr('pgsec_name'), new_section_state: 'true' });
            });

            anchor.bind('ncbitoggleropen', function() {
                anchor.removeClass('shutter_closed');
                $.post('?', { section_name: anchor.attr('pgsec_name'), new_section_state: 'false' });
            });

        });  // end each loop
        
        /* Popper for brieflink */
        $('li.brieflinkpopper').each( function(){
            var $this = $( this );
            var popper = $this.find('a.brieflinkpopperctrl') ;
            var popnode = $this.find('div.brieflinkpop');
            var popid = popnode.attr('id') || $.ui.jig._generateId('brieflinkpop');
            popnode.attr('id', popid);
            popper.ncbipopper({
                destSelector: "#" + popid,
                destPosition: 'top right', 
                triggerPosition: 'middle left', 
                hasArrow: true, 
                arrowDirection: 'right',
                isTriggerElementCloseClick: false,
                adjustFit: 'none',
                openAnimation: 'none',
                closeAnimation: 'none',
                delayTimeout : 130
            });
        }); // end each loop
    }//initPageSections
    
    //$(document).live("disco.ajax",function(){console.log("disco add loaded successfully");});
    
    });//document.ready()
})(jQuery);


;
Portal.Portlet.EmailTab = Portal.Portlet.extend({

	init: function(path, name, notifier) {
		this.base(path, name, notifier);
	},
	
	listen: {
		
		/* browser events */
		
		'SendMail': function(sMessage, oData, sSrc) {
			this.setValue('EmailReport', oData.report);
			this.setValue('EmailFormat', oData.format);
			this.setValue('EmailCount', oData.count);
			this.setValue('EmailStart', oData.start);
			this.setValue('EmailSort', oData.sort);
			this.setValue('Email', oData.email);
			this.setValue('EmailSubject', oData.subject);
			this.setValue('EmailText', oData.text);
            this.setValue('EmailQueryKey', oData.querykey);
			this.setValue('QueryDescription', oData.querydesc);
			this.setValue('Key', oData.captchakey);
			this.setValue('Answer', oData.captchaanswer);
		}

	}
});
;
Portal.Portlet.DbConnector = Portal.Portlet.extend({

	init: function(path, name, notifier) {
		var oThis = this;
		console.info("Created DbConnector");
		this.base(path, name, notifier);
		
		// reset Db value to original value on page load. Since LastDb is the same value as Db on page load and LastDb is not changed on
		// the client, this value can be used to reset Db. This is a fix for back button use.
		if (this.getValue("Db") != this.getValue("LastDb")){
		    this.setValue("Db", this.getValue("LastDb"));
		}
     
		// the SelectedIdList and id count from previous iteration (use a different attribute from IdsFromResult to prevent back button issues)
		Portal.Portlet.DbConnector.originalIdList = this.getValue("LastIdsFromResult");
		console.info("originalIdList " + Portal.Portlet.DbConnector.originalIdList);
		// if there is an IdList from last iteration set the count
		if (Portal.Portlet.DbConnector.originalIdList != ''){
			Portal.Portlet.DbConnector.originalCount = Portal.Portlet.DbConnector.originalIdList.split(/,/).length;
		}

		notifier.setListener(this, 'HistoryCmd', 
        	function(oListener, custom_data, sMessage, oNotifierObj) {
           		var sbTabCmd = $N(oThis.path + '.TabCmd');
           		sbTabCmd[0].value = custom_data.tab;
        	}
    		, null);
    
	},

	send: {
   		'SelectedItemCountChanged': null,
   		'newUidSelectionList': null,
   		'SavedSelectedItemCount': null,
   		'SavedUidList': null
	},

	listen: {
	
		//message from Display bar on Presentation change 
		'PresentationChange' : function(sMessage, oData, sSrc){
			
			// set link information only if it exists
			if (oData.dbfrom){
				console.info("Inside PresentationChange in DbConnector: " + oData.readablename);
				this.setValue("Db", oData.dbto);
				this.setValue("LinkSrcDb", oData.dbfrom);
				this.setValue("LinkName", oData.linkname);
				this.setValue("LinkReadableName", oData.readablename);
			}
			//document.forms[0].submit();
		},
		
		// various commands associated with clicking different form control elements
		'Cmd' : function(sMessage, oData, sSrc){
			console.info("Inside Cmd in DbConnector: " + oData.cmd);
			this.setValue("Cmd", oData.cmd);
			
			// back button fix, clear TabCmd
			if (oData.cmd == 'Go' || oData.cmd == 'PageChanged' || oData.cmd == 'FilterChanged' || 
			oData.cmd == 'DisplayChanged' || oData.cmd == 'HistorySearch' || oData.cmd == 'Text' || 
			oData.cmd == 'File' || oData.cmd == 'Printer' || oData.cmd == 'Order' || 
			oData.cmd == 'Add to Clipboard' || oData.cmd == 'Remove from Clipboard' || 
			oData.cmd.toLowerCase().match('details')){
				this.setValue("TabCmd", '');
				console.info("Inside Cmd in DbConnector, reset TabCmd: " + this.getValue('TabCmd'));
			}

		},
		
		
		// the term to be shown in the search bar, and used from searching
		'Term' : function(sMessage, oData, sSrc){
			console.info("Inside Term in DbConnector: " + oData.term);
			this.setValue("Term", oData.term);
		},
		
		
		// to indicate the Command Tab to be in
		'TabCmd' : function(sMessage, oData, sSrc){
			console.info("Inside TABCMD in DbConnector: " + oData.tab);
			this.setValue("TabCmd", oData.tab);
			console.info("DbConnector TabCmd: " + this.getValue("TabCmd"));
		},
		
		
		// message sent from SearchBar when db is changed while in a Command Tab
		'DbChanged' : function(sMessage, oData, sSrc){
			console.info("Inside DbChanged in DbConnector");
			this.setValue("Db", oData.db);
		},
		
		// Handles item select/deselect events
		// Argument is { 'id': item-id, 'selected': true or false }
		'ItemSelectionChanged' : function(sMessage, oData, oSrc) {
			var sSelection = this.getValue("IdsFromResult");
			var bAlreadySelected = (new RegExp("\\b" + oData.id + "\\b").exec(sSelection) != null);
	       	var count =0;
	       	
			if (oData.selected && !bAlreadySelected) {
				sSelection += ((sSelection > "") ? "," : "") + oData.id;
			   	this.setValue("IdsFromResult", sSelection);
			   	if (sSelection.length > 0){
			   		count = sSelection.split(',').length;
			   	}
			   	this.send.SelectedItemCountChanged({'count': count});
			   	this.send.newUidSelectionList({'list': sSelection});
			   	jQuery(document).trigger("itemsel",{'list': sSelection});
		   	} else if (!oData.selected && bAlreadySelected) {
				sSelection = sSelection.replace(new RegExp("^"+oData.id+"\\b,?|,?\\b"+oData.id+"\\b"), '');
		   	   	this.setValue("IdsFromResult", sSelection);
				console.info("Message ItemSelectionChanged - IdsFromResult after change:  " + this.getValue("IdsFromResult"));
			   	if (sSelection.length > 0){
			   		count = sSelection.split(',').length;
			   	}
				console.info("Message ItemSelectionChanged - IdsFromResult length:  " + count);   
				this.send.SelectedItemCountChanged({'count': count});
			   	this.send.newUidSelectionList({'list': sSelection});
			   	jQuery(document).trigger("itemsel",{'list': sSelection});
		   	}
		},
				
		// FIXME: This is the "old message" that is being phased out.
		// when result citations are selected, the list of selected ids are intercepted here,
		// and notification sent that selected item count has changed.
		'newSelection' : function(sMessage, oData, sSrc){
		
			// Check if we already have such IDs in the list
			var newList = new Array();
			var haveNow = new Array();
			if(Portal.Portlet.DbConnector.originalIdList){
				haveNow = Portal.Portlet.DbConnector.originalIdList.split(',');
				newList = haveNow;
			}
			
			var cameNew = new Array();
			if (oData.selectionList.length > 0) {
				cameNew = oData.selectionList;
			}
			
			if (cameNew.length > 0) {
				for(var ind=0;ind<cameNew.length;ind++) {
					var found = 0;
					for(var i=0;i<haveNow.length;i++) {
						if (cameNew[ind] == haveNow[i]) {
							found = 1;
							break;
						}
					}
						//Add this ID if it is not in the list
					if (found == 0) {
						newList.push(cameNew[ind]);
					}
				}
			}
			else {
				newList = haveNow;
			}

				// if there was an IdList from last iteration add new values to old
			var count = 0;
			if ((newList.length > 0) && (newList[0].length > 0)){
				count = newList.length;
			}
			
			console.info("id count = " + count);
			this.setValue("IdsFromResult", newList.join(","));
			
			this.send.SelectedItemCountChanged({'count': count});
			this.send.newUidSelectionList({'list': newList.join(",")});
			jQuery(document).trigger("itemsel",{'list': newList.join(",")});
		},


		// empty local idlist when list was being collected for other purposes.
		//used by Mesh and Journals (empty UidList should not be distributed, otherwise Journals breaks)
		// now used by all reports for remove from clipboard function.
		'ClearIdList' : function(sMessage, oData, sSrc){
			this.setValue("IdsFromResult", '');
			this.send.SelectedItemCountChanged({'count': '0'});
			this.send.newUidSelectionList({'list': ''});
			jQuery(document).trigger("itemsel",{'list': ""});
		}, 


		// back button fix: when search backend click go or hot enter on term field,
		//it also sends db. this db should be same as dbconnector's db
		'SearchBarSearch' : function(sMessage, oData, sSrc){
			if (this.getValue("Db") != oData.db){
				this.setValue("Db", oData.db);
			}
		},
		
		// back button fix: whrn links is selected from DisplayBar,
		//ResultsSearchController sends the LastQueryKey from the results on the page
		// (should not be needed by Entrez 3 code)
		'LastQueryKey' : function(sMessage, oData, sSrc){
			if (this.getInput("LastQueryKey")){
				this.setValue("LastQueryKey", oData.qk);
			}
		},
		
		'QueryKey' : function(sMessage, oData, sSrc){
			if (this.getInput("QueryKey")){
				this.setValue("QueryKey", oData.qk);
			}
		},
		
		
		//ResultsSearchController asks for the initial item count in case of send to file 
		'needSavedSelectedItemCount' : function(sMessage, oData, sSrc){
			var count = 0;
			if(this.getInput("IdsFromResult")){
				if (this.getValue("IdsFromResult").length > 0){
					count = this.getValue("IdsFromResult").split(',').length;
				}
				console.info("sending SavedSelectedItemCount from IdsFromResult: " + count);
			}
			else{
				count = Portal.Portlet.DbConnector.originalCount;
				console.info("sending SavedSelectedItemCount from OriginalCount: " + count);
			}
			this.send.SavedSelectedItemCount({'count': count});
		},
		
		// Force form submit, optionally passing db, term and cmd parameters
		'ForceSubmit': function (sMessage, oData, sSrc)
		{
		    if (oData.db)
    			this.setValue("Db", oData.db);
		    if (oData.cmd)
    			this.setValue("Cmd", oData.cmd);
		    if (oData.term)
    			this.setValue("Term", oData.term);
    		Portal.requestSubmit ();
		},
		
		'LinkName': function (sMessage, oData, sSrc){
		    this.setValue("LinkName", oData.linkname);
		},
		
		'IdsFromResult': function (sMessage, oData, sSrc){
		    this.setValue("IdsFromResult", oData.IdsFromResult);
		},
		
		'SendSavedUidList': function (sMessage, oData, sSrc){
		    this.send.SavedUidList({'idlist': this.getValue("IdsFromResult")});
		}
		
	}, //listen
	
	/* other portlet functions */
	
	// DisplayBar in new design wants selected item count
	'SelectedItemCount': function(){
	    var count = 0;
		if(this.getInput("IdsFromResult")){
			if (this.getValue("IdsFromResult") != ''){
				count = this.getValue("IdsFromResult").split(',').length;
			}
		}
		else{
			count = Portal.Portlet.DbConnector.originalCount;
		}
		return count;
	},
	
	'SelectedItemList': function(){
		if(this.getInput("IdsFromResult") && this.getValue("IdsFromResult") != ''){
			return this.getValue("IdsFromResult");
		}
		else{
			return Portal.Portlet.DbConnector.originalIdList;
		}
		
	},
	setValue: function(name, value){
	    if(name == 'Term')
	        value = jQuery.trim(value);
	    this.base(name,value);
	}
},
{
	originalIdList: '',
	originalCount: 0
});

function getEntrezSelectedItemCount() {
    return $PN('DbConnector').SelectedItemCount();
}

function getEntrezSelectedItemList() {
    return $PN('DbConnector').SelectedItemList();
}
