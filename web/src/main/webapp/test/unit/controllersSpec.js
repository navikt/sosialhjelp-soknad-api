'use strict';

/** jasmine spec */

describe('Controllers', function() {

	var $scope;
	var $controller;

	beforeEach(module('app.grunnlagsdata'));
	
	beforeEach(inject(function ($injector) {
		$scope = $injector.get('$rootScope');

		$controller = $injector.get('$controller');
	}));

	describe('GrunnlagsdataCtrl', function() {
		var scope, ctrl;
		beforeEach(function(){
			scope = $scope;
			ctrl = $controller('GrunnlagsdataCtrl', {$scope: scope});

		});

		//it('skal returnere personalia for bruker', function(){
		//	expect(scope.personalia.fornavn).toEqual('Test');
		//});

		
		it('skal returnere false for ung arbeidsøker', function() {
			//var vel ikke et nedre alderskrav
		});
	
		it('skal returnere false for gammel arbeidsøker', function() {
			scope.personaliaAlder.alder = 67;
			expect(scope.isGyldigAlder()).toEqual(false);
		});

		it('skal returnere true for myndig arbeidsøker', function() {
			scope.personaliaAlder.alder = 18;
			expect(scope.isGyldigAlder()).toEqual(true);
		});

		it('skal returnere false for på grensen til for gammel arbeidsøker', function() {
			scope.personaliaAlder.alder = 66;
			expect(scope.isGyldigAlder()).toEqual(true);
		});
		it('skal returnere false for bor i utland med folkeregistrert adresse i norge og midlertidig adresse i norge', function() {
			scope.personalia.bostedsadresseLandkode = 'NOR';
			scope.personalia.midlertidigadresseLandkode = 'NOR';
			expect(scope.borIUtlandet()).toEqual(false);
		});

		it('skal returnere bor i utland med midlertidig adresse i utlandet og folkeregistrert adresse i utlandet', function() {
			scope.personalia.bostedsadresseLandkode = 'ENG';
			scope.personalia.midlertidigadresseLandkode = 'ENG';
			expect(scope.borIUtlandet()).toEqual(true);
		});
		it('skal returnere false for bor i utland med midlertidig adresse i norge og folkeregistrert adresse i utlandet', function() {
			scope.personalia.bostedsadresseLandkode = 'ENG';
			scope.personalia.midlertidigadresseLandkode = 'NOR';
			expect(scope.borIUtlandet()).toEqual(false);
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
			var fra = new Date('10.10.2010');
			var til = new Date('10.10.2011');
			expect(fraMindreEnnTil(fra,til)).toEqual(true);
		});
		it('skal returnere false for fra-dato 10.10.2010 og til-dato 10.10.2010', function(){
			var fra = new Date('10.10.2010');
			var til = new Date('10.10.2010');
			expect(fraMindreEnnTil(fra,til)).toEqual(false);
		});
		it('skal returnere true for fra-dato 10.10.2010 og til-dato 10.11.2010', function(){
			var fra = new Date('10.10.2010');
			var til = new Date('10.11.2010');
			expect(fraMindreEnnTil(fra,til)).toEqual(true);
		});
		it('skal returnere false for fra-dato 10.11.2010 og til-dato 10.11.2010', function(){
			var fra = new Date('10.11.2010');
			var til = new Date('10.11.2010');
			expect(fraMindreEnnTil(fra,til)).toEqual(false);
		});
		it('skal returnere true for fra-dato 10.10.2010 og til-dato 11.10.2010', function(){
			var fra = new Date('10.10.2010');
			var til = new Date('11.10.2010');
			expect(fraMindreEnnTil(fra,til)).toEqual(true);
		});
		it('skal returnere false for fra-dato 11.10.2010 og til-dato 11.10.2010', function(){
			var fra = new Date('11.10.2010');
			var til = new Date('11.10.2010');
			expect(fraMindreEnnTil(fra,til)).toEqual(false);
		});
	});
});

