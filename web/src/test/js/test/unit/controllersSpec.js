'use strict';

/** jasmine spec */

describe('Controllers', function() {

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
        $httpBackend.whenGET('/sendsoknad/rest/enonic/utslagskriterier').respond({});
	}));

	describe('GrunnlagsdataCtrl', function() {
		var scope, ctrl;
		beforeEach(function(){
			scope = $scope;
			ctrl = $controller('GrunnlagsdataCtrl', {$scope: scope});

		});

		it('skal kvalifisere for gjenopptak hvis bruker har fått dagpenger siste året og ikke har hatt permitering', function() {
			expect(scope.fattDagpengerSisteAaret()).toEqual(true);
			expect(scope.hattPermitering()).toEqual(false);
			//expect(scope.kvalifisererForGjenopptak()).toEqual("*Gjenopptak pga ikke hatt permitering*");
		});
		it('skal kvalifisere for gjenopptak hvis bruker har fått dagpenger siste året, og har ikke jobbet hos samme arbeidsgvier mer enn seks uker', function() {
			//expect(scope.fattDagpengerSisteAaret()).toEqual(true);
			//expect(scope.jobbetHosSammeArbeidsgiverMerEnnSeksUker()).toEqual(false);
			//expect(scope.kvalifisererForGjenopptak()).toEqual("*Gjennopptak pga ikke hatt jobb hos samme arbeidsgiver vedkommende ble permitert fra, i mer enn 6 uker");
		});
		it('skal kvalifisere for gjenopptak hvis bruker har fått dagpenger siste året, og har ikke jobbet hos samme arbeidsgiver mer enn 26 uker og er fisker', function() {
			//expect(scope.fattDagpengerSisteAaret()).toEqual(true);
			//expect(scope.jobetMerEnn26Uker()).toEqual(false);
			//expect(scope.erFisker()).toEqual(true);
			//expect(scope.kvalifisererForGjenopptak()).toEqual("*Gjennopptak pga fisker");
		});
		it('skal kvalifisere for gjenopptak hvis bruker har fått dagpenger siste året, og har ikke jobbet hos samme arbeidsgiver mer enn 26 uker og er fisker', function() {
			//expect(scope.fattDagpengerSisteAaret()).toEqual(true);
			//expect(scope.avbruddPgaUtdanning()).toEqual(false);
			//expect(scope.kvalifisererForGjenopptak()).toEqual("*Gjennopptak pga ikke avbrudd pga utdanning");
		});
	});

	describe('fraMindreEnnTil', function(){
		it('skal returnere true for fra-dato 10.10.2010 og til-dato 10.10.2011', function(){
			var fra = new Date();
            fra.setFullYear(2010, 10, 10);
			var til = new Date();
            til.setFullYear(2011, 10, 10);
			expect(fraMindreEnnTil(fra,til)).toEqual(true);
		});

		it('skal returnere false for fra-dato 10.10.2010 og til-dato 10.10.2010', function(){
			var fra = new Date();
            fra.setFullYear(2010, 10, 10);
			var til = new Date();
            til.setFullYear(2010, 10, 10);
			expect(fraMindreEnnTil(fra,til)).toEqual(false);
		});

		it('skal returnere true for fra-dato 10.10.2010 og til-dato 10.11.2010', function(){
			var fra = new Date();
            fra.setFullYear(2010, 10, 10);
			var til = new Date();
            til.setFullYear(2011, 11, 10);
			expect(fraMindreEnnTil(fra,til)).toEqual(true);
		});

        it('skal returnere false for fra-dato 10.11.2010 og til-dato 10.11.2010', function(){
			var fra = new Date();
            fra.setFullYear(2010, 11, 10);
			var til = new Date('10.11.2010');
            til.setFullYear(2010, 11, 10);
			expect(fraMindreEnnTil(fra,til)).toEqual(false);
		});

		it('skal returnere true for fra-dato 10.10.2010 og til-dato 11.10.2010', function(){
			var fra = new Date();
            fra.setFullYear(2010, 10, 10);
			var til = new Date();
            til.setFullYear(2010, 10, 11);
			expect(fraMindreEnnTil(fra,til)).toEqual(true);
		});

		it('skal returnere false for fra-dato 11.10.2010 og til-dato 11.10.2010', function(){
			var fra = new Date();
            fra.setFullYear(2010, 10, 11);
			var til = new Date();
            til.setFullYear(2010, 10, 11);
			expect(fraMindreEnnTil(fra,til)).toEqual(false);
		});
	});
});
