angular.module('nav.input', ['nav.cmstekster'])
    .directive('navradio', [function () {
        return {
            restrict: "A",
            replace: true,
            require: 'ngModel',
            scope: {
                model: '=ngModel',
                value: '@',
                modus: '=',
                inputname: '@',
                label: '@',
                feilmelding: '@'
            },
            link: function (scope, element) {
                var tmpElementName = 'tmpName';
                fiksNavn(element, scope.inputname, tmpElementName);

                scope.hvisSynlig = function () {
                    return element.is(':visible');
                }

                scope.hvisIRedigeringsmodus = function () {
                    return scope.modus;
                }

                scope.hvisIOppsummeringsmodusOgRadioErValgt = function () {

                    return !scope.hvisIRedigeringsmodus() && scope.model == scope.value.toString();
                }

            },
            templateUrl: '../js/app/directives/navinput/navradioTemplate.html'
        }
    }])
    .directive('navcheckbox', [function () {
        return {
            restrict: "A",
            replace: true,
            transclude: true,
            require: 'ngModel',
            scope: {
                model: '=ngModel',
                modus: '=',
                inputname: '@',
                label: '@',
                hjelpetekst: '@',
                endret: '&'
            },
            link: function (scope, element) {
//                var tmpElementName = 'tmpName';
//                fiksNavn(element, scope.inputname, tmpElementName);

                scope.hvisHarHjelpetekst = function() {
                    return scope.tittel && scope.tekst;
                }

                scope.hvisIRedigeringsmodus = function () {
                    return scope.modus;
                }

                scope.hvisIOppsummeringsmodusOgChecked = function () {
                    return !scope.hvisIRedigeringsmodus() && checkTrue(scope.model);
                }

                scope.hvisHuketAv = function () {
                    var transcludeElement = element.find('.ng-transclude');
                    return checkTrue(scope.model) && transcludeElement.text().length > 0;
                }
            },
            templateUrl: '../js/app/directives/navinput/navcheckboxTemplate.html'
        }
    }])

    .directive('navtekst', [function () {
        return {
            restrict: "A",
            replace: true,
            require: 'ngModel',
            scope: {
                model: '=ngModel',
                modus: '=',
                inputname: '@',
                label: '@',
                feilmelding: '@'
            },
            link: function (scope, element) {
                var tmpElementName = 'tmpName';
                fiksNavn(element, scope.inputname, tmpElementName);

                scope.hvisIRedigeringsmodus = function () {
                    return scope.modus;
                }

                scope.hvisIOppsummeringsmodus = function () {
                    return !scope.hvisIRedigeringsmodus();
                }

                scope.hvisSynlig = function () {
                    return element.is(':visible');
                }
            },
            templateUrl: '../js/app/directives/navinput/navtekstTemplate.html'
        }
    }])

    .directive('navButtonSpinner', [function () {
        return {
            restrict: "A",
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

    .directive('booleanVerdi', [function () {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, element, attr, ngModel) {
                function fraTekst(tekst) {
                    if (tekst === "true") {
                        return true;
                    }
                    return false;
                }

                function tilTekst(booleanVerdi) {
                    if (booleanVerdi) {
                        return "true";
                    }
                    return "false";
                }

                ngModel.$formatters.push(fraTekst);
                ngModel.$parsers.push(tilTekst);
            }
        };
    }]);
