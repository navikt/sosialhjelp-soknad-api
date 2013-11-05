angular.module('nav.markup.panelbelyst', [])
    .directive('panelbelyst', [function () {
        return {
            restrict: 'EA',
            replace: true,
            transclude: true,
            templateUrl: '../js/app/directives/markup/panelStandardBelystTemplate.html'
        }
    }]);