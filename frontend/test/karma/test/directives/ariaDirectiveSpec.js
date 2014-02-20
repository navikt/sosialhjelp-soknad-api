describe('ariaModule', function () {
    var element;
    var $scope;

    beforeEach(module('nav.aria'));

    beforeEach(inject(function ($compile, $rootScope) {
        $scope = $rootScope;
        $scope.isOpen = true;
        element = angular.element('<div nav-aria-expanded="isOpen" nav-aria-hidden="!isOpen">tester aria</div>');
        $compile(element)($scope);
        $scope.$apply();
    }))

    describe("navAriaHidden", function() {
        it("should add class aria-hidden", function() {
            $scope.$apply();
            $scope.$digest();
            $scope.isOpen = false;
            $scope.$apply();
            expect(element.attr("aria-hidden")).toBe("true");
        })
    })

     describe("navAriaHidden", function() {
        it("should add class aria-hidden", function() {
            $scope.isOpen = false;
            $scope.$apply();
            expect(element.attr("aria-expanded")).toBe("false");
        })
    })
});

