angular.module('nav.input',['nav.cmstekster'])
    .directive('navradio', [function() {
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
    }])
    .directive('navButtonSpinner', [function() {
        return {
            restrict: "E",
            replace: true,
            scope: {
                laster: '=',
                klasse: '@',
                nokkel: '@',
                type: '@',
                click: '&'
            },
            templateUrl: '../js/app/directives/navinput/navbuttonspinnerTemplate.html'
        }
    }])

    .directive('navcheckbox', [function(){
        return {
            restrict: 'E',
            replace: true,
            scope: {

            },
            templateUrl: '../js/app/directives/navinput/navcheckboxTemplate.html'
        }
    }]);