describe('datepicker', function () {
    var ugyldigFremtidigDatoFeil = 'Ugyldig fremtidig dato';
    var tildatoFeil = 'Ugyldig tildato';
    var formatFeil = 'Ugyldig format';
    var ikkeGyldigDato = 'Ugyldig dato';
    var requiredFeil = 'Required';
    var datoFormat = 'dd.mm.yyyy';
    var label = 'Labeltekst';

    var scope, rootElement, element;

    beforeEach(module('nav.datepicker', 'templates-main', 'nav.cmstekster'));

    beforeEach(module(function ($provide) {
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

    describe('single datepicker', function() {
        beforeEach(inject(function ($compile, $rootScope) {
            rootElement = angular.element('<form><div nav-dato ng-model="fraDato" er-required="true" label="label.tekst" required-error-message="required.feil"></div></form>');

            $compile(rootElement)($rootScope);
            $rootScope.$apply();
            element = rootElement.find('.datepicker');
            scope = element.scope();
        }));

        it('skal ha rett labeltekst', function() {
            expect(element.find('.labeltekst').text()).toBe(label);
        });

        it('skal ha rett datoformat, som skal ha klasse vekk', function() {
            var datoFormatElem = element.find('.labeltekst').next();
            expect(datoFormatElem.text()).toBe(datoFormat);
            expect(datoFormatElem.attr('class')).toContain('vekk');
        });

        it('skal vise required feilmelding dersom inputfelt får fokus og så mister fokus uten at noe er skrevet i det', function() {
            var input = element.find('input[type=text]');

            input.triggerHandler('focus');
            input.triggerHandler('blur');


            var requiredFeilElem = element.find('.melding');
            expect(requiredFeilElem.text()).toBe(requiredFeil);
        });

        describe('dato-mask', function() {
            var input;
            var maskElement;

            beforeEach(function() {
                input = element.find('input[type=text]');
                maskElement = element.find('.mask');
            });

            it('dato-mask skal ha rett datoformat', function() {
                expect(maskElement.text()).toBe(datoFormat);
            });

            it('dato-mask skal ha format d.mm.yyyy etter å ha skrevet ett tall', function() {
                var inputString = '1';
                input.val(inputString);
                input.trigger('input');
                expect(maskElement.text()).toBe(datoFormat.substring(inputString.length));
            });

            it('dato-mask skal ha format m.yyyy etter å ha skrevet 3 tall', function() {
                var inputString = '12.0';
                input.val(inputString);
                input.trigger('input');
                expect(maskElement.text()).toBe(datoFormat.substring(inputString.length));
            });
        });
    });
});