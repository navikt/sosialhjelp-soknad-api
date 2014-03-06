describe('validering', function () {
    var element, scope, timeout, elementNavn, form;

    beforeEach(module('nav.validering', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster':
        {'tittel.key': 'Min tittel',
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
                    '<input type="text" required data-ng-model="modell" data-blur-validate name="inputname" data-error-messages="{feilmeldingstekst}"> ' +
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
    });
});