describe('avbrytHelperDirective', function () {
    var element;
    var $scope;

    beforeEach(module('nav.markup.bodydirective'));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        $scope = $rootScope;
        element = angular.element('<div avbryt-helper>' +
                    '<div class="avbryt-boks">tester modalboks</div>' +
                '</div>');
        
        $compile(element)($scope);
        $scope.$apply();
        $timeout.flush(1);
    }));

    describe("avbrytHelper", function() {
        it("should add class modalside på body", function() {
            expect(angular.element("body").attr("id")).toEqual('avbryt-side');
        });
    });
});