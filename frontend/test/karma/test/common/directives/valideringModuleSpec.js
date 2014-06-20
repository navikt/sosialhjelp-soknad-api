describe('valideringBlurValidate', function () {
    var element, scope, timeout, elementNavn, form;

    beforeEach(module('nav.validering', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel',
            '{feilmeldingstekst}': 'Min feilmelding'}
        });
        $provide.value("$cookieStore", {
            get: function () {
                return true;
            }
        });
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        scope = $rootScope;
        element = angular.element(
            '<form name="form">' +
                '<div class="form-linje"> ' +
                '<input type="text" ng-required="true" data-ng-model="modell" data-blur-validate name="inputname" data-error-messages="{feilmeldingstekst}"> ' +
                '<span class="melding"></span>' +
                '</div> ' +
                '</form>');

        $compile(element)(scope);
        scope.$apply();
        form = scope.form;
        elementNavn = form.inputname;
    }));

    describe('blurValidate', function () {
        it('elementet skal ikke ha klassen feil', function () {
            expect(element.find('.form-linje').hasClass('feil')).toBe(false);
        });
        it('element skal få klassen feil på blur-event', function () {
            expect(element.find('.form-linje').hasClass('feil')).toBe(false);
            var inputEl = element.find('input');
            inputEl.blur();
            expect(element.find('.form-linje').hasClass('feil')).toBe(true);
        });
        it('element skal ikke få klassen feil på blur-event hvis input-feltet er gyldig', function () {
            expect(element.find('.form-linje').hasClass('feil')).toBe(false);
            var inputEl = element.find('input');
            elementNavn.$setViewValue("litt tekst");
            scope.$apply();
            inputEl.blur();
            expect(element.find('.form-linje').hasClass('feil')).toBe(false);
        });
        it('elementet ikker gyldig og så blir det gyldig, skal ikke ha klasse feil', function () {
            var inputEl = element.find('input');
            expect(element.find('.form-linje').hasClass('feil')).toBe(false);
            inputEl.blur();
            expect(element.find('.form-linje').hasClass('feil')).toBe(true);
            elementNavn.$setViewValue("litt tekst");
            scope.$apply();
            inputEl.blur();
            expect(element.find('.form-linje').hasClass('feil')).toBe(false);
        });
        it('melding skal inneholde feilmeldingsteksten når den får klassen feil', function () {
            expect(element.find('.form-linje').hasClass('feil')).toBe(false);
            var inputEl = element.find('input');
            inputEl.blur();
            expect(element.find('.form-linje').hasClass('feil')).toBe(true);
            expect(element.find('.melding').text()).toBe('Min feilmelding');
        });
        it('elementet skal få klassen feil når blokken blir validert og elementet ikke er gyldig', function () {
            scope.$broadcast("RUN_VALIDATIONform");
            expect(element.find('.form-linje').hasClass('feil')).toBe(true);
        });
        it('elementet skal få klassen feil når blokken blir validert og elementet er gyldig', function () {
            elementNavn.$setViewValue("litt tekst");
            scope.$apply();
            scope.$broadcast("RUN_VALIDATIONform");
            expect(element.find('.form-linje').hasClass('feil')).toBe(false);
        });
        it('elementet skal få klassen feil når blokken blir validert og elementet er ikke gyldig', function () {
            scope.$broadcast("RUN_VALIDATIONform");
            expect(element.find('.form-linje').hasClass('feil')).toBe(true);
        });
        it('elementet skal få klassen feil når blokken blir validert og elementet er ikke gyldig men har klassen ng-valid', function () {
            element.find('input').addClass("ng-valid");
            scope.$broadcast("RUN_VALIDATIONform");
            expect(element.find('.form-linje').hasClass('feil')).toBe(true);
        });
    });
});
describe('valideringBlurValidate', function () {
    var element, scope, timeout, elementNavn, form;

    beforeEach(module('nav.validering', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel',
            feilmeldingstekst: 'Min feilmelding'}
        });
        $provide.value("$cookieStore", {
            get: function () {
                return true;
            }
        });
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        scope = $rootScope;
        element = angular.element(
            '<form name="form">' +
                '<div class="form-linje"> ' +
                '<input type="text" ng-required="true" data-ng-model="modell" data-blur-validate name="inputname" data-error-messages="{required: \'feilmeldingstekst\'}"> ' +
                '<span class="melding"></span>' +

                '</div> ' +
                '</form>');

        $compile(element)(scope);
        scope.$apply();
        form = scope.form;
        elementNavn = form.inputname;
    }));

    describe('blurValidate', function () {
        it('melding med klassen ng-binding skal ikke få satt tekst', function () {
            var meldingEl = element.find('.melding');
            meldingEl.addClass('ng-binding');
            var inputEl = element.find('input');
            inputEl.blur();
            expect(meldingEl.text()).toBe('');
        });
        it('error-messages som et object skal sette riktig feilmelding', function () {
            var meldingEl = element.find('.melding');
            var inputEl = element.find('input');
            inputEl.blur();
            scope.$apply();

            expect(meldingEl.text()).toBe('Min feilmelding');
        });
    });
});
describe('valideringclickValidate', function () {
    var rootScope, element, scope, timeout, elementNavn, form, elementNavnReq;

    beforeEach(module('nav.validering', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel',
            '{feilmeldingstekst}': 'Min feilmelding'}
        });
        $provide.value("$cookieStore", {
            get: function () {
                return true;
            }
        });
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        rootScope = $rootScope;
        scope = $rootScope;
        element = angular.element(
            '<form name="form">' +
                '<div class="form-linje"> ' +
                '<input type="radio" required data-ng-model="modell" data-click-validate name="inputnameReq" data-error-messages="w{feilmeldingstekst}w"> ' +
                '<span class="melding"></span>' +
                '</div> ' +
                '<div class="form-linje"> ' +
                '<input type="radio" data-ng-model="modell" data-click-validate name="inputname" data-error-messages="{feilmeldingstekst}">' +
                '<span class="melding"></span>' +
                '</div> ' +
                '</form>');

        $compile(element)(scope);
        scope.$apply();
        form = scope.form;
        elementNavn = form.inputname;
        elementNavnReq = form.inputnameReq;
    }));

    beforeEach(function () {
        jasmine.Clock.useMock();
    });

    describe('clickValidate', function () {
        it('elementet skal ikke ha klassen feil', function () {
            expect(element.find('.form-linje').first().hasClass('feil')).toBe(false);
        });
        it('elementet skal få klassen feil når blokken blir validert og elementet ikke er gyldig', function () {
            expect(element.find('.form-linje').first().hasClass('feil')).toBe(false);
            scope.$broadcast("RUN_VALIDATIONform");
            expect(element.find('.form-linje').first().hasClass('feil')).toBe(true);
        });
        it('elementet skal ikke få klassen feil når blokken blir validert og elementet ikke er required', function () {
            var formEl = element.find('.form-linje').last();

            expect(formEl.hasClass('feil')).toBe(false);
            scope.$broadcast("RUN_VALIDATIONform");
            expect(formEl.hasClass('feil')).toBe(false);
        });
        it('elementet blir gyldig så skal feil-klassen fjernes', function () {
            var formEl = element.find('.form-linje').first();
            expect(formEl.hasClass('feil')).toBe(false);
            elementNavnReq.$setViewValue("true");
            scope.$apply();
            expect(formEl.hasClass('feil')).toBe(false);
        });
        it('elementet blir gyldig så skal feil-klassen fjernes', function () {
            var formEl = element.find('.form-linje').first();
            scope.$broadcast("RUN_VALIDATIONform");
            expect(formEl.hasClass('feil')).toBe(true);
            elementNavnReq.$setViewValue("true");
            scope.$apply();
            jasmine.Clock.tick(2000);
            expect(formEl.hasClass('feil')).toBe(false);
        });
        it('elementet blir gyldig så skal feil-klassen fjernes', function () {
            var formEl = element.find('.form-linje').first();
            formEl.addClass('aktiv-feilmelding');
            scope.$broadcast("RUN_VALIDATIONform");
            expect(formEl.hasClass('aktiv-feilmelding')).toBe(true);
            expect(formEl.hasClass('feil')).toBe(true);
            elementNavnReq.$setViewValue("true");
            scope.$apply();
            jasmine.Clock.tick(3000);
            expect(formEl.hasClass('feil')).toBe(false);
        });
        it('melding skal inneholde feilmelding når elementet ikke er gyldig og har feilstylingklassen', function () {
            expect(element.find('.form-linje').first().hasClass('feil')).toBe(false);
            scope.$broadcast("RUN_VALIDATIONform");
            expect(element.find('.form-linje').first().hasClass('feil')).toBe(true);
            expect(element.find('.melding').first().text()).toBe("Min feilmelding");
        });
        it('skal ikke få klassen feil når bolken valideres og feltet er gyldig', function () {
            expect(element.find('.form-linje').first().hasClass('feil')).toBe(false);
            elementNavnReq.$setViewValue("true");
            scope.$apply();
            scope.$broadcast("RUN_VALIDATIONform");
            expect(element.find('.form-linje').first().hasClass('feil')).toBe(false);
        });
    });
});
describe('checkboxValidate', function () {
    var rootScope, element, scope, timeout, elementNavn, form, elementNavnReq;

    beforeEach(module('nav.validering', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel',
            '{feilmeldingstekst}': 'Min feilmelding'}
        });
        $provide.value("$cookieStore", {
            get: function () {
                return true;
            }
        });
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        rootScope = $rootScope;
        scope = $rootScope;
        timeout = $timeout;
        element = angular.element(
            '<form name="form">' +
                '<div class="form-linje" data-checkbox-validate> ' +
                '<input type="checkbox" required data-ng-model="modell"  name="inputnameReq" data-error-messages="{feilmeldingstekst}"> ' +
                '<span class="melding"></span>' +
                '</div> ' +
                '<div class="form-linje" data-checkbox-validate> ' +
                '<input type="checkbox" ng-checked="true" data-ng-model="model2"  name="inputname" data-error-messages="{feilmeldingstekst}">' +
                '<span class="melding"></span>' +
                '</div> ' +
                '</form>');

        $compile(element)(scope);
        scope.$apply();
        form = scope.form;
        elementNavn = form.inputname;
        elementNavnReq = form.inputnameReq;
    }));

    beforeEach(function () {
        jasmine.Clock.useMock();
    });

    describe('clickValidate', function () {
        it('elementet skal ikke ha klassen feil', function () {
            expect(element.find('.form-linje').first().hasClass('feil')).toBe(false);
        });
        it('elementet skal få klassen feil når blokken blir validert og elementet ikke er gyldig', function () {
            expect(element.find('.form-linje').first().hasClass('feil')).toBe(false);
            scope.$broadcast("RUN_VALIDATIONform");
            expect(element.find('.form-linje').first().hasClass('feil')).toBe(true);
        });
        it('elementet skal få klassen feilstyling når blokken blir validert og elementet ikke er gyldig og har klassen checkbox', function () {
            var formelement = element.find('.form-linje').first();
            formelement.addClass('checkbox');
            expect(formelement.hasClass('feil')).toBe(false);
            scope.$broadcast("RUN_VALIDATIONform");
            expect(formelement.hasClass('feil')).toBe(false);
            expect(formelement.hasClass('feilstyling')).toBe(true);
        });
        it('elementet skal ikke få klassen feil når blokken blir validert og elementet er gyldig', function () {
            var formelement = element.find('.form-linje').last();
            scope.$broadcast("RUN_VALIDATIONform");
            expect(formelement.hasClass('feil')).toBe(false);
        });
        it('elementet blir gyldig så skal feil-klassen fjernes', function () {
            var formEl = element.find('.form-linje').first();
            expect(formEl.hasClass('feil')).toBe(false);
            elementNavnReq.$setViewValue("true");
            scope.$apply();
            expect(formEl.hasClass('feil')).toBe(false);
        });
        it('elementet blir gyldig så skal feil-klassen fjernes', function () {
            var formEl = element.find('.form-linje').first();
            scope.$broadcast("RUN_VALIDATIONform");
            expect(formEl.hasClass('feil')).toBe(true);
            scope.modell = true;
            scope.$digest();
            jasmine.Clock.tick(3000);
            expect(formEl.hasClass('feil')).toBe(false);
        });
        it('elementet blir gyldig og har aktiv-feilmelding-klasse så skal feil-klassen fjernes', function () {
            var formEl = element.find('.form-linje').first();
            formEl.addClass('aktiv-feilmelding');
            scope.$broadcast("RUN_VALIDATIONform");
            expect(formEl.hasClass('aktiv-feilmelding')).toBe(true);
            expect(formEl.hasClass('feil')).toBe(true);
            scope.modell = true;
            scope.$digest();
            jasmine.Clock.tick(3000);
            expect(formEl.hasClass('feil')).toBe(false);
        });
        it('elementet blir gyldig og har feilstyling-klasse men ikke aktiv-feilmelding-klasse, så skal feilstyling-klassen fjernes', function () {
            var formEl = element.find('.form-linje').first();
            formEl.addClass('feilstyling');
            expect(formEl.hasClass('feilstyling')).toBe(true);
            expect(formEl.hasClass('aktiv-feilmelding')).toBe(false);
            scope.modell = true;
            scope.$digest();
            jasmine.Clock.tick(3000);
            expect(formEl.hasClass('feilstyling')).toBe(false);
        });
        it('elementet blir gyldig og har feilstyling-klasse og aktiv-feilmelding-klasse, så skal feilstyling-klassen fjernes', function () {
            var formEl = element.find('.form-linje').first();
            formEl.addClass('feilstyling');
            formEl.addClass('aktiv-feilmelding');
            expect(formEl.hasClass('feilstyling')).toBe(true);
            expect(formEl.hasClass('aktiv-feilmelding')).toBe(true);
            scope.modell = true;
            scope.$digest();
            jasmine.Clock.tick(3000);
            expect(formEl.hasClass('feilstyling')).toBe(false);
        });
    });
});