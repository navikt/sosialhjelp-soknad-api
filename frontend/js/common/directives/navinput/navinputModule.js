angular.module('nav.input', ['nav.cmstekster'])
    .directive('navconfig', [function () {
        return {
            link: {
                pre: function (scope, element, attr) {
                    for (var key in attr) {
                        if (key.indexOf('nav') === 0 && key !== 'navconfig') {
                            scope[key] = attr[key];
                        }
                    }
                }}
        };
    }])
    .directive('navradio', ['cms', function (cms) {
        return {
            restrict: 'A',
            replace: true,
            scope: true,
            transclude: true,
            link: {
                pre: function (scope, element, attr) {
                    scope.value = attr.value;
                    scope.hjelpetekst = {
                        tittel: attr.hjelpetekst + '.tittel',
                        tekst: attr.hjelpetekst + '.tekst'
                    };
                },
                post: function (scope, element, attr) {
                    scope.hvisAktiv = function () {
                        return scope.faktum.value === scope.value;
                    };

                    scope.hvisHarTranscludedInnhold = function () {
                        var transcludeElement = element.find('.ng-transclude');
                        return transcludeElement.text().trim().length > 0;
                    };

                    scope.hvisHarHjelpetekst = function() {
                        return cms.tekster[scope.hjelpetekst.tittel] !== undefined;
                    };

                    scope.name = scope.navlabel.substr(0, scope.navlabel.lastIndexOf("."));
                }
            },
            templateUrl: '../js/common/directives/navinput/navradioTemplate.html'
        };
    }])
    .directive('navradioUtenfaktum', ['cms', function (cms) {
        return {
            restrict: 'A',
            replace: true,
            scope: {
                radiomodel: '=',
                navlabel: '@',
                navfeilmelding: '@'
            },
            transclude: true,
            link: {
                pre: function (scope, element, attr) {
                    scope.value = attr.value;
                    scope.hjelpetekst = {
                        tittel: attr.hjelpetekst + '.tittel',
                        tekst: attr.hjelpetekst + '.tekst'
                    };
                },
                post: function (scope, element, attr) {

                    scope.hvisHarTranscludedInnhold = function () {
                        var transcludeElement = element.find('.ng-transclude');
                        return transcludeElement.text().trim().length > 0;
                    };

                    scope.hvisHarHjelpetekst = function() {
                        return cms.tekster[scope.hjelpetekst.tittel] !== undefined;
                    };

                    scope.name = scope.navlabel.substr(0, scope.navlabel.lastIndexOf("."));
                }
            },
            templateUrl: '../js/common/directives/navinput/navradioUtenfaktumTemplate.html'
        };
    }])
    .directive('navcheckbox', ['cms', function (cms) {
        return {
            restrict: 'A',
            replace: true,
            transclude: true,
            scope: true,
            link: {
                pre: function (scope, elem, attr) {
                    scope.hjelpetekst = {
                        tittel: cms.tekster[attr.navlabel + '.hjelpetekst.tittel'],
                        tekst: cms.tekster[attr.navlabel + '.hjelpetekst.tekst']
                    };
                },
                post: function (scope, element) {
                    scope.hvisHarHjelpetekst = function () {
                        return scope.hjelpetekst.tittel && scope.hjelpetekst.tekst;
                    };

                    scope.hvisHuketAv = function () {
                        return checkTrue(scope.faktum.value);
                    };

                    scope.hvisHarTranscludedInnhold = function () {
                        var transcludeElement = element.find('.ng-transclude');
                        return transcludeElement.text().trim().length > 0;
                    };

                    scope.endret = function () {
                        scope.$eval(scope.navendret);
                    };
                }},
            templateUrl: '../js/common/directives/navinput/navcheckboxTemplate.html'
        };
    }])
    .directive('navtekst', [function () {
        return {
            restrict: 'A',
            replace: true,
            scope: true,
            link: {
                pre: function (scope, element, attrs) {
                    if (attrs.regexvalidering) {
                        scope.regexvalidering = attrs.regexvalidering.toString();
                    } else {
                        scope.regexvalidering = '';
                    }
                    if (attrs.inputfeltmaxlength) {
                        scope.inputfeltmaxlength = attrs.inputfeltmaxlength;
                    } else {
                        scope.inputfeltmaxlength = undefined;
                    }
                },
                post: function (scope, element) {
                    scope.harSporsmal = function() {
                        return isNotNullOrUndefined(scope.navsporsmal) && scope.navsporsmal.length > 0;
                    };
                }
            },
            templateUrl: '../js/common/directives/navinput/navtekstTemplate.html'
        };
    }])
    .directive('navtall', [function () {
        return {
            restrict: 'A',
            replace: true,
            scope: true,
            link: {
                pre: function (scope, element, attrs) {
                    if (attrs.regexvalidering) {
                        scope.regexvalidering = attrs.regexvalidering.toString();
                    } else {
                        scope.regexvalidering = '';
                    }
                    if (attrs.inputfeltmaxlength) {
                        scope.inputfeltmaxlength = attrs.inputfeltmaxlength;
                    } else {
                        scope.inputfeltmaxlength = undefined;
                    }
                },
                post: function (scope, element) {
                    scope.harSporsmal = function() {
                        return isNotNullOrUndefined(scope.navsporsmal) && scope.navsporsmal.length > 0;
                    };
                }
            },
            templateUrl: '../js/common/directives/navinput/navtallTemplate.html'
        };
    }])
    .directive('tekstfeltPatternvalidering', [function () {
        return {
            require: 'ngModel',
            link: function (scope, element, attrs, ctrl) {
                element.bind('blur', function () {
                    if (ctrl.$valid) {
                        scope.lagreFaktum();
                    }
                });
            }
        };
    }])

    .directive('navorganisasjonsnummerfelt', [function () {
        return {
            restrict: "A",
            replace: true,
            scope: true,
            link: function (scope, element) {
                scope.visSlett = function(idx) {
                    if (scope.navVisSlett !== undefined && scope.navVisSlett === 'false') {
                        return false;
                    }
                    return idx !== 0;
                };
            },
            templateUrl: '../js/common/directives/navinput/navorgnrfeltTemplate.html'
        };
    }]).directive('orgnrValidate', [function () {
        return {
            require: 'ngModel',
            link: function (scope, element, attrs, ctrl) {
                element.bind('blur', function () {
                    if (ctrl.$valid) {
                        scope.lagreFaktum();
                    }
                });
                scope.formateringsfeil = function () {
                    return ctrl.$error.pattern;
                };

                scope.harRequiredFeil = function () {
                    return ctrl.$error.required;
                };
            }
        };
    }])

    .directive('navButtonSpinner', [function () {
        return {
            restrict: 'A',
            replace: true,
            scope: {
                laster: '=',
                klasse: '@',
                nokkel: '@',
                type: '@',
                click: '&'
            },
            templateUrl: '../js/common/directives/navinput/navbuttonspinnerTemplate.html'
        };
    }])

    .directive('booleanVerdi', [function () {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, element, attr, ngModel) {
                function fraTekst(tekst) {
                    return tekst === 'true';
                }

                function tilTekst(booleanVerdi) {
                    if (booleanVerdi) {
                        return 'true';
                    }
                    return 'false';
                }

                ngModel.$formatters.push(fraTekst);
                ngModel.$parsers.push(tilTekst);
            }
        };
    }])

    .directive('disableScroll', [function () {
        return {
            restrict: 'A',
            link: function (scope, element) {
                element.bind('mousewheel', function(event) {
                    event.preventDefault();
                });
            }
        };
    }])
    .directive('tallRange', [function() {
        return {
            restrict: 'A',
            require: ['ngModel', '^form'],
            link: {
                pre: function(scope, element, attrs, ctrls) {
                    var ngModel = ctrls[0];

                    if (attrs.min) {
                        var min = parseFloat(attrs.min);
                        ngModel.$parsers.push(function(input) {
                            var num = parseFloat(input);
                            if (isNaN(num)) {
                                return input;
                            }


                            ngModel.$setValidity('min', num > min);
                            return input;
                        });
                    }

                    if (attrs.max) {
                        var max = parseFloat(attrs.max);
                        ngModel.$parsers.push(function(input) {
                            var num = parseFloat(input);
                            if (isNaN(num)) {
                                return input;
                            }

                            ngModel.$setValidity('max', num <= max);
                            return input;
                        });
                    }
                }
            }
        };
    }]);

