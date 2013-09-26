'use strict';

// describe('Application test', function() {

//     beforeEach(function() {
//     	//login
//     	browser().navigateTo('http://e34apsl00227.devillo.no:8080/openam/UI/Login');
//     	input("IDToken1").enter('***REMOVED***');
//     	input("IDToken2").enter('Eifel123');
//     	element("#Login.Submit").click();
//     });

describe('Send soknad app', function() {

	beforeEach(function() {
		browser().navigateTo('../../html/index.html');
			//browser().navigateTo('http://a34duvw22389.devillo.no:8181/sendsoknad/soknadliste');
		});


	it('skal redirigere index.html til index.html#/utslagskriterier', function(){
		expect(browser().location().path()).toBe('/utslagskriterier');
	})
	
	it('skal redirigere index.html til index.html#/dagpenger hvis bruker kommer videre fra utslagskriterier', function() {
		expect(browser().location().path()).toBe('/utslagskriterier');

	})

	it('skal presentere personalia hvis bruker kommer videre fra utslagskriterier', function() {
		element("#fortsett").click();
			//expect(element("#soknadData.fakta.mellomnavn.value").val()).toEqual('Johan');
		})

	it('skal presentere reell-arbeidssøkersiden når man tykker på start-knappen på informasjonssiden', function(){
		browser().navigateTo('../../html/index.html#/informasjonsside');
		element("#start").click();
		expect(browser().location().path()).toBe('/reell-arbeidssoker');
	})
	

		//Disse testene må refaktores. 

		/*
		it("skal kunne hente frem epost etter browser back", function() {
			browser().navigateTo('#/personalia/1');
			expect(browser().location().url()).toBe('/personalia/1');
			input("soknadData.fakta.epost.value").enter("ketil@mailprovider.com");
			element("#neste").click();
			browser().navigateTo('#/personalia');
			expect(input("soknadData.fakta.epost.value").val()).toEqual("ketil@mailprovider.com");
		});

		it("skal kunne lagre mobil etter browser back", function() {
			browser().navigateTo('#/personalia/1');
			input("soknadData.fakta.telefon.value").enter("55555555");
			element("#neste").click();
			browser().navigateTo('#/personalia');
			expect(input("soknadData.fakta.telefon.value").val()).toBe("55555555");
		});
*/
});

//});