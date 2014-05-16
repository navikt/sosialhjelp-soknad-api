angular.module('nav.truncatehover', [])
    .directive('truncateHover', ['$window', '$timeout', function($window, $timeout) {
        return function(scope, element) {
            $($window).bind('resize', function() {
                settHoverText();
            });

            $timeout(function() {
                settHoverText();
            });


            function settHoverText() {
                if (element.width() < element[0].scrollWidth) {
                    element.attr('title', element.text());
                } else {
                    element.removeAttr('title');
                }
            }
        };
    }]);