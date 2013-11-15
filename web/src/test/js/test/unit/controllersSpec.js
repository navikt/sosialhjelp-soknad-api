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

describe('DagpengerControllere', function() {
    var scope, ctrl, form;

    beforeEach(
        module('app.services', 'app.controllers', 'nav.feilmeldinger')
    );

     beforeEach(module(function($provide) {
         $provide.value("data", {});
    }));

    beforeEach(inject(function ( $rootScope, $controller, $compile, $httpBackend) {
        $httpBackend.expectGET('../js/app/directives/feilmeldinger/feilmeldingerTemplate.html').
            respond('');

        scope = $rootScope.$new();
        scope.validateFormFunctionBleKalt = false;
        scope.validateForm = function(form) {
            scope.validateFormFunctionBleKalt = true;
        };
        scope.runValidation = function(form) {
           //expected call..
        };


        scope.soknadData = {
            fakta: {}
        }

        form = angular.element(
            '<form name="form">'
          +      '<form-errors></form-errors>'
          +  '</form>'
        );

        form.$setValidity = function(key, value) {
            //expected call..
        }
        $compile(form)(scope);

        
        scope.$digest();
        form.scope().$apply();
    }));

    describe('egennaeringCtrl', function() {
        beforeEach(inject(function ($controller) {
            ctrl = $controller('EgennaeringCtrl', {
                $scope: scope
            });
        }));

        it('skal kalle metode for å validere form', function() {
            expect(scope.validateFormFunctionBleKalt).toEqual(false);
            scope.validerEgennaering(form);
            expect(scope.validateFormFunctionBleKalt).toEqual(true);
        });
    })

    describe('vernepliktCtrl', function() {
        beforeEach(inject(function ($controller) {
            ctrl = $controller('VernepliktCtrl', {
                $scope: scope
            });
        }));

        it('skal kalle metode for å validere form', function() {
            expect(scope.validateFormFunctionBleKalt).toEqual(false);
            scope.validerVerneplikt(form);
            expect(scope.validateFormFunctionBleKalt).toEqual(true);
        });
    })

    describe('UtdanningCtrl', function() {
        beforeEach(inject(function ($controller) {
            ctrl = $controller('UtdanningCtrl', {
                $scope: scope
            });
        }));

        it('skal kalle metode for å validere form', function() {
            expect(scope.validateFormFunctionBleKalt).toEqual(false);
            scope.validerUtdanning(form);
            expect(scope.validateFormFunctionBleKalt).toEqual(true);
        });
    })

    describe('ReellarbeidssokerCtrl', function() {
        beforeEach(inject(function ($controller) {
            ctrl = $controller('ReellarbeidssokerCtrl', {
                $scope: scope
            });
        }));

        it('skal kalle metode for å validere form', function() {
            scope.soknadData.fakta.villigdeltid = true;
            scope.soknadData.fakta.villigpendle = true;
            expect(scope.validateFormFunctionBleKalt).toEqual(false);
            scope.validerReellarbeidssoker(form);
            expect(scope.validateFormFunctionBleKalt).toEqual(true);
        });
    })

    describe('YtelserCtrl', function() {
        var nokler = ['nokkel1', 'nokkel2'];

        beforeEach(inject(function ($controller) {
            ctrl = $controller('YtelserCtrl', {
                $scope: scope
            });
        }));

        it('skal returnere false for avhukede checkbokser dersom ingen har en satt verdi i modellen', function() {
            expect(scope.erCheckboxerAvhuket(nokler)).toEqual(false);
        });

        it('skal returnere false for avhukede checkbokser dersom alle verdiene for nøklene er satt til false i modellen', function() {
            scope.soknadData.fakta.nokkel1 = {value: false};
            scope.soknadData.fakta.nokkel2 = {value: false};
            expect(scope.erCheckboxerAvhuket(nokler)).toEqual(false);
        });

        it('skal returnere true for avhukede checkbokser dersom alle verdiene for nøklene er satt til true i modellen', function() {
            scope.soknadData.fakta.nokkel1 = {value: true};
            scope.soknadData.fakta.nokkel2 = {value: true};
            expect(scope.erCheckboxerAvhuket(nokler)).toEqual(true);
        });

        it('skal returnere true for avhukede checkbokser dersom en av verdiene for nøklene er satt til true i modellen, mens de andre er satt til false', function() {
            scope.soknadData.fakta.nokkel1 = {value: true};
            scope.soknadData.fakta.nokkel2 = {value: false};
            expect(scope.erCheckboxerAvhuket(nokler)).toEqual(true);
        });

        it('skal returnere true for avhukede checkbokser dersom en av verdiene for nøklene er satt til true i modellen, mens de andre ikke er satt', function() {
            scope.soknadData.fakta.nokkel1 = {value: true};
            expect(scope.erCheckboxerAvhuket(nokler)).toEqual(true);
        });
    })
})
