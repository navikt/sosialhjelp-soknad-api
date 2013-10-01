angular.module('sendsoknad',['app.routes','app.brukerdata','app.services','app.directives','app.controllers', 'app.date', 'app.grunnlagsdata', 'ngMockE2E'])

.run(function($httpBackend) {
	soknadData = {"soknadId":1,"gosysId":"Dagpenger","brukerBehandlingId":"100000000",
	"fakta":{
		"fornavn":{"soknadId":1,"key":"fornavn","value":"Kari"},
		"mellomnavn":{"soknadId":1,"key":"mellomnavn","value":"Johan"},
		"etternavn":{"soknadId":1,"key":"etternavn","value":"Nordmann"},
		"fnr":{"soknadId":1,"key":"fnr","value":"***REMOVED***"},
		"bostedsadresseLandkode":{"soknadId":1,"key":"landkode","value":"NOR"},
		"bostedsadressePoststed":{"soknadId":1,"key":"poststed","value":"Oslo"},
		"bostedsadressePostnr":{"soknadId":1,"key":"postnr","value":"0175"},
		"bostedsadresseGatenavn":{"soknadId":1,"key":"gatenavn","value":"Waldemar Thranes Gt."},
		"bostedsadresseHusnummer":{"soknadId":1,"key":"postnr","value":"98"},
		"midlertidigadresseLandkode":{"soknadId":1,"key":"landkode","value":"NOR"},
		"midlertidigadressePoststed":{"soknadId":1,"key":"poststed","value":"Oslo"},
		"midlertidigadressePostnr":{"soknadId":1,"key":"postnr","value":"0175"},
		"midlertidigadresseGatenavn":{"soknadId":1,"key":"gatenavn","value":"Waldemar Thranes Gt."},
		"midlertidigadresseHusnummer":{"soknadId":1,"key":"postnr","value":"98"},
	}
};

pensjonist = {"fornavn":"Ola","etternavn":"Nordmann","mellomnavn":"J","alder":50,"bostedsadresseLandkode":"NOR","bostedsadressePoststed":"Oslo", "bostedsadressePostnr":"0175", "bostedsadresseGatenavn":"Testveien", "bostedsadresseHusnummer":"1", "midlertidigadresseLandkode":"NOR","midlertidigadressePoststed":"Oslo", "midlertidigadressePostnr":"0175", "midlertidigadresseGatenavn":"Testveien", "midlertidigadresseHusnummer":"1", "email":"ola@nordmann.no"};
arbeidssoker = {"fornavn":"Test","etternavn":"Testesen","mellomnavn":"","alder":31,"postnummer":"5001","poststed":"Bergen","adresse":"Kalfaret","email":"test@testesen.no"};

$httpBackend.whenGET('/sendsoknad/rest/grunnlagsdata').respond(pensjonist);	
$httpBackend.whenGET('/sendsoknad/rest/grunnlagsdata/alder').respond({"alder": 67});	


$httpBackend.whenGET('/sendsoknad/rest/soknad').respond(soknadData);
	//$httpBackend.whenGET('/sendsoknad/rest/soknad').passThrough();
	$httpBackend.whenPOST('/sendsoknad/rest/soknad/1').respond(function(method, url, data) {
		soknadData.fakta.push(angular.fromJson(data));
		console.log("SoknadData " + soknadData);
		//soknadData.push(angular.fromJson(data));
	});
	//$httpBackend.whenPOST('/sendsoknad/rest/soknad').passThrough();

	//$httpBackend.whenGET('/utslagskriterier').passThrough();
	$httpBackend.whenGET(/html\/.*/).passThrough();
})