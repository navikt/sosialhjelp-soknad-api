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

	/* TODO: Kommenter inn igjen når testene kjører

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
    var scope, form, element, checkbox;

    beforeEach(module('app.directives'));
    beforeEach(inject(function($compile, $rootScope){
        scope = $rootScope;
        element = angular.element(
            '<form name="form">' +
                '<input type="checkbox" ng-model="checkboxValueModel" name="checkbox" boolean-verdi />' +
                '</form>'
        );
        $compile(element)(scope);
        scope.$digest();
        form = scope.form;
        checkbox = form.checkbox;
        element.scope().$apply();

    }));

    describe('booleanVerdi', function(){
        it('viewvalue skal være true når modellen er en string som sier "true"', function() {
            scope.checkboxValueModel = 'true';
            element.scope().$apply();
            expect(checkbox.$viewValue).toEqual(true);
        });
        it('viewvalue skal være false når modellen er en string som sier "false"', function() {
            scope.checkboxValueModel = 'false';
            element.scope().$apply();
            expect(checkbox.$viewValue).toEqual(false);
        });
    });
});



describe('directives', function() {
    var scope, element;

    beforeEach(module('app.directives'));
    beforeEach(inject(function($compile, $rootScope){
        scope = $rootScope;
        scope.soknadData = {
            fakta: {}
        };

        scope.data = {
            redigeringsModus: true
        };

        scope.tekster = {
            sporsmal: 'sporsmal',
            svar_ja: 'svar_ja',
            svar_nei: 'svar_nei'
        };

        element = angular.element(
            '<radioknapp model="soknadData.fakta.testName.value"' +
                        'modus="data.redigeringsModus"' +
                        'sporsmal="tekster.sporsmal"' +
                        'svarAlternativ1="tekster.svar_ja"' +
                        'svarAlternativ2="tekster.svar_nei"' +
                        'name="testName"/>'
        );
        $compile(element)(scope);
        scope.$digest();
        element.scope().$apply();

    }));

    describe('radioknapp', function(){
        it('skal først vises i redigeringsmodus, så endre til oppsummeringsmodus', function() {
            var redigering = element.children()[1];
            var oppsummering = element.children()[2];
            expect(redigering.className).toNotContain('ng-hide');
            expect(oppsummering.className).toContain('ng-hide');
            scope.data.redigeringsModus = false;
            element.scope().$apply();
            expect(redigering.className).toContain('ng-hide');
            expect(oppsummering.className).toNotContain('ng-hide');
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
			fra: new Date(2010, 10, 10),
			til: new Date(2011, 10, 10),
			fraTo: new Date(2010, 10, 10),
			tilTo: new Date(2011, 10, 10),
			permiteringVetikke: true
		};

		$compile(element)(scope);
		scope.$digest();
		form = scope.form;
		element.scope().$apply();
	}));
	it('skal returnere false når fra-dato endres fra mindre til større enn til-dato', function(){
		scope.arbeidsforhold.fra = new Date(2012, 10, 10);
		element.scope().$apply();
		expect(scope.arbeidsforhold.fra).toEqual(new Date(2012, 10, 10));
		expect(form.fra.$invalid).toBe(false);
		expect(scope.$parent.arbeidsforhold.til).toBeUndefined();
		element.scope().$apply();
	});

	it('skal returnere true når fra-dato endres fra større til mindre enn til-dato', function(){
		scope.arbeidsforhold.fra = new Date(2011, 10, 10);
		element.scope().$apply();

		expect(scope.arbeidsforhold.fra).toEqual(new Date(2011, 10, 10));

		expect(form.til.$valid).toBe(false);
		expect(scope.$parent.arbeidsforhold.til).toBeUndefined();

		scope.arbeidsforhold.fra = new Date(2010, 10, 10);
		element.scope().$apply();
		expect(scope.arbeidsforhold.fra).toEqual(new Date(2010, 10, 10));
		expect(scope.arbeidsforhold.til).toEqual(new Date(2011, 10, 10));
		expect(form.til.$valid).toBe(true);
	});

	it('skal returnere false når til-dato endres til mindre enn fra-dato',function(){
		form.til.$setViewValue(new Date(2010, 10, 10));
		element.scope().$apply();
		expect(scope.$parent.arbeidsforhold.til).toBeUndefined();
		expect(form.til.$valid).toBe(false);
		expect(scope.arbeidsforhold.fra).toEqual(new Date(2010, 10, 10));
	});

	it('skal returnere true når til-dato endres til større enn fra-dato', function(){
		scope.arbeidsforhold.fra = new Date(2011, 10, 10);
		element.scope().$apply();

		expect(scope.$parent.arbeidsforhold.til).toBeUndefined();
		expect(form.til.$valid).toBe(false);
		expect(scope.arbeidsforhold.fra).toEqual(new Date(2011, 10, 10));

		form.til.$setViewValue(new Date(2012, 10, 10));
		element.scope().$apply();
		expect(scope.$parent.arbeidsforhold.til).toEqual(new Date(2012, 10, 10));
		expect(form.til.$valid).toBe(true);
		expect(scope.arbeidsforhold.fra).toEqual(new Date(2011, 10, 10));
		element.scope().$apply();
	});

	it('endringer på fra skal ikke påvirke tilTo', function(){
		scope.arbeidsforhold.fra = new Date(2011, 10, 10);
		element.scope().$apply();
		expect(scope.arbeidsforhold.tilTo).toEqual(new Date(2011, 10, 10));
		expect(form.tilTo.$valid).toBe(true);
	});

	it('endringer på fra2 skal ikke påvirke til', function(){
		scope.arbeidsforhold.fraTo = new Date(2011, 10, 10);
		element.scope().$apply();
		expect(scope.arbeidsforhold.til).toEqual(new Date(2011, 10, 10));
		expect(form.til.$valid).toBe(true);
	});
*/

});