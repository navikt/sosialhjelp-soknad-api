describe('stickyFeilmeldingToFeil', function () {
    var element, scope;
    beforeEach(module('nav.stickyFeilmelding', 'nav.cms', 'templates-main'));

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
                '   <div class="form-linje ikkefeil"><input type="text"></div>' +

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
            element.find('.feil').first().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(1);
        });
        it('alle feil er løst skal ikke stickyFeilmelding vises lengre og antallFeil være 0', function () {
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(2);
            element.find('.feil').first().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(1);
            element.find('.feil').first().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(0);
            expect(scope.skalVises()).toEqual(false);
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
        it('Nåværende feil skal settes til 0 etter andre forrige-kall pa rad', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            scope.forrige();
            expect(scope.feil.navaerende).toBe(0);
            scope.forrige();
            expect(scope.feil.navaerende).toBe(0);
        });
        it('Nåværende feil skal være 0 etter at den stod på andre feil og første feil ble rettet', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
            element.find('.feil').first().removeClass('feil');
            scope.$apply();
            expect(scope.feil.navaerende).toBe(0);
        });
        it('Nåværende feil skal være 0 etter at den stod på første feil og andre feil ble rettet', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            element.find('.feil').last().removeClass('feil');
            scope.$apply();
            expect(scope.feil.navaerende).toBe(0);
        });
        it('Nåværende feil skal være 0 etter at den stod på første feil og første feil ble rettet', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            element.find('.feil').first().removeClass('feil');
            scope.$apply();
            expect(scope.feil.navaerende).toBe(0);
        });
        it('Nåværende feil skal være 0 etter at den stod på andre feil og første feil ble rettet og det er en feil igjen', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
            element.find('.feil').first().removeClass('feil');
            scope.$apply();
            expect(scope.feil.navaerende).toBe(0);
        });
        it('varToFeilNaEnFeilStodPaAndreFeil trykker neste, navaerende være 0 og neste deaktivert', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(2);
            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
            element.find('.feil').first().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(1);
            expect(scope.feil.navaerende).toBe(0);
            scope.neste();
            expect(scope.feil.navaerende).toBe(0);
            expect(scope.skalDeaktivereNesteKnapp).toBe(true);
        });
        it('varToFeilNaEnFeilStodPaAndreFeil trykker neste to ganger, navaerende være 0 og neste deaktivert', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(2);
            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
            element.find('.feil').first().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(1);
            expect(scope.feil.navaerende).toBe(0);
            scope.neste();
            expect(scope.feil.navaerende).toBe(0);
            expect(scope.skalDeaktivereNesteKnapp).toBe(true);
            scope.neste();
            expect(scope.feil.navaerende).toBe(0);
            expect(scope.skalDeaktivereNesteKnapp).toBe(true);
        });
        it('varToFeilNaEnFeilStodPaAndreFeil trykker forrige, navaerende være 0, forrigeknapp deaktivert', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(2);
            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
            element.find('.feil').first().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(1);
            expect(scope.feil.navaerende).toBe(0);
            scope.forrige();
            expect(scope.feil.navaerende).toBe(0);
            expect(scope.skalDeaktivereForrigeKnapp()).toEqual(true);
        });
        it('varToFeilNaEnFeilStodPaAndreFeil trykker forrige to ganger, navaerende være 0, forrigeknapp deaktivert', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(2);
            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
            element.find('.feil').first().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(1);
            expect(scope.feil.navaerende).toBe(0);
            scope.forrige();
            expect(scope.feil.navaerende).toBe(0);
            expect(scope.skalDeaktivereForrigeKnapp()).toEqual(true);
            scope.forrige();
            expect(scope.feil.navaerende).toBe(0);
            expect(scope.skalDeaktivereForrigeKnapp()).toEqual(true);
        });

        it('varToFeilNaEnFeilStodPaForsteFeil trykker neste, navaerende være 0 og neste deaktivert', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(2);
            element.find('.feil').first().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(1);
            expect(scope.feil.navaerende).toBe(0);
            scope.neste();
            expect(scope.feil.navaerende).toBe(0);
            expect(scope.skalDeaktivereNesteKnapp).toBe(true);
        });
        it('varToFeilNaEnFeilStodPaForsteFeil trykker neste to ganger, navaerende være 0 og neste deaktivert', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(2);
            element.find('.feil').first().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(1);
            expect(scope.feil.navaerende).toBe(0);
            scope.neste();
            expect(scope.feil.navaerende).toBe(0);
            expect(scope.skalDeaktivereNesteKnapp).toBe(true);
            scope.neste();
            expect(scope.feil.navaerende).toBe(0);
            expect(scope.skalDeaktivereNesteKnapp).toBe(true);
        });
        it('varToFeilNaEnFeilStodPaForsteFeil trykker forrige, navaerende være 0, forrigeknapp deaktivert', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(2);
            element.find('.feil').first().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(1);
            expect(scope.feil.navaerende).toBe(0);
            scope.forrige();
            expect(scope.feil.navaerende).toBe(0);
            expect(scope.skalDeaktivereForrigeKnapp()).toEqual(true);
        });
        it('varToFeilNaEnFeilStodPaForsteFeil trykker forrige to ganger, navaerende være 0, forrigeknapp deaktivert', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(2);
            element.find('.feil').first().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(1);
            expect(scope.feil.navaerende).toBe(0);
            scope.forrige();
            expect(scope.feil.navaerende).toBe(0);
            expect(scope.skalDeaktivereForrigeKnapp()).toEqual(true);
            scope.forrige();
            expect(scope.feil.navaerende).toBe(0);
            expect(scope.skalDeaktivereForrigeKnapp()).toEqual(true);
        });
    });
    describe("stickyFeilmeldingAntallFeilDeaktivering", function () {
        it('Forrige knappen skal være dekativert hver gang leggTilStickyFeilmelding kjøres ', function () {
            scope.leggTilStickyFeilmelding();
            expect(scope.skalDeaktivereForrigeKnapp()).toBe(true);
        });
    });
});
describe('stickyFeilmeldingFireFeil', function () {
    var element, scope;
    beforeEach(module('nav.stickyFeilmelding', 'nav.cms', 'templates-main'));

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
                '   <div class="form-linje feil"><input type="text"></div>' +
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
        it('nestForsteOgForsteRettet trykker neste, navaerende være 1 og neste ikke deaktivert', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(5);
            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
            element.find('.feil').first().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(4);
            expect(scope.feil.navaerende).toBe(0);
            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
            expect(scope.skalDeaktivereNesteKnapp).toBe(false);
        });
        it('nestForsteOgForsteRettet trykker neste to ganger, navaerende være 2 og neste ikke deaktivert', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(5);
            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
            element.find('.feil').first().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(4);
            expect(scope.feil.navaerende).toBe(0);
            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
            scope.neste();
            expect(scope.feil.navaerende).toBe(2);
            expect(scope.skalDeaktivereNesteKnapp).toBe(false);
        });
        it('nestForsteOgForsteRettet trykker forrige, navaerende være 0 og forrige deaktivert', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(5);
            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
            element.find('.feil').first().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(4);
            expect(scope.feil.navaerende).toBe(0);
            scope.forrige();
            expect(scope.feil.navaerende).toBe(0);
            expect(scope.skalDeaktivereForrigeKnapp()).toBe(true);
        });
        it('nestForsteOgForsteRettet trykker forrige to ganger, navaerende være 0 og forrige deaktivert', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(5);
            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
            element.find('.feil').first().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(4);
            expect(scope.feil.navaerende).toBe(0);
            scope.forrige();
            expect(scope.feil.navaerende).toBe(0);
            expect(scope.skalDeaktivereForrigeKnapp()).toBe(true);
            scope.forrige();
            expect(scope.feil.navaerende).toBe(0);
            expect(scope.skalDeaktivereForrigeKnapp()).toBe(true);
        });
        it('sisteFeilBleRettetOgStodPaSisteFeil skal navarende være 4, neste deaktivert', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(5);
            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
            scope.neste();
            expect(scope.feil.navaerende).toBe(2);
            scope.neste();
            expect(scope.feil.navaerende).toBe(3);
            scope.neste();
            expect(scope.feil.navaerende).toBe(4);
            element.find('.feil').last().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(4);
            expect(scope.feil.navaerende).toBe(4);
            expect(scope.skalDeaktivereNesteKnapp).toBe(true);
        });
        it('sisteFeilBleRettetOgStodPaSisteFeil trykker neste, skal navarende være 4, neste deaktivert', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(5);
            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
            scope.neste();
            expect(scope.feil.navaerende).toBe(2);
            scope.neste();
            expect(scope.feil.navaerende).toBe(3);
            scope.neste();
            expect(scope.feil.navaerende).toBe(4);
            scope.neste();
            expect(scope.feil.navaerende).toBe(4);
            element.find('.feil').last().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(4);
            expect(scope.feil.navaerende).toBe(4);
            expect(scope.skalDeaktivereNesteKnapp).toBe(true);
            scope.neste();
            expect(scope.feil.navaerende).toBe(4);
            expect(scope.skalDeaktivereNesteKnapp).toBe(true);
        });
        it('sisteFeilBleRettetOgStodPaSisteFeil trykker forrige, skal navarende være 3, neste deaktivert', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(5);
            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
            scope.neste();
            expect(scope.feil.navaerende).toBe(2);
            scope.neste();
            expect(scope.feil.navaerende).toBe(3);
            scope.neste();
            expect(scope.feil.navaerende).toBe(4);
            scope.neste();
            expect(scope.feil.navaerende).toBe(4);
            element.find('.feil').last().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(4);
            expect(scope.feil.navaerende).toBe(4);
            expect(scope.skalDeaktivereNesteKnapp).toBe(true);
            scope.forrige();
            expect(scope.feil.navaerende).toBe(3);
            expect(scope.skalDeaktivereNesteKnapp).toBe(true);
        });
        it('sisteFeilBleRettetOgStodPaSisteFeil trykker forrige og så trykker neste, navaerende være 3, neste deaktivert', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(5);
            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
            scope.neste();
            expect(scope.feil.navaerende).toBe(2);
            scope.neste();
            expect(scope.feil.navaerende).toBe(3);
            scope.neste();
            expect(scope.feil.navaerende).toBe(4);
            scope.neste();
            expect(scope.feil.navaerende).toBe(4);
            element.find('.feil').last().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(4);
            expect(scope.feil.navaerende).toBe(4);
            expect(scope.skalDeaktivereNesteKnapp).toBe(true);
            scope.forrige();
            expect(scope.feil.navaerende).toBe(3);
            expect(scope.skalDeaktivereNesteKnapp).toBe(true);
            scope.neste();
            expect(scope.feil.navaerende).toBe(3);
            expect(scope.skalDeaktivereNesteKnapp).toBe(true);
        });
        it('sisteFeilBleRettetOgStodPaNestSisteFeil trykker neste, navaerende være 4, neste deaktivert', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(5);
            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
            scope.neste();
            expect(scope.feil.navaerende).toBe(2);
            scope.neste();
            expect(scope.feil.navaerende).toBe(3);
            element.find('.feil').last().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(4);
            expect(scope.feil.navaerende).toBe(3);
            scope.neste();
            expect(scope.feil.navaerende).toBe(4);
            expect(scope.skalDeaktivereNesteKnapp).toBe(true);
        });
        it('sisteFeilBleRettetOgStodPaNestSisteFeil trykker neste og neste, navaerende være 4, neste deaktivert', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(5);
            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
            scope.neste();
            expect(scope.feil.navaerende).toBe(2);
            scope.neste();
            expect(scope.feil.navaerende).toBe(3);
            element.find('.feil').last().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(4);
            expect(scope.feil.navaerende).toBe(3);
            scope.neste();
            expect(scope.feil.navaerende).toBe(4);
            scope.neste();
            expect(scope.feil.navaerende).toBe(4);
            expect(scope.skalDeaktivereNesteKnapp).toBe(true);
        });
        it('sisteFeilBleRettetOgStodPaNestSisteFeil trykker neste og så forrige, navaerende være 3, neste deaktivert', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(5);
            scope.neste();
            expect(scope.feil.navaerende).toBe(1);
            scope.neste();
            expect(scope.feil.navaerende).toBe(2);
            scope.neste();
            expect(scope.feil.navaerende).toBe(3);
            element.find('.feil').last().removeClass('feil');
            scope.$apply();
            expect(scope.feil.antallFeil).toBe(4);
            expect(scope.feil.navaerende).toBe(3);
            scope.neste();
            expect(scope.feil.navaerende).toBe(4);
            scope.forrige();
            expect(scope.feil.navaerende).toBe(3);
            expect(scope.skalDeaktivereNesteKnapp).toBe(true);
        });
        it('stodPaForsteFeilBleRettet trykker neste, mer enn to feil igjen ', function () {
            expect(scope.feil.navaerende).toBe(0);
            scope.leggTilStickyFeilmelding();
            expect(scope.feil.antallFeil).toBe(5);
            element.find('.feil').first().removeClass('feil');
            scope.$apply();
            scope.neste();
            expect(scope.feil.navaerende).toBe(0);
            expect(scope.skalDeaktivereNesteKnapp).toBe(false);
        });
    });
});
describe('stickyFeilmeldingFireFeil', function () {
    var element, scope;
    beforeEach(module('nav.stickyFeilmelding', 'nav.cms', 'templates-main'));

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
        it('enFeil, neste og forrige deaktiver, navarende 0, og antallfeil 1', function () {
            scope.leggTilStickyFeilmelding();
            expect(scope.skalDeaktivereNesteKnapp).toBe(true);
            expect(scope.skalDeaktivereForrigeKnapp()).toBe(true);
            expect(scope.feil.navaerende).toBe(0);
            expect(scope.feil.antallFeil).toBe(1);

        });
        it('enFeil neste, neste og forrige deaktivert, navarende 0, og antallfeil 1', function () {
            scope.leggTilStickyFeilmelding();
            scope.neste();
            expect(scope.skalDeaktivereNesteKnapp).toBe(true);
            expect(scope.skalDeaktivereForrigeKnapp()).toBe(true);
            expect(scope.feil.navaerende).toBe(0);
            expect(scope.feil.antallFeil).toBe(1);
        });
        it('enFeil forrige, neste og forrige deaktivert, navarende 0, og antallfeil 1', function () {
            scope.leggTilStickyFeilmelding();
            scope.forrige();
            expect(scope.skalDeaktivereNesteKnapp).toBe(true);
            expect(scope.skalDeaktivereForrigeKnapp()).toBe(true);
            expect(scope.feil.navaerende).toBe(0);
            expect(scope.feil.antallFeil).toBe(1);
        });
    });
});