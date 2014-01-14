angular.module('nav.input', ['nav.cmstekster'])
    .directive('navconfig', function ($parse) {
        return {
            link: {
                pre: function (scope, element, attr) {
                    for (key in attr) {
                        if (key.indexOf('nav') == 0 && key != 'navconfig') {
                            scope[key] = attr[key];
                        }
                    }
                }}
        }
    })
    .directive('navradio', [function () {
        return {
            restrict: "A",
            replace: true,
            scope: true,
            link: {
                pre: function (scope, element, attr) {
                    scope.value = attr.value;
                    scope.label = attr.label;
                },
                post: function (scope, element, attr) {
                    scope.hvisSynlig = function () {
                        return element.is(':visible');
                    };

                    scope.endret = function () {
                        scope.$eval(attr.navendret);
                    }
                }
            },
            templateUrl: '../js/common/directives/navinput/navradioTemplate.html'
        }
    }])
    .directive('navcheckbox', ['cms', function (cms) {
        return {
            restrict: "A",
            replace: true,
            transclude: true,
            scope: true,
            link: {
                pre: function (scope, elem, attr) {
                    scope.hjelpetekst = {
                        tittel: cms.tekster[attr.navlabel + '.hjelpetekst.tittel'],
                        tekst: cms.tekster[attr.navlabel + '.hjelpetekst.tekst']
                    }

                },
                post: function (scope, element) {
                    scope.hvisHarHjelpetekst = function () {
                        return scope.hjelpetekst.tittel && scope.hjelpetekst.tekst;
                    }

                    scope.hvisHuketAv = function () {
                        var transcludeElement = element.find('.ng-transclude');
                        return checkTrue(scope.faktum.value) && transcludeElement.text().trim().length > 0;
                    }
                    scope.endret = function () {
                        scope.$eval(scope.navendret);
                    }

                }},
            templateUrl: '../js/common/directives/navinput/navcheckboxTemplate.html'
        }
    }])

    .directive('navtekst', [function () {
        return {
            restrict: "A",
            replace: true,
            scope: true,
            link: {
                pre: function (scope, element, attrs) {
                    if (attrs.regexvalidering) {
                        scope.regexvalidering = attrs.regexvalidering.toString();
                    } else {
                        scope.regexvalidering = "";
                    }
                    if (attrs.inputfeltmaxlength) {
                        scope.inputfeltmaxlength = attrs.inputfeltmaxlength;
                    } else {
                        scope.inputfeltmaxlength = undefined;
                    }
                },
                post: function (scope, element, attrs, ctrl) {
                    scope.hvisSynlig = function () {
                        return element.is(':visible');
                    }
                }
            },
            templateUrl: '../js/common/directives/navinput/navtekstTemplate.html'
        }
    }]).directive('tekstfeltPatternvalidering', ['$timeout', function ($timeout) {
        return {
            require: 'ngModel',
            link: function (scope, element, attrs, ctrl) {

                element.bind('blur', function () {
                    if (ctrl.$valid) {
                        scope.lagreFaktum();
                    }
                })
            }
        }
    }])

    .directive('navorganisasjonsnummerfelt', [function () {
        return {
            restrict: "A",
            replace: true,
            scope: true,
            link: function (scope, element, attrs) {
                scope.erSynlig = function () {
                    return element.is(':visible');
                }
            },
            templateUrl: '../js/common/directives/navinput/navorgnrfeltTemplate.html'
        }
    }]).directive('orgnrValidate', [function () {
        return {
            require: 'ngModel',
            link: function (scope, element, attrs, ctrl) {
                element.bind('blur', function () {
                    if (ctrl.$valid) {
                        scope.lagreFaktum();
                    }
                })

                scope.formateringsfeil = function () {
                    return ctrl.$error.pattern;
                }

                scope.harRequiredFeil = function () {
                    return ctrl.$error.required;
                }
            }
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
            templateUrl: '../js/common/directives/navinput/navbuttonspinnerTemplate.html'
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
