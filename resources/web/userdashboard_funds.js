
$(document).ready(function(){


	sessionId = getCookie("authenticated_session_id");
	console.log(sessionId);
    
	fillUserHighChartStandardTemplate('get_users_funds_accum', '#users_funds', 'Funds ($)', '$');
	fillTableFromMustache('get_users_transactions', '#transactions_template', '#transactions');
});

	function fillTableFromMustache(url, templateId, divId) {
       var url = "http://localhost:4567/" + sessionId + "/" + url// the script where you handle the form input.
       $.ajax({
       	type: "GET",
       	url: url,
       	xhrFields: {
       		withCredentials: true
       	},
      // data: seriesData, 
      success: function(data, status, xhr) {
      	console.log(data);
            // var jsonObj = JSON.parse(data);
            // JSON.useDateParser();
            // var jsonObj = jQuery.parseJSON(data);
            JSON.useDateParser();
            var jsonObj = JSON.parse(data);
            
            var template = $(templateId).html();
            Mustache.parse(template);   // optional, speeds up future uses
            var rendered = Mustache.render(template,jsonObj);
            $(divId).html(rendered);
			$("#transactions_table").tablesorter({debug: true}); 
            console.log(jsonObj);              
            console.log(template);
            console.log(rendered);
            

        },
        error: function (request, status, error) {

        	toastr.error(request.responseText);
        }
    });

   }


