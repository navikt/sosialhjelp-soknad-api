describe('bodyDirectives', function () {
    var element;
    var $scope;

    beforeEach(module('nav.markup.bodydirective'));

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        $scope = $rootScope;
        element = angular.element('<div styling-helper>' +
                    '<div class="modalBoks">tester modalboks</div>' +
                '</div>');
        
        $compile(element)($scope);
        $scope.$apply();
        $timeout.flush(1);
    }));

    describe("stylingHelper", function() {
        it("should add class modalside på body", function() {
            expect(angular.element("body").hasClass('modalside')).toBe(true);
        });
    });
});