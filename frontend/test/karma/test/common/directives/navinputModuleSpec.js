describe('navradio', function () {
    var scope, element;

    beforeEach(module('nav.input', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
        $provide.value("data", {});
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element(
            '<form name="form" > ' +
                '<div data-navradio ' +
                'data-value="value" ' +
                'data-navconfig ' +
                'data-nav-faktum="arbeidstilstand" ' +
                'data-navlabel="navlabel.true" ' +
                'data-navfeilmelding="tittel.key"> ' +
                '</div> ' +
                '</form>');

        $rootScope.faktum = {value: 'value'};
        $compile(element)($rootScope);
        $rootScope.$apply();

        scope = element.find('div').scope();
    }));
    describe("navradio", function () {
        it('tekstene skal bli satt', function () {
            expect(scope.value).toBe('value');
            expect(scope.navlabel).toBe('navlabel.true');
        });
        it('name skal bli satt til navlabel', function () {
            expect(scope.name).toBe('navlabel');
        });
        it('hvisAktiv skal returnere true hvis faktum value er det samme som scope.value', function () {
            expect(scope.hvisAktiv()).toBe(true);
        });
        it('hvisHarTransvludedInnhold skal returnere false', function () {
            expect(scope.hvisHarTranscludedInnhold()).toEqual(false);
        });
    });
});
describe('navradio', function () {
    var scope, element;

    beforeEach(module('nav.input', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
        $provide.value("data", {});
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element(
            '<form name="form" > ' +
                '<div data-navradio ' +
                'data-value="value" ' +
                'data-navconfig ' +
                'data-nav-faktum="arbeidstilstand" ' +
                'data-navlabel="navlabel.false" ' +
                'data-navfeilmelding="tittel.key"> ' +
                '</div> ' +
                '</form>');

        $rootScope.faktum = {value: ''};
        $compile(element)($rootScope);
        $rootScope.$apply();

        scope = element.find('div').scope();
    }));
    describe("navradioMedFalseLabel", function () {
        it('tekstene skal bli satt', function () {
            expect(scope.value).toBe('value');
            expect(scope.navlabel).toBe('navlabel.false');
        });
        it('name skal bli satt til navlabel', function () {
            expect(scope.name).toBe('navlabel');
        });
        it('hvisAktiv skal returnere false hvis faktum value ikke er det samme som scope.value', function () {
            expect(scope.hvisAktiv()).toBe(false);
        });
    });
});
describe('navradio', function () {
    var scope, element;

    beforeEach(module('nav.input', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
        $provide.value("data", {});
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element(
            '<form name="form" > ' +
                '<div data-navradio ' +
                'data-value="value" ' +
                'data-navconfig ' +
                'data-nav-faktum="arbeidstilstand" ' +
                'data-navlabel="etellerutdanningnoe" ' +
                'data-navfeilmelding="tittel.key"> ' +
                '</div> ' +
                '</form>');

        $rootScope.faktum = {value: ''};
        $compile(element)($rootScope);
        $rootScope.$apply();

        scope = element.find('div').scope();
    }));
    describe("navradioMedUtdanning", function () {
        it('hvisAktiv skal returnere false hvis faktum value ikke er det samme som scope.value', function () {
            expect(scope.hvisAktiv()).toBe(false);
        });
    });
});
describe('navradioMedArbeidsforhold', function () {
    var scope, element;

    beforeEach(module('nav.input', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
        $provide.value("data", {});
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element(
            '<form name="form" > ' +
                '<div data-navradio ' +
                'data-value="value" ' +
                'data-navconfig ' +
                'data-nav-faktum="arbeidstilstand" ' +
                'data-navlabel="et.arbeidstilstand.noe" ' +
                'data-navfeilmelding="tittel.key"> ' +
                '</div> ' +
                '</form>');

        $rootScope.faktum = {value: ''};
        $compile(element)($rootScope);
        $rootScope.$apply();

        scope = element.find('div').scope();
    }));
    describe("navradio", function () {
        it('name skal bli satt til arbeidstilstand', function () {
            expect(scope.navlabel).toBe('et.arbeidstilstand.noe');
            expect(scope.name).toBe('et.arbeidstilstand');
        });
        it('hvisAktiv skal returnere false hvis faktum value ikke er det samme som scope.value', function () {
            expect(scope.hvisAktiv()).toBe(false);
        });
    });
});

