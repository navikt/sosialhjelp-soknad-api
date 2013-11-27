describe('directives', function () {
    var scope, form, element;

    beforeEach(module('app.directives'));
    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope;
        element = angular.element(
            '<form name="form">' +
                '<input type="text" ng-model="permiteringProsent" name="permiteringProsent" prosent />' +
                '</form>'
        );
        scope.permiteringProsent = '';
        $compile(element)(scope);
        scope.$digest();
        form = scope.form;
        element.scope().$apply();

    }));

    describe('prosent', function () {
        it('skal returnere true for prosenten 0', function () {
            form.permiteringProsent.$setViewValue('0');
            expect(scope.permiteringProsent).toEqual('0');
            expect(form.permiteringProsent.$valid).toBe(true);
        });
        it('skal returnere true for prosenten 100', function () {
            form.permiteringProsent.$setViewValue('100');
            expect(scope.permiteringProsent).toEqual('100');
            expect(form.permiteringProsent.$valid).toBe(true);
        });
        it('skal returnere false for prosenten p', function () {
            form.permiteringProsent.$setViewValue('p');
            expect(scope.permiteringProsent).toBeUndefined();
            expect(form.permiteringProsent.$valid).toBe(false);
        });
        it('skal returnere false for prosenten -1', function () {
            form.permiteringProsent.$setViewValue('-1');
            expect(scope.permiteringProsent).toBeUndefined();
            expect(form.permiteringProsent.$valid).toBe(false);
        });
        it('skal returnere false for prosenten 50.0', function () {
            form.permiteringProsent.$setViewValue('50.0');
            expect(scope.permiteringProsent).toBeUndefined();
            expect(form.permiteringProsent.$valid).toBe(false);
        });
        it('skal returnere false for prosenten 101', function () {
            form.permiteringProsent.$setViewValue('101');
            expect(scope.permiteringProsent).toBeUndefined();
            expect(form.permiteringProsent.$valid).toBe(false);
        });
        it('skal returnere false for prosenten 20%', function () {
            form.permiteringProsent.$setViewValue('20%');
            expect(scope.permiteringProsent).toBeUndefined();
            expect(form.permiteringProsent.$valid).toBe(false);
        });
    });
});


describe('directives', function () {
    var scope, form, element, checkbox;

    beforeEach(module('app.directives'));
    beforeEach(inject(function ($compile, $rootScope) {
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

    describe('booleanVerdi', function () {
        it('viewvalue skal settes til false dersom det ikke er satt en verdi i modellen', function () {
            expect(checkbox.$viewValue).toEqual(false);
        });
        it('viewvalue skal være true når modellen er en string som sier "true"', function () {
            scope.checkboxValueModel = 'true';
            element.scope().$apply();
            expect(checkbox.$viewValue).toEqual(true);
        });
        it('viewvalue skal være false når modellen er en string som sier "false"', function () {
            scope.checkboxValueModel = 'false';
            element.scope().$apply();
            expect(checkbox.$viewValue).toEqual(false);
        });
        it('modellen skal lagres som string "true" når viewvalue (boolean) settes til true', function () {
            checkbox.$setViewValue(true);
            element.scope().$apply();
            expect(scope.checkboxValueModel).toEqual('true');
        });
        it('modellen skal lagres som string "false" når viewvalue (boolean) settes til false', function () {
            checkbox.$setViewValue(false);
            element.scope().$apply();
            expect(scope.checkboxValueModel).toEqual('false');
        });
    });
});

describe('cmstekster-direktiv', function () {
    var scope, element;

    beforeEach(module('nav.cmstekster', function ($provide) {
        $provide.value("data", {
            tekster: {
                "nokkel.label": "Label",
                "nokkel.input": "Inputlabel"
            }
        });
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope;

        element = angular.element(
            '<div name="testdiv" >' +
                '<input type="text" name="testinput" cmstekster="nokkel.input" />' +
                '<span name="testspan" cmstekster="nokkel.label"></span>' +
            '</div>'
        );
        $compile(element)(scope);
        scope.$digest();
        element.scope().$apply();
    }));

    it('skal legge inn rett tekst ("Label") fra key i ett html-element', function () {
        var spanElement = element.find('span');
        expect(spanElement.text()).toEqual('Label');
    });

    it('skal legge inn rett tekst fra key som value-attributt på ett input-element', function () {
        var inputElement = element.find('input');
        expect(inputElement.attr('value')).toEqual('Inputlabel');
    });
});