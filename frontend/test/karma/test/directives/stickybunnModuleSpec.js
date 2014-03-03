describe('stickybunn', function () {
    var scope, element;

    beforeEach(module('nav.stickybunn', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
        $provide.value("data", {
            soknad: {
                soknadId: 1,
                sistLagret: new Date("2014-01-01")
            }
        });
    }));


    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element(
            '<form name="form"> ' +
                '<div data-sist-lagret data-navtilbakelenke="ingenLenke"></div>' +
                '</div>' +
                '</form>');

        $compile(element)($rootScope);

        $rootScope.$digest();
        $rootScope.$apply();

        scope = element.find('div').isolateScope();
        scope.$apply();

    }));
    describe("Sist lagret", function () {
        it('Med sist lagret 01.01.2014 kl 12.00  så skal hentSistLagreTid returnere det samme', function () {
            expect(scope.hentSistLagretTid()).toEqual(new Date("2014-01-01"));
        });
        it('Hvis soknaden har blitt lagret sa skal soknadHarBlittLagret returnere true', function () {
            expect(scope.soknadHarBlittLagret()).toEqual(true);
        });
        it('Hvis soknaden har blitt lagret sa skal soknadHarAldriBlittLagret returnere false', function () {
            expect(scope.soknadHarAldriBlittLagret()).toEqual(false);
        });
    });
});
describe('stickybunnUtensistLagret', function () {
    var scope, element;

    beforeEach(module('nav.stickybunn', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
        $provide.value("data", {
            soknad: {
                soknadId: 1,
            }
        });
    }));


    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element(
            '<form name="form"> ' +
                '<div data-sist-lagret data-navtilbakelenke="ingenLenke"></div>' +
                '</div>' +
                '</form>');

        $compile(element)($rootScope);

        $rootScope.$digest();
        $rootScope.$apply();

        scope = element.find('div').isolateScope();
        scope.$apply();

    }));
    describe("Sist lagret", function () {
        it('hvis soknad ikke har blitt lagret skal hentSistLagreTid returnere null', function () {
            expect(scope.hentSistLagretTid()).toEqual(null);
        });
        it('Hvis soknaden ikke har blitt lagret sa skal soknadHarBlittLagret returnere false', function () {
            expect(scope.soknadHarBlittLagret()).toEqual(false);
        });
        it('Hvis soknaden har blitt lagret sa skal soknadHarAldriBlittLagret returnere true', function () {
            expect(scope.soknadHarAldriBlittLagret()).toEqual(true);
        });
    });
});