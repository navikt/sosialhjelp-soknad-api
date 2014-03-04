describe('stickybunn', function () {
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
                '<div data-bildenavigering data-vedlegg="forventning"' +
                'data-nav-template="bildenavigeringTemplateLiten.html">' +
                '</div>' +
                '</form>');

        $compile(element)($rootScope);

        $rootScope.$digest();
        $rootScope.$apply();

        scope = element.find('div').scope();
        scope.$apply();

    }));
    describe("Sist lagret", function () {
    });
});
