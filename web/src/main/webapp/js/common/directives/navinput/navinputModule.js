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
                post: function (scope, element, attr) {
//                var tmpElementName = 'tmpName';
//                fiksNavn(element, scope.inputname, tmpElementName);
                    scope.hvisHarHjelpetekst = function () {
                        return scope.hjelpetekst.tittel && scope.hjelpetekst.tekst;
                    }

                    scope.hvisHuketAv = function () {
                        var transcludeElement = element.find('.ng-transclude');
                        return checkTrue(scope.faktum.value) && transcludeElement.text().length > 0;
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
            link: function (scope, element) {
                scope.hvisSynlig = function () {
                    return element.is(':visible');
                }
            },
            templateUrl: '../js/common/directives/navinput/navtekstTemplate.html'
        }
    }])

    .directive('navorganisasjonsnummerfelt', [function () {
        return {
            restrict: "A",
            replace: true,
            scope: true,
            link: function (scope, element) {
                scope.hvisSynlig = function () {
                    return element.is(':visible');
                }
            },
            templateUrl: '../js/common/directives/navinput/navorgnrfeltTemplate.html'
        }
    }]).directive('orgnrValidate', [function () {
        return {
            require: ['ngModel'],
            link: function (scope, element, attrs, ctrls) {
                var tallRegEx = new RegExp(/^-?\d+\.?\d*$/);
                var ngModel = ctrls[0];

                ngModel.$parsers.unshift(function (viewValue) {
                    var verdi = viewValue
                    if(verdi && verdi.length == 9 || scope.harFormatteringsFeil()) {
                        scope.forFaaTegn = false;
                    }
                    if (verdi && verdi.length > 9) {
                        verdi = viewValue.substring(0, 9);
                        ngModel.$viewValue = verdi;
                        ngModel.$render();
                    }
                    return verdi;
                });

                //hvis man skrive space så validerer den ikke til false før neste verdi blir skrevet inn
                scope.harFormatteringsFeil = function () {
                    if (ngModel.$viewValue == undefined || ngModel.$viewValue.length == 0) {
                        return false;
                    }
                    return !tallRegEx.test(ngModel.$viewValue);
                }

                element.bind('blur', function(){
                    scope.forFaaTegn = false;
                    if (ngModel.$viewValue && ngModel.$viewValue.length < 9 && ngModel.$viewValue.length > 1) {
                        scope.forFaaTegn = true;
                    }
                })

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
