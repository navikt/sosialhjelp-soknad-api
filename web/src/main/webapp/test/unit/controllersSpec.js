'use strict';

/** jasmine spec */

describe('Controllers', function() {

	var $scope;
	var $controller;

	beforeEach(module('app.controllers'));
	
	beforeEach(inject(function ($injector) {
		$scope = $injector.get('$rootScope');

		$controller = $injector.get('$controller');
	}));

	describe('PersonaliaCtrl', function() {

		var scope, ctrl;
		beforeEach(function(){
			scope = $scope;
			ctrl = $controller('PersonaliaCtrl', {$scope: scope});
		});

		it('skal returnere personalia for bruker', function(){
			expect(scope.personalia.fornavn).toEqual('Ingvild');
		});

		
		it('skal returnere false for ung arbeidsøker', function() {
			scope.personalia.alder = 17;
			expect(scope.isGyldigAlder()).toEqual(false);
		});

		
		it('skal returnere false for gammel arbeidsøker', function() {
			scope.personalia.alder = 67;
			expect(scope.isGyldigAlder()).toEqual(false);
		});

		it('skal returnere true for myndig arbeidsøker', function() {
			scope.personalia.alder = 18;
			expect(scope.isGyldigAlder()).toEqual(true);
		});

		it('skal returnere false for på grensen til for gammel arbeidsøker', function() {
			scope.personalia.alder = 66;
			expect(scope.isGyldigAlder()).toEqual(true);
		});
	});
 
});