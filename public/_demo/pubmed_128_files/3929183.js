(function ($){
    $(function (){
        var selectedIds = [];
        var selTitleExists = false;
        var RecWithNoTitle = false;
        var jIdsFromResult = $("#IdsFromResult");
        var jTA = $("#PubmedSearchItems");
        var currDb = jTA.data("db");
        var ajaxCall;
        $("#PSI_AddToSearchBuilder").on("click",function(e){
            e.preventDefault();
            e.stopPropagation();
            addToSearchBuilder();
        });
        $("#PSI_SearchPubmed").on("click",function(e){
            e.preventDefault();
            e.stopPropagation();
            var taVal = $.trim(jTA.val());
            if (taVal == "")
                showAlert("Select an item to send to the Search Box");
            else {
                jTA.val("");
                setPubmedSearchItmes("");
                ajaxCall.always(function(){
                    window.location="/pubmed?term=" + encodeURIComponent(taVal).replace(/%20/g,'+');;
                });
            }
        });
        $(document).on("itemsel",function(e,d){
            selectedIds=d.list.split(',');
        });
        
        function addToSearchBuilder(){
            clearAlert();
    	    if (selectedIds.length==0){
        	    var sSelection = jIdsFromResult.val()? jIdsFromResult.val() :'';
            	if ('' + sSelection != '')
        	   	    selectedIds=sSelection.split(',');
        	   	else
        	   	    selectedIds = [];
    	   	}
    	    if(selectedIds.length==0){
    	        showAlert("Select an item to send to the search builder");
    	        return ;
    	    }
    	    var ajaxUrl = '/' + currDb + '/?p$l=AjaxServer';
    	    if (currDb == 'mesh')
                ajaxUrl = ajaxUrl + '&p$rq=AjaxServer.DiscoAdC.DiscoSectionCol.Mesh_PubmedSearchBuilderHelper:GetPubMedSearchTitles&IdsListIn=' + 
                    selectedIds.join(',') + '&Db=' + currDb ;
            else if (currDb == 'nlmcatalog')
                ajaxUrl = ajaxUrl + '&p$rq=AjaxServer.DiscoAdC.DiscoSectionCol.Nlmcat_PubmedSearchBuilderHelper:GetPubMedSearchTitles&IdsListIn=' + 
                    selectedIds.join(',') + '&Db=' + currDb ;
            var ajaxCall = $.ajax({
                url:ajaxUrl,
                timeout:10000,
                dataType:'html'
            });
            ajaxCall.done( function(htmlData){
                try{
                    var resp = eval('(' + htmlData + ')');
                    var taCurrVal = jQuery.trim(jTA.val());
                    selTitleExists = false;
                    RecWithNoTitle = resp.RecWithNoTitle;
                    var taVal = buildTitlesQuery(resp.TitlesList,resp.TitlesDelimiter,taCurrVal);
                    
                   
               }catch(e){
                   console.warn("Error in fetching pubmed titles for PubMed Search Builder - " + e);
               }
            });
            ajaxCall.fail( function(data){
                console.error("Error in fetching pubmed titles for PubMed Search Builder - " + data);
            });
        }
        
        jTA.on("titlesBuilt",function(e,v){
            
            setNewPubmedSearchItems(v.ntaVal,jQuery.trim(jTA.val()));
            if (v.selTitleExists === true)
                selTitleExists = v.selTitleExists;
                    
            if ((v.ntaVal == '' && !selTitleExists ) || RecWithNoTitle == 'true' ){
                showAlert(getGenericErrorMessage());
                return;
            }
        });
        
        //do it the easy way for now
        function getGenericErrorMessage(){
            if (currDb == "mesh")
                return "Errors occurred. PubMed search builder can not retrieve citations. Please try again."
            else if (currDb == "nlmcatalog")
                return "PubMed search builder only retrieves citations for PubMed journals";
        }
        
        function clearAlert(){
            $("#pubmed_searchbar_alert").hide();
        }
        
        function showAlert(msg){
            $("#pubmed_searchbar_alert").html(msg).show();
        }
        
        /* functionality to be implemented by the sub classes, so send a message */
    	function buildTitlesQuery(titlesString,delimiter,taCurrVal){
    	    jTA.trigger("buildTitles",{"titles":titlesString,"delim":delimiter,"taCurrVal":taCurrVal,"db":currDb});
    	}
    	
    	function setNewPubmedSearchItems(taVal,taCurrVal){
            var newVal = "";
            var boolOp = $('#PSI_opCombo').val();
            boolOp = boolOp ? boolOp : "OR";
            if (taCurrVal != "")
                newVal = (taVal !== '') ? "(" + taCurrVal + ") " + boolOp  + " " + taVal : taCurrVal;
            else
                newVal = taVal;
             
            jTA.val(newVal);
            setPubmedSearchItmes(newVal);
        }
        
        function setPubmedSearchItmes(titlesSearchStr){
    	   //do an ajax to set the session attr of selected titles
    	    var ajaxUrl = '/' + currDb + '/?p$l=AjaxServer'; 
            ajaxUrl = ajaxUrl + '&p$rq=AjaxServer.DiscoAdC.DiscoSectionCol.Mesh_PubmedSearchBuilderHelper:SetSelectedTitles&PubmedSearchItemsNew=' + 
                titlesSearchStr ;
                
            var ajaxUrl = '/' + currDb + '/?p$l=AjaxServer';
    	    if (currDb == 'mesh')
                ajaxUrl = ajaxUrl + '&p$rq=AjaxServer.DiscoAdC.DiscoSectionCol.Mesh_PubmedSearchBuilderHelper:SetSelectedTitles&PubmedSearchItemsNew=' + 
                    titlesSearchStr ;
            else if (currDb == 'nlmcatalog')
                ajaxUrl = ajaxUrl + '&p$rq=AjaxServer.DiscoAdC.DiscoSectionCol.Nlmcat_PubmedSearchBuilderHelper:SetSelectedTitles&PubmedSearchItemsNew=' + 
                    titlesSearchStr ;
                
            ajaxCall = $.ajax({
                url:ajaxUrl,
                timeout:10000,
                dataType:'html'
            });
    	}//setPubmedSearchItmes
        
    });//end of DOM ready
})(jQuery);
;
(function($){
    $(function(){
        var pmcommonsad = $('#pm_commons_ad');
        ncbi.sg.scanLinks(pmcommonsad.get());
        $.ncbi.authorpreview.scanAuthorLinks(pmcommonsad);
    });
})(jQuery);


