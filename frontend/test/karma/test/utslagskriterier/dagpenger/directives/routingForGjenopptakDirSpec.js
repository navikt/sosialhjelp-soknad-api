describe('sporsmalferdig', function () {
    var rootScope, element, scope, form, inputEl;

    beforeEach(module('nav.routingForGjenopptakModule', 'nav.cms', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("data", {

        });
        $provide.value("cms", {'tekster': {'hjelpetekst.tittel': 'Tittel hjelpetekst',
            'hjelpetekst.tekst': 'Hjelpetekst tekst' }
        });
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        scope = $rootScope;
        element = angular.element(
            '<form name="form">' +
                '<input type="text" name="inputel" ng-model="test" data-ng-required="true">' +
                '<div data-routing-for-gjenopptak-button></div> ' +
                '</div>' +
                '</form>');

        scope.valider = function (key) {
        };

        scope.gjenopptak = {
            harMotattDagpenger: true,
            harArbeidet: true
        };

        $compile(element)(scope);
        scope.$apply();
    }));

    describe('validerOgStartSoknad', function () {
        it('når skjemaet er validert skal cookie settes', function () {
            scope.test = "validerer formen";
            window.redirectTilUrl = jasmine.createSpy('Redirect URL spy');
            element.scope().$apply();
            scope.validerOgStartSoknad();
            expect(getCookie('routingGjenopptak')).toBeDefined();
        });
        it('ikke har motatt dagpenger så skal man redirectes til sendsoknad', function () {
            scope.test = "validerer formen";
            window.redirectTilUrl = jasmine.createSpy('Redirect URL spy');
            element.scope().$apply();
            scope.validerOgStartSoknad();

            expect(window.redirectTilUrl).toHaveBeenCalledWith('skjema/NAV04-01.03#/informasjonsside');
        });
        it('ikke har motatt dagpenger så skal man redirectes til sendsoknad', function () {
            scope.gjenopptak.harMotattDagpenger = "ja";

            scope.test = "validerer formen";
            window.redirectTilUrl = jasmine.createSpy('Redirect URL spy');
            element.scope().$apply();

            scope.validerOgStartSoknad();

            expect(window.redirectTilUrl).toHaveBeenCalledWith('skjema/NAV04-16.03#/informasjonsside');
        });
    });
});
