describe('stegindikator', function () {
    var element, scope;

    beforeEach(module('nav.stegindikator', 'nav.cms', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
        $provide.value("data", {});
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element(
            '<div data-stegindikator data-steg-liste="veiledning, skjema, vedlegg, sendInn" data-aktiv-index="1"></div>');

        $compile(element)($rootScope);
        $rootScope.$apply();
        scope = element.find('div').scope();
    }));

    describe('Stegindikator', function() {
        it('Stegindikator skal inneholde de nøklene som blir sendt med direktivet', function() {
            expect(scope.stegListe).toBe('veiledning, skjema, vedlegg, sendInn');
        });
        it('data.liste skal inneholde elementene som blir sendt inn med direktivet', function() {
            expect(scope.data.liste.length).toBe(4);
            expect(scope.data.liste[2]).toBe('vedlegg');
        });
        it('Første steget skal alltid vaere klikkbart', function() {
            expect(scope.erKlikkbar(0)).toBe(true);
        });
        it('Andre steget skal kun vaere klikkbart hvis utfylling har startet', function() {
            expect(scope.erKlikkbar(1)).toBe(false);
        });
        it('tredje steget skal kun vaere klikkbart hvis skjemaet er ferdigutfylt', function() {
            expect(scope.erKlikkbar(2)).toBe(false);
        });
        it('fjerde steget skal kun vaere klikkbart hvis vedlegget er ferdigutfylt', function() {
            expect(scope.erKlikkbar(3)).toBe(false);
        });
        it('andre steget skal vaere klikkbart nar utfylling av skjema har startet', function() {
            expect(scope.erKlikkbar(3)).toBe(false);
        });
    });
});
describe('stegindikatorUtfyllingErStartet', function () {
    var element, scope;

    beforeEach(module('nav.stegindikator', 'nav.cms', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
        $provide.value("data", {
            soknad: {
                delstegStatus: 'UTFYLLING'
            }
        });
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element(
            '<div data-stegindikator data-steg-liste="veiledning, skjema, vedlegg, sendInn" data-aktiv-index="1"></div>');

        $compile(element)($rootScope);
        $rootScope.$apply();
        scope = element.find('div').scope();
    }));

    describe('Stegindikator', function(data) {
        it('Første steget skal alltid vaere klikkbart', function() {
            expect(scope.erKlikkbar(0)).toBe(true);
        });
        it('Andre steget skal vaere klikkbart nar utfylling har startet', function() {
            expect(scope.erKlikkbar(1)).toBe(true);
            expect(scope.erKlikkbar(2)).toBe(false);
            expect(scope.erKlikkbar(3)).toBe(false);
        });
    });
});
describe('stegindikatorUtfyllingErStartet', function () {
    var element, scope;

    beforeEach(module('nav.stegindikator', 'nav.cms', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
        $provide.value("data", {
            soknad: {
                delstegStatus: 'SKJEMA_VALIDERT'
            }
        });
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element(
            '<div data-stegindikator data-steg-liste="veiledning, skjema, vedlegg, sendInn" data-aktiv-index="1"></div>');

        $compile(element)($rootScope);
        $rootScope.$apply();
        scope = element.find('div').scope();
    }));

    describe('Stegindikator', function(data) {
        it('Første steget skal alltid vaere klikkbart', function() {
            expect(scope.erKlikkbar(0)).toBe(true);
        });
        it('Andre steget skal vaere klikkbart nar utfylling har startet', function() {
            expect(scope.erKlikkbar(1)).toBe(true);
            expect(scope.erKlikkbar(2)).toBe(true);
            expect(scope.erKlikkbar(3)).toBe(false);
        });
    });
});
describe('stegindikatorUtfyllingErStartet', function () {
    var element, scope;

    beforeEach(module('nav.stegindikator', 'nav.cms', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
        $provide.value("data", {
            soknad: {
                delstegStatus: 'VEDLEGG_VALIDERT'
            }
        });
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element(
            '<div data-stegindikator data-steg-liste="veiledning, skjema, vedlegg, sendInn" data-aktiv-index="1"></div>');

        $compile(element)($rootScope);
        $rootScope.$apply();
        scope = element.find('div').scope();
    }));

    describe('Stegindikator', function(data) {
        it('Første steget skal alltid vaere klikkbart', function() {
            expect(scope.erKlikkbar(0)).toBe(true);
        });
        it('Andre steget skal vaere klikkbart nar utfylling har startet', function() {
            expect(scope.erKlikkbar(1)).toBe(true);
            expect(scope.erKlikkbar(2)).toBe(true);
            expect(scope.erKlikkbar(3)).toBe(true);
        });
    });
});
