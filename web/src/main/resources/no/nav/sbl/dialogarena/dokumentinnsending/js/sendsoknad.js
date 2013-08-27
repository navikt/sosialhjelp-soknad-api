var soknadid, fnr, adresse, navn;

function lastInnSoknad(urlTilSoknadsSkjema, templateData, soknadId){
	var mustacheServiceUrl="sendSoknadService";
	var mustacheTemplate;
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
		soknadid = jqXHR.getResponseHeader("soknadId");
		fnr = jqXHR.getResponseHeader("fnr");
		adresse = jqXHR.getResponseHeader("adresse");
		fornavn = jqXHR.getResponseHeader("fornavn");
		etternavn = jqXHR.getResponseHeader("etternavn");
		barnealder = jqXHR.getResponseHeader("barnealder");
		sivilstatus = jqXHR.getResponseHeader("sivilstatus");
		postnr = jqXHR.getResponseHeader("postnr");
		poststed = jqXHR.getResponseHeader("poststed");
		barnenavn = jqXHR.getResponseHeader("barnenavn");
		forstegangstjeneste= jqXHR.getResponseHeader("forstegangstjeneste");
		fra= jqXHR.getResponseHeader("fra");
		til = jqXHR.getResponseHeader("til");
		lestbrosjyre = jqXHR.getResponseHeader("lestbrosjyre");

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
    return decodeURI(
        (RegExp(name + '=' + '(.+?)(&|$)').exec(location.search)||[,null])[1]
    );
}

