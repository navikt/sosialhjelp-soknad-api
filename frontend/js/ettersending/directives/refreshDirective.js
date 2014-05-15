angular.module('ettersending.refresh', [])
    .directive('refresh', function($timeout) {
        return function(scope, element) {
            element.css('right', '0');
            $timeout(function() {
                element.css('right', 'auto');
            }, 50);
        };
    });