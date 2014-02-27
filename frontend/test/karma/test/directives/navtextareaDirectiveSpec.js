describe('navtextarea', function () {
    var element, scope;
    beforeEach(module('nav.textarea', 'nav.cmstekster', 'templates-main'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
        $provide.value("data", {});
    }));

    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope;

        element = angular.element(
            '<form name="form"> ' +
                    ' <div data-navtextarea data-navconfig' +
                    'data-nav-faktum="etfaktum"' +
                    'data-nokkel="ennokkel"' +
                    'data-maxlengde="500"' +
                    'data-navfeilmelding="feilmelding"' +
                    ' data-obligatorisk="true">' +
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

    describe("navtextarea", function () {
        it('harIkkFeil skal være true og feil false', function () {
        });
    });
});