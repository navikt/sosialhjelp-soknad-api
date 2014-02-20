describe('navTittel', function () {
    var element;
    var $scope;

    beforeEach(module('nav.skjematittel', 'nav.cmstekster', 'templates-main'));

     beforeEach(module(function ($provide) {
            $provide.value("cms", {'tekster': {'tittel.key': 'Min tittel'}});
            $provide.value("data", {});
        }));

    beforeEach(inject(function ($compile, $rootScope) {
        $scope = $rootScope;
        $scope.tittelkey = "tittel.key";
        element = angular.element('<div nav-tittel="{{tittelkey}}"></div>');
        $compile(element)($scope);
        $scope.$apply();
    }))

    it("should add class aria-hidden", function() {
        expect(element.text().trim()).toBe("Min tittel");
    })
   
});