describe('navcheckbox', function () {
    var scope, element;

    beforeEach(module('nav.input', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster':
        {'hjelpetekstlabel.hjelpetekst.tittel': 'Min tittel',
            'hjelpetekstlabel.hjelpetekst.tekst': 'Min tekst'}
        });
        $provide.value("data", {});
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element(
            '<form name="form" > ' +
                '<div data-navcheckbox ' +
                'data-navconfig ' +
                ' data-nav-faktum="offentligTjenestepensjon" ' +
                'data-navlabel="hjelpetekstlabel" ' +
                ' data-navendret="enFunksjon()"> ' +
                '</form>');

        $rootScope.faktum = {value: ''};
        $compile(element)($rootScope);
        $rootScope.$apply();

        scope = element.find('div').scope();
    }));
    describe("navradio", function () {
        it('skal sette riktig tekster for hjelpetekst', function () {
            expect(scope.hjelpetekst.tittel).toBe('Min tittel');
            expect(scope.hjelpetekst.tekst).toBe('Min tekst');
        });
        it('hvisHarHjelpetekst skal returnere true', function () {
            expect(scope.hvisHarHjelpetekst()).toNotBe(undefined);
        });
        it('hvisHarTransvludedInnhold skal returnere false', function () {
            expect(scope.hvisHarTranscludedInnhold()).toEqual(false);
        });
        it('scope.endret skal kalle funksjonen som er lagret i scope.navendret', function () {
            spyOn(scope, "navendret");
            scope.endret();
            expect(scope.navendret).toHaveBeenCalled();
        });
    });
});
describe('navcheckboxUtenHjelpetekst', function () {
    var scope, element;

    beforeEach(module('nav.input', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster':
        {'hjelpetekstlabel.hjelpetekst.tittel': 'Min tittel',
            'hjelpetekstlabel.hjelpetekst.tekst': 'Min tekst'}
        });
        $provide.value("data", {});
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element(
            '<form name="form" > ' +
                '<div data-navcheckbox ' +
                'data-navconfig ' +
                ' data-nav-faktum="offentligTjenestepensjon" ' +
                'data-navlabel="ikkehjelpetekstlabel" ' +
                ' data-navendret="enFunksjon()"> ' +
                '</div> ' +
                '</form>');

        $rootScope.faktum = {value: ''};
        $compile(element)($rootScope);
        $rootScope.$apply();

        scope = element.find('div').scope();
    }));
    describe("navradio", function () {
        it('skal ikke sette hjelpetekster', function () {
            expect(scope.hjelpetekst.tittel).toBe(undefined);
            expect(scope.hjelpetekst.tekst).toBe(undefined);
        });
        it('hvisHarHjelpetekst og ikke har hjelpetekst, skal returnere undefined', function () {
            expect(scope.hvisHarHjelpetekst()).toEqual(undefined);
        });
    });
});
describe('navtekst', function () {
    var scope, element;

    beforeEach(module('nav.input', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster':
        {'hjelpetekstlabel.hjelpetekst.tittel': 'Min tittel',
            'hjelpetekstlabel.hjelpetekst.tekst': 'Min tekst'}
        });
        $provide.value("data", {});
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element(
            '<form name="form" > ' +
                '<div data-navtekst ' +
                'data-navconfig ' +
                ' data-nav-faktum="offentligTjenestepensjon" ' +
                'data-navlabel="ikkehjelpetekstlabel" ' +
                ' data-navendret="enFunksjon()"> ' +
                '</div> ' +
                '</form>');

        $rootScope.faktum = {value: ''};
        $compile(element)($rootScope);
        $rootScope.$apply();

        scope = element.find('div').scope();
    }));
    describe("navtekst", function () {
        it('regexvalidering skal settes til tom string nar det ikke skal vaere regexvalidering pa feltet', function () {
            expect(scope.regexvalidering).toBe('');
            expect(scope.inputfeltmaxlength).toBe(undefined);
        });
        it('harSporsmal skal returnere false hvis sporsmal ikke finnes', function () {
            expect(scope.harSporsmal()).toBe(false);
        });
    });
});
describe('navtekstMedSporsmalOgRegEx', function () {
    var scope, element;

    beforeEach(module('nav.input', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster':
        {'hjelpetekstlabel.hjelpetekst.tittel': 'Min tittel',
            'hjelpetekstlabel.hjelpetekst.tekst': 'Min tekst'}
        });
        $provide.value("data", {});
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element(
            '<form name="form" > ' +
                '<div data-navtekst ' +
                'data-navconfig ' +
                ' data-nav-faktum="offentligTjenestepensjon" ' +
                'data-navlabel="ikkehjelpetekstlabel" ' +
                ' data-navendret="enFunksjon()"' +
                'data-regexvalidering="/^(\\d+(?:[\\.\\,]\\d{0,2})?)$/"' +
                'data-navsporsmal="barnetillegg.barnetilegg.barneinntekttall.sporsmal" ' +
                'data-inputfeltmaxlength="200"> ' +
                '</div> ' +
                '</form>');

        $rootScope.faktum = {value: ''};
        $compile(element)($rootScope);
        $rootScope.$apply();

        scope = element.find('div').scope();
    }));
    describe("navtekst", function () {
        it('regexvalidering skal settes til den aktuelle regexpatternet', function () {
            expect(scope.regexvalidering).toBe("/^(\\d+(?:[\\.\\,]\\d{0,2})?)$/");
            expect(scope.inputfeltmaxlength).toBe('200');
        });
        it('harSporsmal skal returnere true hvis har sporsmal', function () {
            expect(scope.harSporsmal()).toBe(true);
        });
    });
});
describe('navorganisasjonsnummerfelt', function () {
    var scope, element;

    beforeEach(module('nav.input', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster':
        {'hjelpetekstlabel.hjelpetekst.tittel': 'Min tittel',
            'hjelpetekstlabel.hjelpetekst.tekst': 'Min tekst'}
        });
        $provide.value("data", {});
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element(
            '<form name="form" > ' +
                '<div data-navorganisasjonsnummerfelt ' +
                'data-nav-faktum="egennaering.gardsbruk.false.organisasjonsnummer" ' +
                'data-navconfig ' +
                'data-navlabel="egennaering.gardsbruk.false.organisasjonsnummer" ' +
                'data-nav-vis-slett="false" ' +
                'data-navfeilmelding="{ required: \'egennaering.gardsbruk.false.organisasjonsnummer.feilmelding\', ' +
                'pattern: \'organisasjonsnummer.format.feilmelding\'}"></div> ' +
                '</form>');

        $rootScope.faktum = {value: ''};
        $compile(element)($rootScope);
        $rootScope.$apply();

        scope = element.find('div').scope();
    }));
    describe("navtekst", function () {
        it('visSlett skal ikke vises for index 0', function () {
            expect(scope.visSlett(0)).toBe(false);
        });
        it('visSlett skal ikke vises for navVisSlett satt til false selv med index storre enn 0', function () {
            expect(scope.visSlett(2)).toBe(false);
        });
        it('visSlett skal vises for index storre enn 0', function () {
            expect(scope.visSlett(0)).toBe(false);
        });
    });
});
describe('navorganisasjonsnummerfeltNavVisSlettTrue', function () {
    var scope, element;

    beforeEach(module('nav.input', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster':
        {'hjelpetekstlabel.hjelpetekst.tittel': 'Min tittel',
            'hjelpetekstlabel.hjelpetekst.tekst': 'Min tekst'}
        });
        $provide.value("data", {});
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element(
            '<form name="form" > ' +
                '<div data-navorganisasjonsnummerfelt ' +
                'data-nav-faktum="egennaering.gardsbruk.false.organisasjonsnummer" ' +
                'data-navconfig ' +
                'data-navlabel="egennaering.gardsbruk.false.organisasjonsnummer" ' +
                'data-nav-vis-slett="true" ' +
                'data-navfeilmelding="{ required: \'egennaering.gardsbruk.false.organisasjonsnummer.feilmelding\', ' +
                'pattern: \'organisasjonsnummer.format.feilmelding\'}"></div> ' +
                '</form>');

        $rootScope.faktum = {value: ''};
        $compile(element)($rootScope);
        $rootScope.$apply();

        scope = element.find('div').scope();
    }));
    describe("orgnr med navvisslett satt til true", function () {
        it('visSlett skal ikke vises for index 0 og navVisSlett true', function () {
            expect(scope.visSlett(0)).toBe(false);
        });
        it('visSlett skal vises for index storre enn 0', function () {
            expect(scope.visSlett(1)).toBe(true);
        });
    });
});
describe('navorganisasjonsnummerfeltVisSlett', function () {
    var scope, element;

    beforeEach(module('nav.input', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster':
        {'hjelpetekstlabel.hjelpetekst.tittel': 'Min tittel',
            'hjelpetekstlabel.hjelpetekst.tekst': 'Min tekst'}
        });
        $provide.value("data", {});
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element(
            '<form name="form" > ' +
                '<div data-navorganisasjonsnummerfelt ' +
                'data-nav-faktum="egennaering.gardsbruk.false.organisasjonsnummer" ' +
                'data-navconfig ' +
                'data-navlabel="egennaering.gardsbruk.false.organisasjonsnummer" ' +
                'data-navfeilmelding="{ required: \'egennaering.gardsbruk.false.organisasjonsnummer.feilmelding\', ' +
                'pattern: \'organisasjonsnummer.format.feilmelding\'}"></div> ' +
                '</form>');

        $rootScope.faktum = {value: ''};
        $compile(element)($rootScope);
        $rootScope.$apply();

        scope = element.find('div').scope();
    }));
    describe("organisasjonsnummer med ikke satt navVisSlett attributt", function () {
        it('visSlett skal vises for index 2 hvis navVisSlett ikke er definert', function () {
            expect(scope.visSlett(2)).toBe(true);
        });
    });
});
describe('orgnrValidate', function () {
    var scope, element, inputnavn, form;

    beforeEach(module('nav.input', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster':
        {'hjelpetekstlabel.hjelpetekst.tittel': 'Min tittel',
            'hjelpetekstlabel.hjelpetekst.tekst': 'Min tekst'}
        });
        $provide.value("data", {});
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element(
            '<form name="form" > ' +
                '<input type="text" name="inputname" ' +
                'value="value" ' +
                'data-ng-model="model" ' +
                'data-ng-required="true" ' +
                'data-error-messages="navfeilmelding" ' +
                'data-blur-validate ' +
                'data-ng-pattern="/[0-9]{9}/" ' +
                'maxlength="9" ' +
                'data-orgnr-validate> ' +
            '</form>');

        $rootScope.faktum = {value: ''};
        $compile(element)($rootScope);
        $rootScope.$apply();

        scope = element.find('input').scope();
        scope.lagreFaktum = function(){};
        form = scope.form;
        inputnavn = form.inputname;
    }));
    describe("organisasjonsnummer med ikke satt navVisSlett attributt", function () {
        it('lagreFaktum skal ikke bli kalt hvis inputfeltet ikke er valid og blur blir kalt', function () {
            spyOn(scope, 'lagreFaktum');
            var inputEl = element.find('input');
            inputEl.blur();
            expect(scope.lagreFaktum).wasNotCalled();

        });
        it('lagreFaktum skal bli kalt hvis inputfeltet er valid og blur blir kalt', function () {
            spyOn(scope, 'lagreFaktum');

            var inputEl = element.find('input');
            inputnavn.$setViewValue(123456789);
            inputEl.blur();
            expect(scope.lagreFaktum).toHaveBeenCalled();
        });
        it('lagreFaktum skal ikke bli kalt hvis inputfeltet inneholder andre ting enn tall og blur blir kalt', function () {
            spyOn(scope, 'lagreFaktum');

            var inputEl = element.find('input');
            inputnavn.$setViewValue(12 + 'f' + 456789);
            inputEl.blur();
            expect(scope.lagreFaktum).wasNotCalled();
        });
        it('lagreFaktum skal ikke bli kalt hvis inputfeltet inneholder for fa tall og blur blir kalt', function () {
            spyOn(scope, 'lagreFaktum');

            var inputEl = element.find('input');
            inputnavn.$setViewValue(12345678);
            inputEl.blur();
            expect(scope.lagreFaktum).wasNotCalled();
        });
    });
});
