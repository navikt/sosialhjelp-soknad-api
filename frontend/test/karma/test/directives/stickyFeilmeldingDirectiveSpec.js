describe('stickyFeilmelding', function () {
    var element, scope;
    beforeEach(module('nav.stickyFeilmelding', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
        $provide.value("data", {});
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope;

        element = angular.element(
            '<form name="form"> ' +
                '<div data-sticky-feilmelding></div>' +
                '<div data-accordion-group class="spm-blokk open">' +
                '   <div class="form-linje feil"><input type="text"></div>' +
                '   <div class="form-linje feil"><input type="text"></div>' +
                '</div>' +
            '</form>');

        scope.apneTab = function () {
        };
        scope.lukkTab = function () {
        };

        $compile(element)(scope);
        scope.$digest();
        scope.$apply();
    }));

    describe("stickyFeilmeldingAntallFeil", function () {
        it('stickyFeilmelding skal ikke vises på oppstart av soknaden', function () {
            expect(scope.feil.skalViseStickyFeilmeldinger).toEqual(false);
        });
        it('stickyFeilmelding skal vises når leggTilStickyFeilmelding blir kjørt', function () {
            expect(scope.feil.skalViseStickyFeilmeldinger).toEqual(false);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.skalViseStickyFeilmeldinger).toEqual(true);
        });
        it('antallFeil skal være 2 nar antall elementer med klasse feil er 2', function () {
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(2);
        });
        it('antallFeil skal være 1 mindre nar en feil blir løst', function () {
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(2);
            console.log(element.find('.feil').first());
            element.find('.feil').first().removeClass('feil');
            console.log(element.find('.feil').first());

            expect(scope.feil.antallFeil).toBe(1);
        });
    });
    describe("stickyFeilmeldingNaverende", function () {
        it('Nåværende feil skal være 0 når soknaden startes ', function () {
            expect(scope.feil.navaerende).toEqual(0);
        });
        it('Nåværende feil skal settes til 0 hver gang leggTilStickyFeilmelding kjøres ', function () {
            expect(scope.feil.navaerende).toBe(0);

            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.navaerende).toEqual(0);
        });
        it('Nåværende feil skal settes til 1 etter første neste-kall ', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
        });
        it('Nåværende feil skal settes til 0 etter første forrige-kall ', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            scope.forrige();
            expect(scope.feil.navaerende).toBe(0);
        });
    });
    describe("stickyFeilmeldingAntallFeilDeaktivering", function () {
        it('Forrige knappen skal være dekativert hver gang leggTilStickyFeilmelding kjøres ', function () {
            scope.leggTilStickyFeilmelding();
            expect(scope.skalDeaktivereForrigeKnapp()).toBe(true);
        });
    });
});