'use strict';

describe('Send soknad app', function() {

	beforeEach(function() {
			browser().navigateTo('../../html/index.html');
	});

	it('should be true', function() {
			expect(true).toBe(true);
	});

	it('skal redirigere index.html til index.html#/soknadiste', function(){
		expect(browser().location().url()).toBe('/soknadliste');
	});
});