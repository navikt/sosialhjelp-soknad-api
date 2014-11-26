describe('cmsvedlegg', function () {
    var element, compile, scope;

    beforeEach(module('nav.cms'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {
            'tittel.key': 'Min tittel',
            'input.value': 'Dette er value',
            'a.lenketekst': 'http://helt-riktig.com'
        }});
        $provide.value("data", {});
    }));


    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope;
        compile = $compile;
        scope.$apply();
    }));

    describe("cmsvedlegg", function() {
        it("hvis cmsvedlegg ikke har satt en verdi skal cmsProps kun opprette et object", function() {
            element = angular.element(
                '<div data-cmsvedlegg data-nav-faktum="etnavfaktum"></div>');

            compile(element)(scope);

            expect(typeof scope.cmsProps === 'object').toBe(true);
        });
        it("hvis cmsvedlegg har satt en verdi skal cmsProps.ekstra settes til denne verdien", function() {
            element = angular.element(
                '<div data-cmsvedlegg="enverdi" data-nav-faktum="etnavfaktum"></div>');

            compile(element)(scope);

            expect(typeof scope.cmsProps === 'object').toBe(true);
            expect(scope.cmsProps.ekstra).toBe("enverdi");
        });
    });
});