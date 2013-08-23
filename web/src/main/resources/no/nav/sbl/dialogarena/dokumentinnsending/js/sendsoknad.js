$(document).ready(function() {
	
});

function eksempel3(htmlside){
	var templateData = {
			navn: "Ketil"
	}
	
	$.get(htmlside, function(templates){
		var template = $(templates).filter("#tpl-greeting").html();
		$("body").html(Mustache.render(template, templateData));
	});
}
function eksempel1(){
	console.log("asdasd");
	var person = {
		firstname:"Ketil",
		lastname:"Velle"
	};

	var template = "<h1>{{firstname}} {{lastname}}</h1>";
	var html= Mustache.to_html(template,person);
	$("#sampleArea").html(html);
}