var soknadid, fnr, adresse, navn;

function lastInnSoknad(urlTilSoknadsSkjema, templateData, soknadId){
	var mustacheServiceUrl="sendSoknadService";
	var mustacheTemplate;
	if(soknadId) {
		mustacheTemplate = "#oppsumering";
		mustacheServiceUrl+="?soknadId="+soknadId;
	} else {
		mustacheTemplate = "#soknad";
	}
	$.ajax(mustacheServiceUrl)
	.done(function(data,text,jqXHR) {
		soknadid = jqXHR.getResponseHeader("soknadId");
		fnr = jqXHR.getResponseHeader("fnr");
		adresse = jqXHR.getResponseHeader("adresse");
		fornavn = jqXHR.getResponseHeader("fornavn");
		etternavn = jqXHR.getResponseHeader("etternavn");
		telefon = jqXHR.getResponseHeader("telefon");
		epost = jqXHR.getResponseHeader("epost");

		var jsonData;
		if(templateData) {
			jsonData = $.parseJSON(templateData);
		}
		
		$.get(urlTilSoknadsSkjema, function(templates){
			var template = $(templates).filter(mustacheTemplate).html();
			$(".skjema").html(Mustache.render(template, jsonData));
			$("#soknadId").attr("value",soknadid);
			$("#fnr").html(fnr);
			$("#adresse").html(adresse);
			$("#fornavn").html(fornavn);
			$("#etternavn").html(etternavn);
			$("#epost").html(epost);
			$("#telefon").html(telefon);
			
		});
	});
	
}

function getURLParameter(name) {
    return decodeURI(
        (RegExp(name + '=' + '(.+?)(&|$)').exec(location.search)||[,null])[1]
    );
}

