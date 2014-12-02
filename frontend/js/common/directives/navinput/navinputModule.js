angular.module('nav.input', ['nav.cms'])
    .directive('navconfig', function () {
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
    })
    .directive('navradio', function (cms) {
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
                    scope.getCmsArgument = function() {
                        var cmsArgs = scope.cmsArgumenter;
                        if(cmsArgs && cmsArgs.modelFaktum) {
                            return cmsArgs.modelFaktum.value ? cmsArgs.modelFaktum.value : cmsArgs.default;
                        }
                        return null;
                    };
                },
                post: function (scope, element) {
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
    })
    .directive('navradioUtenfaktum', function (cms) {
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
                post: function (scope, element) {

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
    })
    .directive('navcheckbox', function (cms) {
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
    })
    .directive('navtekst', function ($compile) {
        return {
            restrict: 'A',
            templateUrl: '../js/common/directives/navinput/navtekstTemplate.html',
            replace: true,
            scope: true,
            compile: function(tElement, tAttrs) {
                if (tAttrs.navminlength) {
                    tElement.find('input').attr('data-ng-minlength', tAttrs.navminlength);
                    tElement.find('input').attr('minlength', tAttrs.navminlength);
                }

                if (tAttrs.navmaxlength) {
                    tElement.find('input').attr('data-ng-maxlength', tAttrs.navmaxlength);
                    tElement.find('input').attr('maxlength', tAttrs.navmaxlength);
                }

                if (tAttrs.regexvalidering) {
                    tElement.find('input').attr('data-ng-pattern', tAttrs.regexvalidering.toString());
                }

                tElement.removeAttr('navtekst');
                tElement.removeAttr('data-navtekst');
                $compile(tElement.find('input'));

                return function (scope) {
                    scope.harSporsmal = function() {
                        return isNotNullOrUndefined(scope.navsporsmal) && scope.navsporsmal.length > 0;
                    };
                };
            }
        };
    })
    .directive('navtall', function (cms, $compile) {
        return {
            restrict: 'A',
            replace: true,
            templateUrl: '../js/common/directives/navinput/navtallTemplate.html',
            scope: true,
            compile: function(tElement, tAttrs) {
                if (tAttrs.navminlength) {
                    tElement.find('input').attr('minlength', tAttrs.navminlength);
                }

                if (tAttrs.navmaxlength) {
                    tElement.find('input').attr('maxlength', tAttrs.navmaxlength);
                }

                if (tAttrs.regexvalidering) {
                    tElement.find('input').attr('data-ng-pattern', tAttrs.regexvalidering.toString());
                }

                if (tAttrs.navminvalue) {
                    tElement.find('input').attr('data-min', tAttrs.navminvalue);
                }

                if (tAttrs.navmaxvalue) {
                    tElement.find('input').attr('data-max', tAttrs.navmaxvalue);
                }

                tElement.removeAttr('navtall');
                tElement.removeAttr('data-navtall');
                $compile(tElement.find('input'));

                return {
                    pre: function (scope, element, attrs) {
                        scope.hjelpetekst = {
                            tittel: attrs.hjelpetekst + '.tittel',
                            tekst: attrs.hjelpetekst + '.tekst'
                        };
                    },
                    post: function (scope) {
                        scope.harSporsmal = function() {
                            return isNotNullOrUndefined(scope.navsporsmal) && scope.navsporsmal.length > 0;
                        };
                        scope.hvisHarHjelpetekst = function() {
                            return cms.tekster[scope.hjelpetekst.tittel] !== undefined;
                        };
                    }
                };
            }
        };
    })
    .directive('tekstfeltPatternvalidering', function () {
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
    })

    .directive('navorganisasjonsnummerfelt', [function () {
        return {
            restrict: "A",
            replace: true,
            scope: true,
            link: function (scope) {
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

