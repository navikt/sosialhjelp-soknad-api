'use strict';

/** jasmine spec */

describe('GrunnlagsdataController', function() {

	var $scope;
	var $controller;
    var $httpBackend;

	beforeEach(module('app.grunnlagsdata'));

	beforeEach(inject(function (_$httpBackend_, $injector) {
		$scope = $injector.get('$rootScope');

		$controller = $injector.get('$controller');

        $httpBackend = _$httpBackend_;
//        $httpBackend.expectGET('/sendsoknad/rest/utslagskriterier/1').
//            respond({"alder":true, "borIUtland":true });
	}));

	describe('fraMindreEnnTil', function(){
		it('skal returnere true for fra-dato 10.10.2010 og til-dato 10.10.2011', function(){
			var fra = new Date(2010, 10, 10);
			var til = new Date(2011, 10, 10);
			expect(fraMindreEnnTil(fra,til)).toEqual(true);
		});

		it('skal returnere false for fra-dato 10.10.2010 og til-dato 10.10.2010', function(){
			var fra = new Date(2010, 10, 10);
			var til = new Date(2010, 10, 10);
			expect(fraMindreEnnTil(fra,til)).toEqual(false);
		});

		it('skal returnere true for fra-dato 10.10.2010 og til-dato 10.11.2010', function(){
			var fra = new Date(2010, 10, 10);
			var til = new Date(2011, 11, 10);
			expect(fraMindreEnnTil(fra,til)).toEqual(true);
		});

        it('skal returnere false for fra-dato 10.11.2010 og til-dato 10.11.2010', function(){
			var fra = new Date(2010, 11, 10);
			var til = new Date(2010, 11, 10);
			expect(fraMindreEnnTil(fra,til)).toEqual(false);
		});

		it('skal returnere true for fra-dato 10.10.2010 og til-dato 11.10.2010', function(){
			var fra = new Date(2010, 10, 10);
			var til = new Date(2010, 10, 11);
			expect(fraMindreEnnTil(fra,til)).toEqual(true);
		});

		it('skal returnere false for fra-dato 11.10.2010 og til-dato 11.10.2010', function(){
			var fra = new Date(2010, 10, 11);
			var til = new Date(2010, 10, 11);
			expect(fraMindreEnnTil(fra,til)).toEqual(false);
		});
	});
});

