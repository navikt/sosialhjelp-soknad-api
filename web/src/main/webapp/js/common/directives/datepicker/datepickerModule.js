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
        altFormat: 'dd.MM.yyyy'
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

                scope.dagBlur = function() {
                    if (scope.dag && scope.dag != '0' && scope.dag.length < 2) {
                        scope.dag = '0' + scope.dag;
                    }

                    scope.sjekkDato();
                }

                scope.manedBlur = function() {
                    if (scope.maned && scope.maned != '0' && scope.maned.length < 2) {
                        scope.maned = '0' + scope.maned;
                    }

                    scope.sjekkDato();
                }

                scope.sjekkDato = function() {
                    harFokus = false;
                    if (scope.dag && scope.maned && scope.aar && scope.aar.length == 4) {
                        scope.ngModel = new Date(scope.aar, scope.maned - 1, scope.dag);
                    } else {
                        scope.ngModel = '';
                    }
                }

                scope.focus = function () {
                    harFokus = true;
                    harHattFokus = true;
//                    if (scope.tilDatoFeil != undefined) {
//                        scope.tilDatoFeil = false;
//                    }
                }

                scope.$on(eventForAValidereHeleFormen, function () {
                    harHattFokus = true;
                });

                scope.harRequiredFeil = function () {
                    return scope.erRequired && !scope.dag && !scope.maned && !scope.aar && !harFokus && harHattFokus && datepickerErLukket && !scope.tilDatoFeil;
                }

                scope.harIkkeFyltInnDag = function () {
                    return scope.erRequired && !scope.dag && (scope.maned || scope.aar) && !harFokus && harHattFokus && datepickerErLukket && !scope.tilDatoFeil;
                }

                scope.harIkkeFyltInnManed = function () {
                    return scope.erRequired && scope.dag && !scope.maned && !harFokus && harHattFokus && datepickerErLukket && !scope.tilDatoFeil;
                }

                scope.harIkkeFyltInnAar = function () {
                    return scope.erRequired && scope.dag && scope.maned && !scope.aar && !harFokus && harHattFokus && datepickerErLukket && !scope.tilDatoFeil;
                }

                scope.harTilDatoFeil = function () {
                    return scope.erRequired && !harFokus && harHattFokus && datepickerErLukket && scope.tilDatoFeil;
                }

                scope.harFormatteringsFeil = function () {
                    return scope.dag && scope.maned && scope.aar && !datoRegExp.test(scope.dag + '.' + scope.maned + '.' + scope.aar) && !harFokus;
                }

                scope.harFeil = function () {
                    return scope.harRequiredFeil() || scope.harIkkeFyltInnDag() || scope.harIkkeFyltInnManed() || scope.harIkkeFyltInnAar() || scope.harFormatteringsFeil();// || scope.harTilDatoFeil();
                }

                scope.$watch('ngModel', function (newVal, oldVal) {
                    if (newVal != oldVal && scope.endret) {
                        scope.endret();
                    }
                });

                scope.$watch('ngModel', function() {
                    if (scope.ngModel) {
                        var d = new Date(scope.ngModel);
                        var dag = d.getDate().toString();
                        var maned = (d.getMonth() + 1).toString();

                        if (dag.length < 2) {
                            dag = '0' + dag;
                        }

                        if (maned.length < 2) {
                            maned = '0' + maned;
                        }
                        scope.dag = dag;
                        scope.maned = maned;
                        scope.aar = d.getFullYear().toString();
                    }
                });

                function datepickerOptions() {
                    return angular.extend({}, datepickerConfig, scope.options);
                };

                function leggTilDatepicker() {
                    var opts = datepickerOptions();

                    opts.onSelect = function () {
                        scope.ngModel = datepickerInput.datepicker("getDate");
//                        datepickerInput.datepicker('hide');
                        scope.$apply();
                    };

                    opts.beforeShow = function () {
                        datepickerErLukket = false;
                    };
                    opts.onClose = function () {
                        datepickerErLukket = true;
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
    .directive('datoFormat', ['$filter', function ($filter) {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, element, attrs, ngModel) {
                var datoFormat = 'dd.MM.yyyy';

                function fraDatoString(datoString) {
                    return konverterStringFraNorskDatoformatTilDateObjekt(datoString);
                }

                function tilDatoString(dato) {
                    return $filter('date')(dato, datoFormat);
                }

                ngModel.$formatters.push(tilDatoString);
                ngModel.$parsers.push(fraDatoString);
            }
        }
    }])
    .directive('tall', [function () {
        return {
            restrict: 'A',
            require: '?ngModel',
            link: function (scope, element, attrs, ngModel) {
                if (!ngModel) {
                    return;
                }

                ngModel.$parsers.unshift(function (inputVerdi) {
                    var tall = inputVerdi.split('').filter(function (siffer) {
                        return (!isNaN(siffer) && siffer != ' ');
                    }).join('');

                    ngModel.$viewValue = tall;
                    ngModel.$render();

                    return tall;
                });
            }
        }
    }])
    .directive('minstToSiffer', [function() {
        return {
            restrict: 'A',
            require: '?ngModel',
            link: function (scope, element, attrs, ngModel) {
                if (!ngModel) {
                    return;
                }

                ngModel.$parsers.unshift(function (inputVerdi) {
                    var tall = inputVerdi;
                    if (inputVerdi.length < 2) {
                        tall = '0' + tall;
                    }

                    ngModel.$viewValue = tall;
                    ngModel.$render();
                    console.log(tall);
                    return tall;
                });
            }
        }
    }]);