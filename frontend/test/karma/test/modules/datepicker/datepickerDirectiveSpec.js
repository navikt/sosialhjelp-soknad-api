describe('datepicker directive', function () {
    var ugyldigFremtidigDatoFeil = 'Ugyldig fremtidig dato';
    var tildatoFeil = 'Ugyldig tildato';
    var formatFeil = 'Ugyldig format';
    var ikkeGyldigDato = 'Ugyldig dato';
    var requiredFeil = 'Required';
    var datoFormat = 'dd.mm.yyyy';
    var label = 'Labeltekst';

    var scope, form, element, rootScope;
    var todayMs = 1394108384263;
    var oldDate = Date;

    beforeEach(module('nav.datepicker', 'templates-main', 'nav.cms', 'ngMessages'));

    beforeEach(module(function ($provide) {
        Date = function() {
            return new oldDate(todayMs);
        };

        $provide.value("cms", {'tekster': {
            'dato.ugyldigFremtidig.feilmelding': ugyldigFremtidigDatoFeil,
            'dato.tilDato.feilmelding': tildatoFeil,
            'dato.format.feilmelding': formatFeil,
            'dato.ikkeGyldigDato.feilmelding': ikkeGyldigDato,
            'required.feil': requiredFeil,
            'dato.format': datoFormat,
            'label.tekst': label
        }});
        $provide.value("data", {});
    }));

    afterEach(function() {
        Date = oldDate;
    });

    describe('enkelt nav datepicker', function() {
        beforeEach(function() {
            spyOn(window, 'erTouchDevice').andReturn(false);
            spyOn(window, 'getIEVersion').andReturn(9);
        });

        beforeEach(inject(function ($compile, $rootScope) {
            form = angular.element(
                '<form>' +
                    '<div dato ng-model="dato" er-required="true" label="label.tekst" required-error-message="required.feil"></div>' +
                '</form>');
            rootScope = $rootScope;
            $compile(form)($rootScope);
            $rootScope.$apply();
            element = form.find('.datepicker');
            scope = element.isolateScope();
        }));

        describe('skal ha rett tekst', function() {
            it('skal ha rett labeltekst', function() {
                expect(element.find('.labeltekst').text()).toBe(label);
            });

            it('skal ha rett datoformat, som skal ha klasse vekk', function() {
                var datoFormatElem = element.find('.labeltekst').next();
                expect(datoFormatElem.text()).toBe(datoFormat);
                expect(datoFormatElem.attr('class')).toContain('vekk');
            });
        });

        describe('skal få rett datomask', function() {
            var input;
            var maskElement;

            beforeEach(function() {
                input = element.find('input[type=text]');
                maskElement = element.find('.mask');
            });

            it('datomask skal ha rett datoformat', function() {
                expect(maskElement.text()).toBe(datoFormat);
            });

            it('datomask skal ha format d.mm.yyyy etter å ha skrevet ett tall', function() {
                var inputString = '1';
                input.val(inputString);
                input.trigger('input');
                expect(maskElement.text()).toBe(datoFormat.substring(inputString.length));
            });

            it('datomask skal ha format m.yyyy etter å ha skrevet 3 tall', function() {
                var inputString = '12.0';
                input.val(inputString);
                input.trigger('input');
                expect(maskElement.text()).toBe(datoFormat.substring(inputString.length));
            });
        });

        describe('feilmeldinger', function() {
            var input;

            beforeEach(function() {
                input = element.find('input[type=text]');
            });

            it('skal ikke vise noen feilmeldinger initielt', function() {
                var errorMessageElements = element.find('span[data-ng-message]');

                expect(errorMessageElements.length).toBe(0);
            });

            it('skal få required feilmelding dersom man går inn i inputfeltet og så ut uten å ha skrevet noe', function() {
                input.triggerHandler('focus');
                input.triggerHandler('blur');
                scope.$apply();

                var errorMessageElement = element.find('span[data-ng-message="required"]');

                expect(errorMessageElement.length).toBe(1);
            });

            it('skal få required feilmelding dersom man går inn i inputfeltet og så ut uten å ha skrevet noe', function() {
                var inputString = '1';

                input.triggerHandler('focus');
                input.val(inputString);
                input.trigger('input');
                input.triggerHandler('blur');
                scope.$apply();

                var errorMessageElement = element.find('span[data-ng-message="dateFormat"]');

                expect(errorMessageElement.length).toBe(1);
            });

            it('skal få required feilmelding dersom man går inn i inputfeltet og så ut uten å ha skrevet noe', function() {
                var inputString = '30.02.2000';

                input.triggerHandler('focus');
                input.val(inputString);
                input.trigger('input');
                input.triggerHandler('blur');
                scope.$apply();

                var errorMessageElement = element.find('span[data-ng-message="validDate"]');

                expect(errorMessageElement.length).toBe(1);
            });
        });

        // TODO Skriv om dersom vi får lagt til angularui sin datepicker
//        describe('jQuery datepicker', function () {
//            var datepickerKnapp;
//            var datepickerInput;
//
//
//            beforeEach(function() {
//                datepickerKnapp = element.find('.apne-datepicker');
//                datepickerInput = element.find('input[type=hidden]');
//            });
//
//            it('skal få opp datepicker ved å trykke på datepicker-knappen', function () {
//                spyOn(scope, 'toggleDatepicker').andCallThrough();
//                datepickerKnapp.trigger('click');
//                scope.$apply();
//                expect(scope.toggleDatepicker).toHaveBeenCalled();
//            });
//        });
    });
});