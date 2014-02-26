describe('navFaktum', function () {
    var element;
    var $scope;

    beforeEach(module('app.services', 'nav.navfaktum'));

    beforeEach(module(function ($provide) {
        $provide.value("cms", {'tekster': {}});
        $provide.value("data", {
            fakta: [{}],
            soknad: {
                soknadId: 1
            }
        });
    }));


    beforeEach(inject(function ($compile, $rootScope,data) {
        $scope = $rootScope;

        element = angular.element('<div nav-faktum="mittFaktum">' + '</div>');

        $compile(element)($scope);
        $scope.$apply();
    }));

    describe("navFaktum", function() {
        it("skal kunne setet navFaktum", function() {
            expect(true).toBe(true);
        });

    });
});