angular.module('nav.markup.navinfoboks', [])
    .directive('navinfoboks', [function () {
        return {
            restrict: 'A',
            replace: true,
            transclude: true,
            templateUrl: '../js/common/directives/markup/navinfoboksTemplate.html'
        };
    }]);