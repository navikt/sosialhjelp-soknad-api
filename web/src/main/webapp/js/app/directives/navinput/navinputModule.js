angular.module('nav.input', ['nav.cmstekster'])
    .directive('navradio', [function () {
        return {
            restrict: "AE",
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
            restrict: "AE",
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
            link: function (scope, element) {
//                var tmpElementName = 'tmpName';
//                fiksNavn(element, scope.inputname, tmpElementName);

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
            restrict: "AE",
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
            restrict: "AE",
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
    }])
    .directive('blurValidate', ['data', function (data) {
        return {
            require: 'ngModel',
            link: function (scope, element, attrs, ngModel) {
                var eventString = 'RUN_VALIDATION' + attrs.$name;
                console.log(attrs);
                var feil, feilNokkel;
                try {
                    feil = scope.$eval(attrs.errorMessages);
                } catch(e) {
                    feil = attrs.errorMessages;
                }

                var revaliderFeilMetode;
                var valideringsMetoder = [];

                // Rekkefølgen på setup-metodene bestemmer prioriteten på valideringsmetodene
                setupRequired();
                var pattern = setupPattern();
                var minLengde = setupMinLength();
                var maxLengde = setupMaxLength();
                var formElem = element.closest('.form-linje');

//                scope.$on('')

                element.bind('blur', function () {
                    if (!sjekkOmInputErGyldig()) {
                        formElem.addClass('feil');
                    }
                });

                scope.$watch(function() {
                    return ngModel.$viewValue;
                }, function() {
                    if (revaliderFeilMetode && revaliderFeilMetode()) {
                        formElem.removeClass('feil');
                    }
                });

                function sjekkOmInputErGyldig() {
                    var gyldig = true;
                    for (var i = 0; i < valideringsMetoder.length; i++) {
                        gyldig = gyldig && valideringsMetoder[i]();

                        if (!gyldig) {
                            revaliderFeilMetode = valideringsMetoder[i];
                            settFeilmeldingsTekst();
                            break;
                        }
                    }
                    return gyldig;
                }

                function sjekkOmInputErRequiredOgValiderer() {
                    feilNokkel = 'required';
                    return verdiErIkkeTom(ngModel.$viewValue);
                }

                function sjekkOmInputHarMinimumLengde() {
                    feilNokkel = 'minlength';
                    return ngModel.$viewValue.length > minLengde;
                }

                function sjekkOmInputHarRegexOgValiderer() {
                    feilNokkel = 'pattern';
                    return pattern.test(ngModel.$viewValue);
                }

                function sjekkOmInputHarMaxLengde() {
                    feilNokkel = 'maxlength';
                    return ngModel.$viewValue.length < maxLengde;
                }

                function setupMaxLength() {
                    var maxLengdeVal = harAttributt(attrs, 'maxlength');

                    if (maxLengdeVal) {
                        valideringsMetoder.push(sjekkOmInputHarMaxLengde);
                        return maxLengdeVal;
                    }
                    return false;
                }

                function setupMinLength() {
                    var minLengdeVal = harAttributt(attrs, 'minlength');

                    if (minLengdeVal) {
                        valideringsMetoder.push(sjekkOmInputHarMinimumLengde);
                        return minLengdeVal;
                    }
                    return false;
                }

                function setupRequired() {
                    if (harAttributt(attrs, 'required')) {
                        valideringsMetoder.push(sjekkOmInputErRequiredOgValiderer);
                    }
                }

                function setupPattern() {
                    var stringPattern = harAttributt(attrs, 'pattern');
                    if (stringPattern) {
                        valideringsMetoder.push(sjekkOmInputHarRegexOgValiderer);

                        // Tatt fra angular for å bygge regexp fra string
                        var match = stringPattern.match(/^\/(.*)\/([gim]*)$/);
                        return new RegExp(match[1], match[2]);
                    } else {
                        return false;
                    }
                }

                function settFeilmeldingsTekst() {
                    var feilmeldingsNokkel = feil;

                    if(typeof feil === 'object') {
                        feilmeldingsNokkel = feil[feilNokkel];
                    }

                    var feilmeldingTekst = data.tekster[feilmeldingsNokkel];
                    formElem.find('.melding').text(feilmeldingTekst);
                }
            }
        }
    }]);
