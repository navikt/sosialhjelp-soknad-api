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
        it('tekstene skal bli satt', function() {
            expect(scope.value).toBe('value');
            expect(scope.navlabel).toBe('navlabel.true');
        });
        it('name skal bli satt til navlabel', function() {
            expect(scope.name).toBe('navlabel');
        });
        it('hvisAktiv skal returnere true hvis faktum value er det samme som scope.value', function() {
            expect(scope.hvisAktiv()).toBe(true);
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
        it('tekstene skal bli satt', function() {
            expect(scope.value).toBe('value');
            expect(scope.navlabel).toBe('navlabel.false');
        });
        it('name skal bli satt til navlabel', function() {
            expect(scope.name).toBe('navlabel');
        });
        it('hvisAktiv skal returnere false hvis faktum value ikke er det samme som scope.value', function() {
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
        it('name skal bli satt til utdanning', function() {
            expect(scope.navlabel).toBe('etellerutdanningnoe');
            expect(scope.name).toBe('utdanning');
        });
        it('hvisAktiv skal returnere false hvis faktum value ikke er det samme som scope.value', function() {
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
        it('name skal bli satt til arbeidstilstand', function() {
            expect(scope.navlabel).toBe('et.arbeidstilstand.noe');
            expect(scope.name).toBe('arbeidstilstand');
        });
        it('hvisAktiv skal returnere false hvis faktum value ikke er det samme som scope.value', function() {
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
                'data-navlabel="et.noe" ' +
                'data-navfeilmelding="tittel.key"> ' +
                '</div> ' +
                '</form>');

        $rootScope.faktum = {value: ''};
        $compile(element)($rootScope);
        $rootScope.$apply();

        scope = element.find('div').scope();
    }));
    describe("navradio", function () {
        it('hvis navlabel ikke inneholder true eller false eller noen av de andre spesielle keyene skal det settes til en tom string', function() {
            expect(scope.navlabel).toBe('et.noe');
            expect(scope.name).toBe('');
        });
    });
});