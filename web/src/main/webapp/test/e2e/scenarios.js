'use strict';

describe('Send soknad app', function() {

	beforeEach(function() {
		browser().navigateTo('../../html/index.html');
	});


	it('skal redirigere index.html til index.html#/soknadiste', function(){
		expect(browser().location().url()).toBe('/soknadliste');
	});
});