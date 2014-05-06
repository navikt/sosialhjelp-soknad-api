describe('nav.fokus', function () {
    var element;
    var $scope;

    beforeEach(module('nav.fokus'));

    var TAB_BUTTON = 9;
    var triggerKey = function(element, keyCode, type) {
        var e = $.Event(type);
        e.which = keyCode;
        element.trigger(e);
    };

    beforeEach(inject(function ($compile, $rootScope, $timeout) {
        $scope = $rootScope;
        element = angular.element('<div>' +
            '<div class="dagpenger" tab-autoscroll>' +
                '<div class="sticky-feilmelding">' +
                    '<div data-sticky-feilmelding></div>' +
                '</div>' +
                '<input type="text">' + 
                '<input type="textarea">' + 
            '</div>' +
            '<div><div class="sticky-bunn"></div></div></div>');
        
        $compile(element)($scope);
        element.find("input").focus();
        $scope.$apply();
    }));

    describe("stylingHelper", function() {
        it("skal scrolle til første element med feil", function() {
            spyOn(window, "scrollToElement"); 
            triggerKey(element.find(".dagpenger"), TAB_BUTTON, "keydown");
            triggerKey(element.find(".dagpenger"), TAB_BUTTON, "keyup");

            expect(window.scrollToElement).toHaveBeenCalled();           
        });
    });
});