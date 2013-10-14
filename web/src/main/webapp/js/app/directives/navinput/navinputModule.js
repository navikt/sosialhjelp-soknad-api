angular.module('nav.input',['nav.cmstekster'])
    .directive('navradio', function() {
        return {
            restrict: "E",
            replace: true,
            scope: {
                model: '=',
                value: '@',
                name: '@',
                label: '@'
            },
            templateUrl: '../js/app/directives/navinput/navradioTemplate.html'
        }
    });
