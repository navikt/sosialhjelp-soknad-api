angular.module('nav.markup.navinfoboks', [])
    .directive('navinfoboks', [function () {
        return {
            restrict: 'EA',
            scope: {
            	nokkel: '@'
            },
            replace: true,
            transclude: true,
            templateUrl: '../js/app/directives/markup/navinfoboksTemplate.html'
        }
    }]);