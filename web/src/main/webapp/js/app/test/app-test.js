angular.module('sendsoknad',['app.routes','app.brukerdata','app.services','app.directives','app.controllers', 'app.date', 'app.grunnlagsdata', 'ngMockE2E'])

.run(function($httpBackend) {
	soknadData = {"soknadId":1,"gosysId":"Dagpenger","brukerBehandlingId":"100000000",
					"fakta":{
						"fornavn":{"soknadId":1,"key":"fornavn","value":"Kari"},
						"mellomnavn":{"soknadId":1,"key":"mellomnavn","value":"Johan"},
						"etternavn":{"soknadId":1,"key":"etternavn","value":"Nordmann"},
						"fnr":{"soknadId":1,"key":"fnr","value":"01015245464"},
						"adresse":{"soknadId":1,"key":"adresse","value":"Waldemar Thranes Gt. 98B"},
						"postnr":{"soknadId":1,"key":"postnr","value":"0175"},
						"poststed":{"soknadId":1,"key":"poststed","value":"Oslo"}
					}
				};

	pensjonist = {"fornavn":"Ola","etternavn":"Nordmann","mellomnavn":"J","alder":71,"postnummer":"0123","poststed":"Oslo","adresse":"Testveien 1","email":"ola@nordmann.no"};
	arbeidssoker = {"fornavn":"Test","etternavn":"Testesen","mellomnavn":"","alder":31,"postnummer":"5001","poststed":"Bergen","adresse":"Kalfaret","email":"test@testesen.no"};
	
	$httpBackend.whenGET('/sendsoknad/rest/grunnlagsdata/912731247').respond(pensjonist);	
	$httpBackend.whenGET('/sendsoknad/rest/grunnlagsdata/12345612345').respond(arbeidssoker);

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