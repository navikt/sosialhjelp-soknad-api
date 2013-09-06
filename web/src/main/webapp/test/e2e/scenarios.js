'use strict';

describe('Send soknad app', function() {

	beforeEach(function() {
		browser().navigateTo('../../html/index.html');
	});


	it('skal redirigere index.html til index.html#/soknadiste', function(){
		expect(browser().location().url()).toBe('/soknadliste');
	});

	it("skal kunne hente frem epost etter browser back", function() {
		element("#dagpenger").click();
		
		input("ePost").enter("ketil.velle@gmail.com");
		element("#neste").click();
		browser().navigateTo('#/dagpenger');
		expect(input("ePost").val()).toEqual("ketil.velle@gmail.com");


	});

});
