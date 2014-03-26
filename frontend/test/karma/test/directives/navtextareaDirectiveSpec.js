describe('navtextarea', function () {
    var element, scope, timeout;
    beforeEach(module('nav.textarea', 'nav.cmstekster', 'templates-main', 'ngSanitize'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'ennokkel.sporsmal': 'Et sporsmal'}});
        $provide.value("data", {});
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        element = angular.element(
            '<form name="form"> ' +
                    ' <div data-navtextarea data-navconfig' +
                    'data-nav-faktum="etfaktum"' +
                    'data-nokkel="ennokkel"' +
                    'data-maxlengde="500"' +
                    'data-navfeilmelding="feilmelding"' +
                    'data-obligatorisk="true">' +
                    '</div>' +
            '</form>');

        $compile(element)($rootScope);

        $rootScope.$apply();

        timeout = $timeout;
        scope = element.find('div').scope();
        scope.lagreFaktum = function() {};

    }));

    describe("navtextarea", function () {
        it('attributtene skal bli satt riktig', function () {
            expect(scope.nokkel).toBe("ennokkel");
            expect(scope.sporsmal).toBe("ennokkel.sporsmal");
            expect(scope.label).toBe("ennokkel.label");
            expect(scope.feilmelding).toBe("ennokkel.feilmelding");
            expect(scope.tellertekst).toBe("ennokkel.tellertekst");
            expect(scope.maxlengde).toBe("500");
            expect(scope.counter).toBe("500");
        });
        it('harIkkFeil skal være true og feil false', function () {
            expect(scope.feil).toBe(false);
            expect(scope.harIkkeFeil).toBe(true);
        });
        it('harFokusOgFeil skal returnere false når textarea hverken har feil eller fokus', function () {
            expect(scope.harFokusOgFeil()).toBe(false);
        });
        it('harFokusOgFeil skal returnere true når textarea har feil og ikke fokus', function () {
            scope.feil = true;
            expect(scope.harFokusOgFeil()).toBe(true);
        });
        it('harFokusOgFeil skal returnere true når textarea ikke har feil men har fokus', function () {
            spyOn(scope, 'lagreFaktum');
            element.find('textarea').blur();

            expect(scope.lagreFaktum).toHaveBeenCalled();
        });
        it('harSporsmal skal returnere true når textarea er knyttet til et spørsmal', function () {
            expect(scope.harSporsmal()).toBe(true);
        });
    });
});
describe('navtextareaUtenSporsmal', function () {
    var element, scope;
    beforeEach(module('nav.textarea', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'ikkeetsporsmal': ''}});
        $provide.value("data", {});
    }));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        element = angular.element(
            '<form name="form"> ' +
                ' <div data-navtextarea data-navconfig' +
                'data-nav-faktum="etfaktum"' +
                'data-nokkel="ennokkel"' +
                'data-maxlengde="500"' +
                'data-navfeilmelding="feilmelding">'+
                '</div>' +
                '</form>');

        $compile(element)($rootScope);

        $rootScope.$apply();
        $timeout.flush();

        scope = element.find('div').scope();
    }));

    describe("navtextarea", function () {
        it('harSporsmal skal returnere false når textarea ikke er knyttet til et spørsmal', function () {
            expect(scope.harSporsmal()).toBe(false);
        });
    });
});