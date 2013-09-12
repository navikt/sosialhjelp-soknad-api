describe('brukerdata domene', function(){

	var $scope;
	var $controller;
	var $httpBackend;

	beforeEach(module('brukerdata'));

	describe('controllers', function () {

		beforeEach(inject(function($injector) {
			$scope = $injector.get('$rootScope');
			$controller = $injector.get('$controller');
			$httpBackend = $injector.get('$httpBackend');
		}));

		it('skal returnere soknaddata', inject(function(HentSoknadService) {
			var testbruker = {fornavn: 'Ketil'};
			$httpBackend.expectGET('/sendsoknad/rest/soknad/1').respond(testbruker);
			var params = {$scope: $scope};
			var ctrl = $controller('BrukerdataCtrl', params);
			
			expect($scope.b.fornavn).toEqual('Ketil');
		}));
	})
});