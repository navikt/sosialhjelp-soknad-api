describe('directives', function() {
	var scope, form, element;

	beforeEach(module('app.directives'));
	beforeEach(inject(function($compile, $rootScope){
		scope = $rootScope;
		element = angular.element(
			'<form name="form">' + 
			'<input type="text" ng-model="permiteringProsent" name="permiteringProsent" prosent />' +
			'</form>'
			);
		scope.permiteringProsent = ''
		$compile(element)(scope);
		scope.$digest();
		form = scope.form;	
		element.scope().$apply();	

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
		it('skal returnere false for prosenten p', function(){
			form.permiteringProsent.$setViewValue('p');
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