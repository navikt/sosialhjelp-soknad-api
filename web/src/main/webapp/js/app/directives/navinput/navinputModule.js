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
    .directive('navcheckbox', [function() {
        return {
            restrict: "E",
            replace: true,
            scope: {
                model: '=',
                name: '@',
                label: '@'
            },
            templateUrl: '../js/app/directives/navinput/navcheckboxTemplate.html'
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

    .directive('booleanVerdi', [function(){
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function(scope, element, attr, ngModel){
                function fraTekst(tekst) {
                    if(tekst === "true") {
                        return true;
                    }
                    return false;
                }
                ngModel.$formatters.push(fraTekst);
            }
        };
    }]);
