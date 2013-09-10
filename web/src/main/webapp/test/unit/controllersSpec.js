'use strict';

/** jasmine spec */

describe('Controllers', function() {

	describe('PersonaliaCtrl', function() {

		var $rootScope, $httpBackend, createController;

		beforeEach(inject(function($injector) {
			$httpBackend = $injector.get('$httpBackend');
			$httpBackend.when('GET', '/sendsoknad/rest/soknad/:soknadId').respond({soknadId: '1', gosysId: 'Dagpenger'});

			$rootScope = $injector.get('$controller');
			createController = function() {
				return $controller('PersonaliaCtrl',  {'$scope' : $rootScope});
			};
			
		}));

		it('skal returnere personalia for bruker', function(){
			$httpBackend.expectGET('/sendsoknad/rest/soknad/:soknadId');
			var controller = createController();
			$httpBackend.flush(); 
			expect(scope.personalia.gosysId).toEqual('Dagpenger');
		});
	});
 
});