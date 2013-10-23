angular.module('nav.input',['nav.cmstekster'])
    .directive('navradio', [function() {
        return {
            restrict: "E",
            replace: true,
            require: 'ngModel',
            scope: {
                model: '=ngModel',
                value: '@',
                inputname: '@',
                label: '@'
            },
            link: function(scope, element, attr, ctrl) {
                scope.hvisSynlig = function() {
                    // Potensiell stygg hack for å kunne hente ut hvilket inputfelt som gir feil... :|
                    if (element.is(':visible') && (scope.model == undefined)) {
                        ctrl.$setValidity(scope.inputname, false);
                    } else {
                        ctrl.$setValidity(scope.inputname, true);
                    }
                    return false;
                }

            },
            templateUrl: '../js/app/directives/navinput/navradioTemplate.html'
        }
    }])
    .directive('navcheckbox', [function() {
        return {
            restrict: "E",
            replace: true,
            transclude: true,
            require: 'ngModel',
            scope: {
                model: '=ngModel',
                modus: '=',
                inputname: '@',
                label: '@',
                endret: '&'
            },
            controller: function($scope) {
                $scope.hvisIRedigeringsmodus = function() {
                    return $scope.modus;
                }

                $scope.hvisIOppsummeringsmodusOgChecked = function () {
                    return !$scope.hvisIRedigeringsmodus() && checkTrue($scope.model);
                }

                $scope.hvisHuketAv = function() {
                    return checkTrue($scope.model);
                }
            },
            templateUrl: '../js/app/directives/navinput/navcheckboxTemplate.html'
        }
    }])

    .directive('navtekst', [function() {
        return {
            restrict: "E",
            replace: true,
            require: 'ngModel',
            scope: {
                model: '=ngModel',
                modus: '=',
                inputname: '@',
                label: '@'
            },
            link: function(scope, element, attr, ctrl) {
                scope.hvisIRedigeringsmodus = function() {
                    return scope.modus;
                }

                scope.hvisIOppsummeringsmodus = function () {
                    return !scope.hvisIRedigeringsmodus();
                }

                scope.hvisSynlig = function() {
                    // Potensiell stygg hack for å kunne hente ut hvilket inputfelt som gir feil... :|
                    if (element.is(':visible') && (scope.model == undefined || scope.model == "")) {
                        ctrl.$setValidity(scope.inputname, false);
                    } else {
                        ctrl.$setValidity(scope.inputname, true);
                    }
                    return false;
                }

            },
            templateUrl: '../js/app/directives/navinput/navtekstTemplate.html'
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

                function tilTekst(booleanVerdi) {
                    if(booleanVerdi) {
                        return "true";
                    }
                    return "false";
                }

                ngModel.$formatters.push(fraTekst);
                ngModel.$parsers.push(tilTekst);
            }
        };
    }]);
