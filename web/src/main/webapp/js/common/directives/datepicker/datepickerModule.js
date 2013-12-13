/**
 * Både direktiv for å legge til enkel datepicker og til-fra dato.
 *
 * nav-dato: Legger til en enkel datepicker. Følgende attributter må oppgis:
 *      - ngModel: Modellen hvor valgt dato skal lagres
 *      - label: Nøkkel til CMS for å hente ut label-tekst
 *
 * Følgende attributter kan oppgis:
 *      - er-required: Expression som sier om feltet er påkrevd eller ikke
 *      - required-error-message: Nøkkel til CMS for å hente ut feilmeldingstekst for required-feil
 *
 * Følgende attributter brukes av det andre direktivet for å kjøre validering på datointervallet:
 *      - fraDato: Startdato i intervallet
 *      - tilDato: Sluttdato i intervallet
 *      - tilDatoFeil: Boolean som sier om vi har hatt en feil hvor sluttdato er før startdato
 *
 * Disse attributtene skal man aldri sette når man bruke nav-dato.
 *
 *
 * nav-dato-intervall: Legger til en datepicker med start- og sluttdato. Følgende attributter må oppgis:
 *      - fraDato: Startdato i intervallet
 *      - tilDato: Sluttdato i intervallet
 *      - label: Starten på nøkkeler i CMS. Bruker til å hente ut label og required-feilmelding for begge datepickere.
 *               Dersom label er 'datepicker.noe', må følgende nøkler ligge i CMS:
 *                  - datepicker.noe.til=Label til startdato
 *                  - datepicker.noe.fra=Label til sluttdato
 *                  - datepicker.noe.til.feilmelding=Required-feilmelding for startdato
 *                  - datepicker.noe.fra.feilmelding=Required-feilmelding for sluttdato
 *
 * Følgende attributter kan oppgis:
 *      - er-tildato-required: Expression som sier om til-dato er påkrevd. Er false dersom ikke oppgitt
 *      - er-fradato-required: Expression som sier om fra-dato er påkrevd. Er false dersom ikke oppgitt
 *      - er-begge-required: Expression som sier om både til- og fra-dato er påkrevd. Er false dersom ikke oppgitt.
 *                           Denne setter både er-fradato-required og er-tildato-required.
 */

