describe('booleanradio', function () {
    var rootScope, element, scope, timeout, elementNavn, form, elementNavnReq, injectData;

    beforeEach(module('nav.booleanradio', 'nav.cms', 'templates-main', 'nav.input', 'nav.navfaktum', 'sendsoknad.services', 'nav.hjelpetekst', 'nav.animation'));

    var sporsmal = 'Spørsmal';
    var truelabel = 'True label';
    var falselabel = 'False label';
    var feilmelding = 'Feilmelding';

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster':
        {'radio.sporsmal': sporsmal,
        'radio.true': truelabel,
        'radio.false': falselabel,
        'radio.feilmelding': feilmelding,
        'radio.hjelpetekst.tittel': 'hjelpetekst tittel',
        'radio.hjelpetekst.tekst': 'hjelpetekst tekst'
        }
        });
        $provide.value("data", {
                fakta: [{
                    key: 'etfaktum',
                    value: 'true'
                }],
                soknad: {
                    soknadId: 1
                }
            }
        );
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout, data) {
        rootScope = $rootScope;
        timeout = $timeout;
        injectData = data;
        element = angular.element(
            '<form name="form">' +
                    '<div data-nav-faktum="etfaktum" ' +
                'data-navconfig ' +
                'data-ikke-auto-lagre="true" ' +
                'data-booleanradio ' +
                'data-nokkel="radio"> ' +
                '</div> ' +
            '</form>');

        $compile(element)(rootScope);
        rootScope.$apply();
        scope = element.find('div').scope();

    }));

    it('Skal sette inn korrekt tekst', function () {
        expect(element.find('h4 span').first().text().trim()).toBe(sporsmal);
        expect(element.find('label').first().text()).toBe(truelabel);
        expect(element.find('label').last().text()).toBe(falselabel);
    });

    it('skal lage korrekte nøkkler til CMS', function() {
        expect(scope.navfeilmelding).toBe('radio.feilmelding');
        expect(scope.hjelpetekst.tittel).toBe('radio.hjelpetekst.tittel');
        expect(scope.hjelpetekst.tekst).toBe('radio.hjelpetekst.tekst');
    });

    it('skal ha hjelpetekst-element', function () {
        expect(element.find('.hjelpetekst').length).toBe(1);
        expect(scope.hvisHarHjelpetekst()).toBe(true);
    });

    it('skal sjekke modellen korrekt', function () {
        expect(scope.hvisModelErTrue()).toBe(true);
        expect(scope.hvisModelErFalse()).toBe(false);
        expect(scope.vis()).toBe(false);

        injectData.fakta[0].value = 'false';
        scope.$apply();

        expect(scope.hvisModelErTrue()).toBe(false);
        expect(scope.hvisModelErFalse()).toBe(true);
        expect(scope.vis()).toBe(true);
    });
});