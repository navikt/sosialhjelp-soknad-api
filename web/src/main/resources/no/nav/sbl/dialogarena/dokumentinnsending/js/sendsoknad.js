var soknadid, fnr, adresse, navn;

function lastInnSoknad(urlTilSoknadsSkjema, templateData, soknadId){
	var mustacheServiceUrl="sendSoknadService";
	var mustacheTemplate;
	var formId;
	if(soknadId) {
		formId = "#oppsumeringsform";
		mustacheTemplate = "#oppsumering";
		mustacheServiceUrl+="?soknadId="+soknadId;
	} else {
		mustacheTemplate = "#soknad";
		formId = "#sendsoknadform";
	}
	$.ajax(mustacheServiceUrl)
	.done(function(data,text,jqXHR) {
		var soknadid = jqXHR.getResponseHeader("soknadId");
		var fnr = jqXHR.getResponseHeader("fnr");
		var adresse = jqXHR.getResponseHeader("adresse");
		var fornavn = jqXHR.getResponseHeader("fornavn");
		var etternavn = jqXHR.getResponseHeader("etternavn");
		var barnealder = jqXHR.getResponseHeader("barnealder");
		var sivilstatus = jqXHR.getResponseHeader("sivilstatus");
		var postnr = jqXHR.getResponseHeader("postnr");
		var poststed = jqXHR.getResponseHeader("poststed");
		var barnenavn = jqXHR.getResponseHeader("barnenavn");
		var forstegangstjeneste= jqXHR.getResponseHeader("forstegangstjeneste");
		var fra= jqXHR.getResponseHeader("fra");
		var til = jqXHR.getResponseHeader("til");
		var lestbrosjyre = jqXHR.getResponseHeader("lestbrosjyre");

		var jsonData;
		if(templateData) {
			jsonData = $.parseJSON(templateData);
		}
		
		$.get(urlTilSoknadsSkjema, function(templates){
			var template = $(templates).filter(mustacheTemplate).html();
			$(formId).html(Mustache.render(template, jsonData));
			$("#soknadId").attr("value",soknadid);
			$("#fnr").html(fnr);
			$("#adresse").html(adresse);
			$("#fornavn").html(fornavn);
			$("#etternavn").html(etternavn);
			$("#sivilstatus").html(sivilstatus);
			$("#barnenavn").html(barnenavn);
			$("#postnr").html(postnr);
			$("#poststed").html(poststed);
			$("#barnealder").html(barnealder);
			$("#forstegangstjeneste").html(forstegangstjeneste);
			$("#fra").html(fra);
			$("#til").html(til);
			$("#lestbrosjyre").html(lestbrosjyre);
			
		});
	});
	
}

function getURLParameter(name) {
    return decodeURI((RegExp(name + '=' + '(.+?)(&|$)').exec(location.search)||[,null])[1]);
}