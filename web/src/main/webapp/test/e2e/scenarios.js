'use strict';

describe('Send soknad app', function() {

	beforeEach(function() {
		browser().navigateTo('../../html/index.html');
	});


	it('skal redirigere index.html til index.html#/utslagskriterier', function(){
		expect(browser().location().url()).toBe('/utslagskriterier');
	});

	it("skal kunne hente frem epost etter browser back", function() {
		browser().navigateTo('#/dagpenger');
		input("data.epost").enter("ketil@mailprovider.com");
		element("#neste").click();
		browser().navigateTo('#/dagpenger');
		expect(input("data.epost").val()).toEqual("ketil@mailprovider.com");
	});

	it("skal kunne lagre mobil etter browser back", function() {
		browser().navigateTo('#/dagpenger');
		input("data.mobil").enter("55555555");
		element("#neste").click();
		browser().navigateTo('#/dagpenger');
		expect(input("data.mobil").val()).toBe("55555555");
	});
});
