describe('datepicker', function () {
    var ugyldigFremtidigDatoFeil = 'Ugyldig fremtidig dato';
    var tildatoFeil = 'Ugyldig tildato';
    var formatFeil = 'Ugyldig format';
    var ikkeGyldigDato = 'Ugyldig dato';
    var requiredFeil = 'Required';
    var datoFormat = 'dd.mm.yyyy';
    var label = 'Labeltekst';

    var scope, rootElement, element, rootScope;
    var todayMs = 1394108384263;
    var oldDate = Date;

    beforeEach(module('nav.datepicker', 'templates-main', 'nav.cms'));

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

    describe('single datepicker', function() {
        beforeEach(inject(function ($compile, $rootScope) {
            rootElement = angular.element(
                '<form>' +
                    '<div dato ng-model="dato" er-required="true" label="label.tekst" required-error-message="required.feil"></div>' +
                '</form>');
            rootScope = $rootScope;
            $compile(rootElement)($rootScope);
            $rootScope.$apply();
            element = rootElement.find('.datepicker');
            scope = element.isolateScope();
        }));

        it('skal ha rett labeltekst', function() {
            expect(element.find('.labeltekst').text()).toBe(label);
        });

        it('skal ha rett datoformat, som skal ha klasse vekk', function() {
            var datoFormatElem = element.find('.labeltekst').next();
            expect(datoFormatElem.text()).toBe(datoFormat);
            expect(datoFormatElem.attr('class')).toContain('vekk');
        });

        describe('nav-datepicker', function() {
            beforeEach(function() {
                spyOn(window, 'erTouchDevice').andReturn(false);
            });

            it('skal vise nav-datepicker dersom det ikke er touchdevice', function () {
                expect($('.datepicker-input').parent().hasClass('ng-hide')).toBe(false);
            });

            // TODO: Refaktorer datepicker og testene!
//            it('skal vise required feilmelding dersom inputfelt får fokus og så mister fokus uten at noe er skrevet i det', function() {
//                var input = element.find('input[type=text]');
//
//                input.triggerHandler('focus');
//                input.triggerHandler('blur');
//
//                var requiredFeilElem = element.find('.melding');
//                expect(requiredFeilElem.text()).toBe(requiredFeil);
//            });

//            it('skal vise formatteringsfeilmelding dersom inputfeltet har tekst som ikke er på format dd.mm.yyyy', function() {
//                var input = element.find('input[type=text]');
//                if(!isIE()) {
//                    input.triggerHandler('focus');
//
//                    var inputString = '010.1.2012';
//                    input.val(inputString);
//
//                    input.trigger('input');
//                    input.triggerHandler('blur');
//                    scope.$apply();
//
//                    var feilElem = element.find('.melding');
//                    expect(feilElem.text()).toBe(formatFeil);
//                } else {
//                    expect(true).toBe(true);
//                }
//            });
//
//            it('skal få feilmelding dersom datoen ikke er gyldig (ikke finnes)', function() {
//                var input = element.find('input[type=text]').first();
//                if(!isIE()) {
//                    var inputString = '30.02.2010';
//                    spyOn(window, 'hentCaretPosisjon').andReturn(inputString.length); // Firefox klarer ikke gjøre dette selv i en test, så må mockes.
//                    input.triggerHandler('focus');
//                    input.val(inputString);
//                    input.triggerHandler('blur');
//
//                    var feilElem = element.find('.melding');
//                    expect(feilElem.text()).toBe(ikkeGyldigDato);
//                } else {
//                    expect(true).toBe(true);
//                }
//            });

            describe('dato-mask', function() {
                var input;
                var maskElement;

                beforeEach(function() {
                    input = element.find('input[type=text]');
                    maskElement = element.find('.mask');
                });

//                it('dato-mask skal ha rett datoformat', function() {
//                    expect(maskElement.text()).toBe(datoFormat);
//                });
//
//                it('dato-mask skal ha format d.mm.yyyy etter å ha skrevet ett tall', function() {
//                    if(!isIE()) {
//                        var inputString = '1';
//                        input.val(inputString);
//                        input.trigger('input');
//                        expect(maskElement.text()).toBe(datoFormat.substring(inputString.length));
//                    } else {
//                        expect(true).toBe(true);
//                    }
//
//                });
//
//                it('dato-mask skal ha format m.yyyy etter å ha skrevet 3 tall', function() {
//                    if(!isIE()) {
//                        var inputString = '12.0';
//                        input.val(inputString);
//                        input.trigger('input');
//                        expect(maskElement.text()).toBe(datoFormat.substring(inputString.length));
//                    } else {
//                        expect(true).toBe(true);
//                    }
//                });
            });

            describe('jQuery datepicker', function () {
                var datepickerKnapp;
                var datepickerInput;


                beforeEach(function() {
                    datepickerKnapp = element.find('.apne-datepicker');
                    datepickerInput = element.find('input[type=hidden]');
                });

//                it('skal få opp datepicker ved å trykke på datepicker-knappen', function () {
//                    spyOn(scope, 'toggleDatepicker').andCallThrough();
//                    datepickerKnapp.trigger('click');
//                    scope.$apply();
//                    expect(scope.toggleDatepicker).toHaveBeenCalled();
//                });
            });
        });

        describe('input type date', function() {
            beforeEach(function() {
                spyOn(window, 'erTouchDevice').andReturn(true);
                spyOn(window, 'getIEVersion').andReturn(-1);
            });

            it('skal vise input type date dersom det er touchdevice og ikke er IE', function () {
                expect($('input[type=date]').parent().hasClass('ng-hide')).toBe(false);
            });

//            it('skal vise required feilmelding dersom inputfelt får fokus og så mister fokus uten at noe er skrevet i det', function() {
//                var input = element.find('input[type=date]');
//
//                input.triggerHandler('focus');
//                input.triggerHandler('blur');
//
//                var requiredFeilElem = element.find('.melding');
//                expect(requiredFeilElem.text()).toBe(requiredFeil);
//            });
        });
    });
});