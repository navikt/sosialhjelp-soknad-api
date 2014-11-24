describe('directives', function () {
    var scope, form, element, checkbox;

    beforeEach(module('sendsoknad.directives'));
    beforeEach(module(function ($provide) {
        $provide.value("data", {
        });
    }));

    describe('booleanVerdi', function () {
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

    describe('cmstekster-direktiv', function () {
        beforeEach(module('nav.cms', function ($provide) {
            $provide.value("cms", {
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
                    '<input type="text" name="testinput" value="{{ \'nokkel.input\' | cmstekst }}">' +
                    '<span name="testspan">{{ \'nokkel.label\' | cmstekst }}</span>' +
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
});