'use strict';

/** jasmine spec */

describe('Controllers', function() {

	describe('PersonaliaCtrl', function() {

		it('skal returnere personalia for bruker', function(){
			var scope = {},
				ctrl = new PersonaliaCtrl(scope);
			expect(scope.personalia.fornavn).toEqual('Ingvild');
		});

		it('skal returnere false for ung arbeidsøker', function() {
			var scope = {};
			var ctrl = new PersonaliaCtrl(scope);
			scope.personalia.alder = 17;
			expect(scope.isGyldigAlder()).toEqual(false);
		});

		it('skal returnere false for gammel arbeidsøker', function() {
			var scope = {};
			var ctrl = new PersonaliaCtrl(scope);
			scope.personalia.alder = 67;
			expect(scope.isGyldigAlder()).toEqual(false);
		});


		it('skal returnere true for myndig arbeidsøker', function() {
			var scope = {};
			var ctrl = new PersonaliaCtrl(scope);
			scope.personalia.alder = 18;
			expect(scope.isGyldigAlder()).toEqual(true);
		});


		it('skal returnere false for på grensen til for gammel arbeidsøker', function() {
			var scope = {};
			var ctrl = new PersonaliaCtrl(scope);
			scope.personalia.alder = 66;
			expect(scope.isGyldigAlder()).toEqual(true);
		});

	});
 
});