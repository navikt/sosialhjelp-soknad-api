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
		it('skal returnere true med folkeregistrert adresse i utlandet', function() {
			scope.folkeregistrertAdresse.land = 'england';
			expect(scope.borIUtlandet()).toEqual(true);
		});

		it('skal returnere false med folkeregistrert adresse i norge', function() {
			scope.folkeregistrertAdresse.land = 'norge';
			scope.midlertidigAdresse.land = 'norge';

			expect(scope.borIUtlandet()).toEqual(false);
		});

		it('skal returnere true med midlertidig adresse i utlandet', function() {
			scope.folkeregistrertAdresse.land = 'norge';
			scope.midlertidigAdresse.land = 'england';
			expect(scope.borIUtlandet()).toEqual(true);
		});
		it('skal returnere false/true??? med midlertidig adresse i norge og folkeregistrert adresse i utlandet', function() {
			scope.folkeregistrertAdresse.land = 'england';
			scope.midlertidigAdresse.land = 'norge';
			expect(scope.borIUtlandet()).toEqual(true);
		});
	});
});

describe('directives', function() {
	var scope, form;

	beforeEach(module('app.directives'));
	beforeEach(inject(function($compile, $rootScope){
		scope = $rootScope;
		var element = angular.element(
			'<form name="form">' + 
			'<input ng-model="permiteringProsent" name="permiteringProsent" prosent/>' +
			'</form>'
			);
		scope.permiteringProsent = ''
		$compile(element)(scope);
		scope.$digest();
		form = scope.form;	
	}));

	describe('prosent', function(){
		it('skal returnere true for prosenten 0', function(){
			form.permiteringProsent.$setViewValue('0');
			expect(scope.permiteringProsent).toEqual('0');
			expect(form.permiteringProsent.$valid).toBe(true); 
		});
		it('skal returnere true for prosenten 100', function(){
			form.permiteringProsent.$setViewValue('100');
			expect(scope.permiteringProsent).toEqual('100');
			expect(form.permiteringProsent.$valid).toBe(true); 
		});
		it('skal returnere false for prosenten o', function(){
			form.permiteringProsent.$setViewValue('o');
			expect(scope.permiteringProsent).toBeUndefined();
			expect(form.permiteringProsent.$valid).toBe(false); 
		});
		it('skal returnere false for prosenten -1', function(){
			form.permiteringProsent.$setViewValue('-1');
			expect(scope.permiteringProsent).toBeUndefined();
			expect(form.permiteringProsent.$valid).toBe(false); 
		});
		it('skal returnere false for prosenten 50.0', function(){
			form.permiteringProsent.$setViewValue('50.0');
			expect(scope.permiteringProsent).toBeUndefined();
			expect(form.permiteringProsent.$valid).toBe(false); 
		});
		it('skal returnere false for prosenten 101', function(){
			form.permiteringProsent.$setViewValue('101');
			expect(scope.permiteringProsent).toBeUndefined();
			expect(form.permiteringProsent.$valid).toBe(false); 
		});
		it('skal returnere false for prosenten 20%', function(){
			form.permiteringProsent.$setViewValue('20%');
			expect(scope.permiteringProsent).toBeUndefined();
			expect(form.permiteringProsent.$valid).toBe(false); 
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

describe('directives', function() {
	var scope, form, element;
	beforeEach(module('app.directives'));
	beforeEach(inject(function($compile, $rootScope){
		scope = $rootScope;
		element = angular.element(
			'<form name="form">' + 
			'<input ng-model="arbeidsforhold.fra" name="fra" />' +
			'<input ng-model="$parent.arbeidsforhold.til" name="til" datotil fra-dato="arbeidsforhold.fra" til-dato="$parent.arbeidsforhold.til"/>' +
			'<input data-ng-model="arbeidsforhold.permiteringVetikke" name="permiteringVetikke" value="vetikke"  ng-click="vetIkke()" />' +
			'<input ng-model="arbeidsforhold.fraTo" name="fraTo" />' +
			'<input ng-model="$parent.arbeidsforhold.tilTo" name="tilTo" datotil fra-dato="arbeidsforhold.fraTo" til-dato="$parent.arbeidsforhold.tilTo"/>' +
			'</form>'
			);

		scope.arbeidsforhold = { 
			fra: new Date('10.10.2010'),
			til: new Date('10.10.2011'),
			fraTo: new Date('10.10.2010'),
			tilTo: new Date('10.10.2011'),
			permiteringVetikke: true
		};
		scope.vetIkke = function(){
			if(scope.arbeidsforhold.permiteringVetikke){
				scope.arbeidsforhold.til = undefined; 
				scope.arbeidsforhold.fra = undefined;
			}
		};

		$compile(element)(scope);
		scope.$digest();
		form = scope.form;
		element.scope().$apply();	
	}));
	it('skal returnere false når fra-dato endres fra mindre til større enn til-dato', function(){
		scope.arbeidsforhold.fra = new Date('10.10.2012');
		element.scope().$apply();	
		expect(scope.arbeidsforhold.fra).toEqual(new Date('10.10.2012'));
		expect(form.fra.$invalid).toBe(false);
		expect(scope.$parent.arbeidsforhold.til).toBeUndefined();
		element.scope().$apply();	
	});

	it('skal returnere true når fra-dato endres fra større til mindre enn til-dato', function(){
		scope.arbeidsforhold.fra = new Date('10.10.2011');
		element.scope().$apply(); 

		expect(scope.arbeidsforhold.fra).toEqual(new Date('10.10.2011'));

		expect(form.til.$valid).toBe(false); 
		expect(scope.$parent.arbeidsforhold.til).toBeUndefined();

		scope.arbeidsforhold.fra = new Date('10.10.2010');
		element.scope().$apply(); 
		expect(scope.arbeidsforhold.fra).toEqual(new Date('10.10.2010'));
		expect(scope.arbeidsforhold.til).toEqual(new Date('10.10.2011'));
		expect(form.til.$valid).toBe(true); 

	});
	it('skal returnere false når til-dato endres til mindre enn fra-dato',function(){
		form.til.$setViewValue(new Date('10.10.2010'));
		element.scope().$apply(); 
		expect(scope.$parent.arbeidsforhold.til).toBeUndefined();
		expect(form.til.$valid).toBe(false); 
		expect(scope.arbeidsforhold.fra).toEqual(new Date('10.10.2010'));
	});

	it('skal returnere true når til-dato endres til større enn fra-dato', function(){
		scope.arbeidsforhold.fra = new Date('10.10.2011');
		element.scope().$apply(); 

		expect(scope.$parent.arbeidsforhold.til).toBeUndefined();
		expect(form.til.$valid).toBe(false); 
		expect(scope.arbeidsforhold.fra).toEqual(new Date('10.10.2011'));

		form.til.$setViewValue(new Date('10.10.2012'));
		element.scope().$apply(); 
		expect(scope.$parent.arbeidsforhold.til).toEqual(new Date('10.10.2012'));
		expect(form.til.$valid).toBe(true); 
		expect(scope.arbeidsforhold.fra).toEqual(new Date('10.10.2011'));
		element.scope().$apply(); 
	});
	
	
	it('endringer på fra skal ikke påvirke tilTo', function(){
		scope.arbeidsforhold.fra = new Date('10.10.2011');
		element.scope().$apply(); 
		expect(scope.arbeidsforhold.tilTo).toEqual(new Date('10.10.2011'));
		expect(form.tilTo.$valid).toBe(true);
	});

	it('endringer på fra2 skal ikke påvirke til', function(){
		scope.arbeidsforhold.fraTo = new Date('10.10.2011');
		element.scope().$apply(); 
		expect(scope.arbeidsforhold.til).toEqual(new Date('10.10.2011'));
		expect(form.til.$valid).toBe(true);
	});
});