;
(function( $ ){ // pass in $ to self exec anon fn
    var post_url = '/myncbi/session-state/';

    // on page ready
    $( function() {
    
        $('div.portlet, div.section').each( function() {

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
                
                $.post(post_url, { section_name: anchor.attr('pgsec_name'), new_section_state: 'true' });
            });

            anchor.bind('ncbitoggleropen', function() {
                anchor.removeClass('shutter_closed');
                $.post(post_url, { section_name: anchor.attr('pgsec_name'), new_section_state: 'false' });
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
    });// end on page ready
})( jQuery );
;
(function ($){
     
    $(function (){
         //initFavorites();
         window.setTimeout(function(){initFavorites();},10);   
    })//end of DOM ready
    
    function initFavorites(){
        
        /*JSL-1460: Temporary fix before JIG 1.12 is out*/
        $('#pubmed_favoritesad .ui-ncbisetswitch').addClass('ltd-hover');
        
        //Check if article is favorite
        checkFavStatus();
        
        //When drop-down arrow is clicked
        jQuery("#pubmed_favoritesad .ui-ncbisetswitch-button").live('click',function(){
            //console.log('fav down arrow clicked.');
            var link = this;
            jQuery.ui.jig.requiresLoginURL = "/account/signin/?inlinelogin=true&p$debugoutput=off";
            jQuery.ui.jig.requiresLogin( function(name, requiredLogin ){ 
                LoginCallBack(name, requiredLogin);
                
                //Fetch list of collections
                if(jQuery("#pubmed_favoritesad").hasClass('empty')){
                    try{
                        checkFavStatus();
                        jQuery("div.colloading").show();
                        fetchFavContent(link);
                    }catch(err){
                        console.log(err);
                    }
                }
            });
            
        });
        
        //Event handler for "create collection" link, using "send to" menu
        jQuery("#pubmed_favoritesad .ui-ncbisetswitch-create-collection").on('click', function(){
            jQuery("#dest_AddToCollections").click();
            jQuery("#submenu_AddToCollections .button_apply").click();
        });
        
        //Event handler for click on Favorite/Collection
        jQuery(".collink").on('click', function(event){
            toggleItem(event);
        });
        
        //Event handler repeated - why?
        //for the first click is missed otherwise
        jQuery(".collink").live('click', function(event){
            toggleItem(event);
        });
    }//end initFavorites


    function getAjaxUrl(actionName,IdsFromResult){
        var ajaxUrl = '/pubmed/?p$l=AjaxServer';
        ajaxUrl = ajaxUrl + '&p$rq=AjaxServer.DiscoAdC.DiscoSectionCol.Pubmed_FavoritesAdHelper:' + actionName + '&Db=pubmed';
        return ajaxUrl + (IdsFromResult ? '&IdsFromResult=' + IdsFromResult : '');
    }
    
    function makeAjaxCall(url,data,async){
        async = (typeof async == 'undefined') ? true : async;
        return $.ajax({
            url:url,
            timeout:10000,
            type:'POST',
            dataType:'html',
            async:async,
            data:data
        });
    }
    
    function checkFavStatus(){
        if(($("#myncbiusername").text() != '') && ($("#favList").hasClass('blind'))){
            var ajaxUrl = getAjaxUrl('FetchFav_XHR',$("#absid").val());
            var ajaxCall = makeAjaxCall(ajaxUrl,{},false);
           ajaxCall.done( function(data){
                try{
                    fetchFavResponder(eval('(' + data + ')'));
               }catch(e){
                   console.log(e);
               }
            });//end ajaxCall.done
        }
    }//end chechFavStatus
    
    function fetchFavResponder(JSONobj){
        if((JSONobj.mid == '') || (JSONobj.mid == '0')){
            return; 
        }
        
        if((($("#favList").attr('colid')=== undefined) || ($("#favList").attr('colid') == ''))
        && JSONobj.favid != ''){
            jQuery("#favList").attr('colid', JSONobj.favid);
        }
        
        if((JSONobj.status != undefined) && (JSONobj.status != '')){
            if(JSONobj.status == 'present'){
                jQuery('#favList').html(jQuery('#favList').html().replace('Add to Favorites', 'Favorite'));
                jQuery('#favList span.star').addClass('active');
            }else{
                if(jQuery('#favList').text().indexOf('Add to Favorites') == -1)
                    jQuery('#favList').html(jQuery('#favList').html().replace('Favorite', 'Add to Favorites'));
                jQuery('#favList span.star').removeClass('active');
            }
            jQuery("#favList").removeClass('blind');
        }    
    }//end fetchFavResponder
    
    //callback function for JIG login
    function LoginCallBack(name,requiredLogin){
    }
    
    function fetchFavContent(link){
        var ajaxUrl = getAjaxUrl('Favorites_FetchContent_XHR',$("#absid").val());
        
        function ajaxDoneC(_link){
            return function (data){
                try{
                    fetchFavContentResponder(eval('(' + data + ')'),_link);
               }catch(e){
                   console.log(e);
               }
            }
        }
       
        var ajaxCall = makeAjaxCall(ajaxUrl);
       ajaxCall.done(
          ajaxDoneC(link)
        );//end ajaxCall.done
        
        ajaxCall.always(function(){
           $("div.colloading").hide(); 
        });
        
    }//end fetchFavContent
    
    function fetchFavContentResponder(JSONobj,link){
    try{
        if((JSONobj.mid == '') || (JSONobj.mid == '0')){
	            //alert("Looks like you are not logged in. \nPlease refresh the page and try again.");
	            loginagain();
	            jQuery("div.colloading").hide();
	            jQuery(link).click();
	            return; 
	        }
	        if(JSONobj.content && JSONobj.content != ''){
                jQuery("#favUL").prepend(JSONobj.content, true);
                jQuery("#pubmed_favoritesad").removeClass('empty');
		    }
		    jQuery("div.colloading").hide();
		    
		    //For favorites collection
	        if((JSONobj.status != undefined) && (JSONobj.status != '')){
	            if(JSONobj.status == 'present'){
	                jQuery('#favList').html(jQuery('#favList').html().replace('Add to Favorites', 'Favorite'));
    	            jQuery('#favList span.star').addClass('active');
    	            jQuery('#favUL li.favorite').addClass('starred');
    	        }else if(JSONobj.status == 'absent'){
    	            if(jQuery('#favList').text().indexOf('Add to Favorites') == -1)
    	                jQuery('#favList').html(jQuery('#favList').html().replace('Favorite', 'Add to Favorites'));
    	            jQuery('#favList span.star').removeClass('active');
    	            jQuery('#favUL li.favorite').removeClass('starred');
    	        }
    	        jQuery("#favList").removeClass('blind');
	        }
	        
	        //JSL-1762 - Add pinger click event for Favorites pull-down
            function starredSet() { 
                var link = jQuery(this); 
                var isBeingSelected = !link.parent().hasClass("starred");
                link.attr("ref", "star=" + isBeingSelected );
            } 
            jQuery("#favUL a").click(starredSet); 
            
	        var theLinks = jQuery("#favUL")[0].getElementsByTagName("a");
            if(typeof ncbi !== "undefined" && typeof ncbi.sg  !== "undefined" && typeof ncbi.sg.scanLinks !== "undefined" ){
                 ncbi.sg.scanLinks( Array.prototype.slice.call(theLinks,0));
            } 
            
        }catch(error){
            console.log(error);
            jQuery("div.colloading").hide();
	    } 
    }//end fetchFavContentResponder
    
    function loginagain(){
        jQuery("#myncbiusername").hide().find("#mnu").text(''); 
	    jQuery("#sign_in").show();
	    jQuery("#sign_out").hide();
    }
    
    function toggleItem(event){
        
        event.preventDefault();
        //console.log('toggleItem');
        var link = $(event.target);
        var colname = link.text();
        var colid = link.attr('colid');
        if(colid == '') 
            colid = jQuery("#favList").attr('colid');
        jQuery.ui.jig.requiresLoginURL = "/account/signin/?inlinelogin=true&p$debugoutput=off";
        jQuery.ui.jig.requiresLogin( function(name, requiredLogin ){ 
	        LoginCallBack(name, requiredLogin);
	        
            //Fetch list of collections
            checkFavStatus();
            try{
                var ajaxUrl = getAjaxUrl('Toggle_XHR');
                
                var firstAuthor = jQuery("#maincontent .abstract .auths a")[0];
                var title = jQuery("#maincontent .abstract h1")[0];
                
                var ajaxDoneC = (function (_link){
                    return function (data){
                        try{
                            toggleResponder(eval('(' + data + ')'),_link);
                       }catch(e){
                           console.log(e);
                       }
                    }
                })(link);
                
                var ajaxCall = makeAjaxCall(ajaxUrl,{
                        'IdsFromResult' : jQuery("#absid").val(),
                        'Title': jQuery(title).text(),
                        'Author': jQuery(firstAuthor).text(),
                        'Date': jQuery("#absdate").val(),
                        'ColId': colid
                    });
               ajaxCall.done(
                  ajaxDoneC
                );//end ajaxCall.done
                
                ajaxCall.fail(function(){
                    ajaxFailC(link)
                });
                function ajaxFailC(_link){
                    return function(){
                        _link.addClass('collink');
                    }
                }
            }catch(err){
                console.log(err);
                link.addClass('collink');
            }
	    });
        
    }
    
    function toggleResponder(JSONobj, link){
	     try{
	        if((JSONobj.mid == '') || (JSONobj.mid == '0')){
	            //alert("Looks like you are not logged in. \nPlease refresh the page and try again.");
	            link.addClass('collink');
	            loginagain();
	            link.click();
	            return; 
	        }
	        
	        //For favorites collection
	        if((JSONobj.status != undefined) && (JSONobj.status != '') && 
	          (link.attr('colid') == jQuery("#favList").attr('colid'))){
	            if(JSONobj.status == 'added'){
	                jQuery('#favList').html(jQuery('#favList').html().replace('Add to Favorites', 'Favorite'));
    	            jQuery('#favList span.star').addClass('active');
    	            jQuery('#favUL li.favorite').addClass('starred');
    	        }else if(JSONobj.status == 'removed'){
	                if(jQuery('#favList').text().indexOf('Add to Favorites') == -1)
    	                jQuery('#favList').html(jQuery('#favList').html().replace('Favorite', 'Add to Favorites'));
    	            jQuery('#favList span.star').removeClass('active');
    	            jQuery('#favUL li.favorite').removeClass('starred');
    	        }
    	        jQuery("#favList").removeClass('blind');
	        }
	        //For other collections
	        else if (JSONobj.status != ''){
	            if(JSONobj.status == 'added')
    	            link.parent().addClass('starred');
    	        else if(JSONobj.status == 'removed')
    	            link.parent().removeClass('starred');
	        } 
	        //if no status is returned (smth's wrong)
	        else {
	            //console.log("no status returned");
	            //set the star back to original status
	            //if(uargs.colname == 'Favorites'){
	            if(link.text() == 'Favorites'){
    	            if(jQuery('#favList span.star').hasClass('active')){
    	                if(jQuery('#favList').text().indexOf('Add to Favorites') == -1)
        	                jQuery('#favList').html(jQuery('#favList').html().replace('Favorite', 'Add to Favorites'));
    	                jQuery('#favList span.star').removeClass('active');
    	            }else{
    	                jQuery('#favList').html(jQuery('#favList').html().replace('Add to Favorites', 'Favorite'));
    	                jQuery('#favList span.star').addClass('active')
    	            }
    	        }
	        }
		    link.addClass('collink');
	    }catch(error){
	        console.log(error);
	        link.addClass('collink');
	    }
	}

    
})(jQuery);




;
(function( $ ){ // pass in $ to self exec anon fn
    // on page ready
    $( function() {
    
        // Initialize popper
        $('li.ralinkpopper').each( function(){
            var $this = $( this );
            var popper = $this;
            var popnode = $this.find('div.ralinkpop');
            var popid = popnode.attr('id') || $.ui.jig._generateId('ralinkpop');
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
        
    });// end on page ready

})( jQuery );


function historyDisplayState(cmd)
{
    var post_url = '/myncbi/session-state/';

    if (cmd == 'ClearHT')
    {
        if (!confirm('Are you sure you want to delete all your saved Recent Activity?'))
        {
            return;
        }
    }

    var ajax_request = jQuery.post(post_url, { history_display_state: cmd })
        .complete(function(jqXHR, textStatus) {    
        
            var htdisplay = jQuery('#HTDisplay');
            var ul = jQuery('#activity');

            if (cmd == 'HTOn') 
            { 
                // so that the following msg will show up
                htdisplay.removeClass();
                
                if (jqXHR.status == 408) 
                { 
                    htdisplay.html("<p class='HTOn'>Your browsing activity is temporarily unavailable.</p>");
                    return;
                }
                
                if (htdisplay.find('#activity li').length > 0)
                {
                    ul.removeClass('hide');    
                }
                else
                {
                    htdisplay.addClass('HTOn');
                }
                
            }         
            else if (cmd == 'HTOff') 
            {                         
                ul.addClass('hide'); 
                htdisplay.removeClass().addClass('HTOff');    // make "Activity recording is turned off." and the turnOn link show up             
            }
            else if (cmd == 'ClearHT') 
            { 
                if (htdisplay.attr('class') == '') 
                {                 
                    htdisplay.addClass('HTOn');  // show "Your browsing activity is empty." message                                  

                    ul.removeClass().addClass('hide'); 
                    ul.html('');
                }
            } 
        });

}


;
(function($){
    $(function(){
        $("#relevancead_sort").live("click", SendSortRequest);
        
        function SendSortRequest(e){
            e.preventDefault();
            e.stopPropagation();
            var sort = $("#relevancead_sort").attr('href') == '#relevance' ? '[relevance]' : '';
            Portal.$send('SubmitSort',{'sort': sort});
        }
    });//end of $(function(){
    
})(jQuery);

