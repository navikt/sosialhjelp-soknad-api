'use strict';

/** jasmine spec */

describe('Controllers', function() {

	describe('PersonaliaCtrl', function() {

		it('skal returnere personalia for bruker', function(){
			var scope = {},
				ctrl = new PersonaliaCtrl(scope);
			expect(scope.personalia.fornavn).toEqual('Ingvild');
		});
	});
 
});