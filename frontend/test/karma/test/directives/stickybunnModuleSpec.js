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
                '<div data-sist-lagret data-navtilbakelenke="vedlegg"></div>' +
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
        it('Hvis navtilbakelenke inneholder vedlegg skal scope.lenge.value gå til vedleggsiden', function () {
            expect(scope.lenke.value).toEqual('#/vedlegg');
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
                soknadId: 1
            }
        });
    }));


    beforeEach(inject(function ($compile, $rootScope) {
        element = angular.element(
            '<form name="form"> ' +
                '<div data-sist-lagret data-navtilbakelenke="soknad"></div>' +
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
        it('Hvis navtilbakelenke inneholder soknad skal scope.lenke.value gå til soknadsiden', function () {
            expect(scope.lenke.value).toEqual('#/soknad');
        });
    });
});
describe('stickybunnUtenLenketekst', function () {
    var scope, element;

    beforeEach(module('nav.stickybunn', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
        $provide.value("data", {
            soknad: {
                soknadId: 1
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
        it('Hvis navtilbakelenke ikke inneholder soknad eller vedlegg skal scope.lenke.value vaere en tom string', function () {
            expect(scope.lenke.value).toEqual('');
        });
    });
});