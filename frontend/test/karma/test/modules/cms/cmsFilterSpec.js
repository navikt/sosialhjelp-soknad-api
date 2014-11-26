describe('configUrl', function () {
    var element, compile, scope;

    beforeEach(module('nav.cms'));

    beforeEach(module(function ($provide) {
        $provide.value("data", {
            config: {
                'enurl.url': 'www.nav.no'
            }
        });
    }));


    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope;
        compile = $compile;
        scope.$apply();
    }));

    describe("configUrl", function() {
        it("hvis data.config inneholder urlen, skal denne returneres av filteret", function() {
            element = angular.element(
                '<div>{{"enurl" | configUrl }}</div>');

            compile(element)(scope);
            scope.$apply();

            expect(element.text()).toBe('www.nav.no');
        });
        it("hvis cmsvedlegg ikke har satt en verdi skal cmsProps kun opprette et object", function() {
            element = angular.element(
                '<div>{{"enurlsomikkefinnes" | configUrl }}</div>');

            compile(element)(scope);
            scope.$apply();

            expect(element.text()).toBe('');
        });
    });
});