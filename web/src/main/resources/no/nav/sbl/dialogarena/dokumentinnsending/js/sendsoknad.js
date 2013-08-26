	var soknadid, fnr, adresse, navn;

$(document).ready(function(){
	$.ajax("mustacheServicePage")
		.done(function(data,text,jqXHR) {
			soknadid = jqXHR.getResponseHeader("SoknadId");
			fnr = jqXHR.getResponseHeader("fnr");
			adresse = jqXHR.getResponseHeader("adresse");
			navn = jqXHR.getResponseHeader("navn");
			lastInnSoknad('html/Dagpenger.html','');
		});
	
});

function lastInnSoknad(urlTilSoknadsSkjema, templateData){
	var jsonData;
	if(templateData) {
		jsonData = $.parseJSON(templateData);
	}
	
	$.get(urlTilSoknadsSkjema, function(templates){
		var template = $(templates).filter("#tpl-greeting").html();
		$(".skjema").html(Mustache.render(template, jsonData));
		$("#soknadId").attr("value",soknadid);
		$("#fnr").html(fnr);
		$("#adresse").html(adresse);
		$("#navn").html(navn);
		
	});
}