angular.module('nav.datepicker', [])
    .constant('datepickerConfig', {
        altFormat: 'dd.MM.yyyy',
        changeMonth: true,
        changeYear: true,
        maxDate: new Date()
    })
    .directive('navDato', ['datepickerConfig', function (datepickerConfig) {
        return {
            restrict: "A",
            require: '^form',
            replace: true,
            templateUrl: '../js/common/directives/datepicker/singleDatepickerTemplate.html',
            scope: {
                ngModel: '=',
                erRequired: '=',
                tilDato: '=',
                fraDato: '=',
                tilDatoFeil: '=',
                endret: '&',
                label: '@',
                requiredErrorMessage: '@'
            },
            link: function (scope, element, attrs, form) {
                var eventForAValidereHeleFormen = 'RUN_VALIDATION' + form.$name;
                var datoRegExp = new RegExp(/^\d\d\.\d\d\.\d\d\d\d$/);

                var tekstInput = element.find('input').first();
                var datepickerInput = element.find('input').last();
                var harHattFokus = false;
                var harFokus = false;
                var datepickerErLukket = true;

                scope.toggleDatepicker = function() {
                    if ($('#ui-datepicker-div').is(':hidden')) {
                        datepickerInput.datepicker('show');
                        var pos = $('#ui-datepicker-div').position();
                        pos.top = pos.top + 32;
                        $('#ui-datepicker-div').offset(pos);
                    }
                }

                scope.blur = function() {
                    harFokus = false;

                    if (new Date(scope.ngModel) < new Date(scope.fraDato)) {
                        scope.ngModel = '';
                        scope.tilDatoFeil = true;
                    } else if (new Date(scope.tilDato) < new Date(scope.ngModel)) {
                        scope.tilDato = '';
                        scope.tilDatoFeil = true;
                    }
                }

                scope.focus = function () {
                    harFokus = true;
                    harHattFokus = true;
                    if (scope.tilDatoFeil != undefined) {
                        scope.tilDatoFeil = false;
                    }
                }

                scope.$on(eventForAValidereHeleFormen, function () {
                    harHattFokus = true;
                });

                scope.harRequiredFeil = function () {
                    return scope.erRequired && !scope.ngModel && !harFokus && harHattFokus && datepickerErLukket && !scope.tilDatoFeil && !scope.harFormatteringsFeil() && !scope.erIkkeGyldigDato();
                }

                scope.harTilDatoFeil = function () {
                    return !scope.ngModel && !harFokus && harHattFokus && datepickerErLukket && scope.tilDatoFeil;
                }

                scope.harFormatteringsFeil = function () {
                    return tekstInput.val() && !datoRegExp.test(tekstInput.val()) && !harFokus && harHattFokus;
                }

                scope.erIkkeGyldigDato = function() {
                    return !scope.ngModel && tekstInput.val() && datoRegExp.test(tekstInput.val()) && !erGyldigDato(tekstInput.val()) && !harFokus && harHattFokus;
                }

                scope.harFeil = function () {
                    return scope.harRequiredFeil() || scope.harFormatteringsFeil() || scope.harTilDatoFeil() || scope.erIkkeGyldigDato();
                }

                scope.$watch('ngModel', function (newVal, oldVal) {
                    if (newVal != oldVal && scope.endret) {
                        scope.endret();
                    }

                    if (new Date(scope.ngModel) < new Date(scope.fraDato)) {
                        scope.ngModel = '';
                        scope.tilDatoFeil = true;
                    } else if (new Date(scope.tilDato) < new Date(scope.ngModel)) {
                        scope.tilDato = '';
                        scope.tilDatoFeil = true;
                    }
                });

                function datepickerOptions() {
                    var defaultDate = scope.ngModel ? new Date(scope.ngModel) : new Date();
                    return angular.extend({defaultDate: defaultDate}, datepickerConfig, scope.options);
                };

                function leggTilDatepicker() {
                    var opts = datepickerOptions();

                    opts.onSelect = function () {
                        scope.ngModel = datepickerInput.datepicker("getDate");
                    };

                    opts.beforeShow = function () {
                        datepickerErLukket = false;
                        harHattFokus = true;
                    };
                    opts.onClose = function () {
                        datepickerErLukket = true;
                        scope.$apply();
                    };

                    datepickerInput.datepicker('destroy');
                    datepickerInput.datepicker(opts);
                };

                // Legger til datepicker på nytt dersom options endrer seg
                scope.$watch(datepickerOptions, leggTilDatepicker, true);
            }
        }
    }])
    .directive('navDatoIntervall', [function () {
        return {
            restrict: "A",
            replace: true,
            templateUrl: '../js/common/directives/datepicker/doubleDatepickerTemplate.html',
            scope: {
                fraDato: '=',
                tilDato: '=',
                erFradatoRequired: '=',
                erTildatoRequired: '=',
                erBeggeRequired: '=',
                label: '@'
            },
            controller: function ($scope) {
                $scope.fraLabel = $scope.label + ".fra";
                $scope.tilLabel = $scope.label + ".til";
                $scope.fraFeilmelding = $scope.fraLabel + ".feilmelding";
                $scope.tilFeilmelding = $scope.tilLabel + ".feilmelding";
                $scope.tilDatoFeil = false;

                if ($scope.erBeggeRequired) {
                    $scope.$watch('erBeggeRequired', function () {
                        $scope.fradatoRequired = $scope.erBeggeRequired;
                        $scope.tildatoRequired = $scope.erBeggeRequired;
                    });
                } else {
                    $scope.$watch('erFradatoRequired', function () {
                        $scope.fradatoRequired = $scope.erFradatoRequired;
                    });

                    $scope.$watch('erTildatoRequired', function () {
                        $scope.tildatoRequired = $scope.erTildatoRequired;
                    });
                }
            }
        }
    }])
    .directive('datoMask', ['$filter', function($filter) {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, element, attrs, ngModel) {
                if (!ngModel) {
                    return;
                }

                var datoMaskFormat = 'dd.mm.åååå';

                var caretPosElem = element.closest('.datepicker').find('.caretPosition');
                var maskElement = element.next();
                var originalLeft = element.position().left + 8;
                var top = element.position().top + 7
                var left = originalLeft;
                maskElement.css({top: top + "px", left: left + "px"});
                maskElement.text(datoMaskFormat);

                caretPosElem.hide();
                element.bind('blur', function() {
                    caretPosElem.hide();
                });

                element.bind('focus', function() {
                    caretPosElem.show();
                });

                maskElement.bind('click', function() {
                    element.focus();
                });

                element.bind('keydown', function(event) {
                    if (event.keyCode == 32) {
                        return false;
                    }
                });

                ngModel.$formatters.unshift(function(dato) {
                    return $filter('date')(dato, "dd.MM.yyyy");
                });

                var gammelInputVerdi = '';
                ngModel.$parsers.unshift(function (datoInput) {
                    var slettet = datoInput.length < gammelInputVerdi.length;
                    var caretPos = caretPosisjon(element);
                    if (!slettet) {
                        var skrevetTegn = datoInput[caretPos - 1];
                        if (isNaN(skrevetTegn) || datoInput.length > datoMaskFormat.length) {
                            datoInput = datoInput.splice(caretPos - 1, 1, '');
                            caretPos--;
                        }
                    }

                    if (!slettet && (caretPos == 2 || caretPos == 5)) {
                        if (datoInput.length < caretPos + 1) {
                            datoInput = datoInput + '.';
                            caretPos++;
                        } else if (datoInput[caretPos] == '.') {
                            caretPos++;
                        } else if (datoInput.match(/\./g) == null || datoInput.match(/\./g).length < 2) {
                            datoInput = datoInput.splice(caretPos,0,'.');
                            caretPos++;
                        }
                    }

                    gammelInputVerdi = datoInput;
                    ngModel.$viewValue = datoInput;
                    ngModel.$render();
                    settCaretPosisjon(element, caretPos);

                    return konverterStringFraNorskDatoformatTilDateObjekt(datoInput);
                });

                scope.$watch(
                    function() {
                        return ngModel.$viewValue;
                    },
                    function(nyVerdi, gammelVerdi) {
                        if (nyVerdi == gammelVerdi) {
                            return;
                        }

                        var tekst = nyVerdi;
                        if (nyVerdi == undefined) {
                            tekst = '';
                        }

                        caretPosElem.text(tekst);
                        left = originalLeft + caretPosElem.outerWidth();
                        maskElement.css({top: top + "px", left: left + "px"});

                        var antallPunktum = tekst.match(/\./g) == null ? 0 : tekst.match(/\./g).length;

                        var maskTekst = '';
                        if (antallPunktum == 0 && tekst.length < 3) {
                            maskTekst = datoMaskFormat.substring(tekst.length, datoMaskFormat.length);
                        } else if (antallPunktum == 1) {
                            var dagTekst = tekst.substring(0, tekst.indexOf('.'));
                            var maanedTekst = tekst.substring(tekst.indexOf('.'), tekst.length);

                            if (dagTekst.length < 3 && maanedTekst.length < 4) {
                                maskTekst = datoMaskFormat.substring(2 + maanedTekst.length, datoMaskFormat.length);
                            }
                        } else if (antallPunktum == 2) {
                            var dagOgMaanedTekst = tekst.substring(0, tekst.lastIndexOf('.'));
                            var aarTekst = tekst.substring(tekst.lastIndexOf('.'), tekst.length);
                            if (dagOgMaanedTekst.length < 6 && aarTekst.length < 5) {
                                maskTekst = datoMaskFormat.substring(5 + aarTekst.length, datoMaskFormat.length);
                            }
                        }
                        maskElement.text(maskTekst);
                    }
                )
            }
        }
    }]);