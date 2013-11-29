/**
 * Både direktiv for å legge til enkel datepicker og til-fra dato.
 *
 * nav-dato: Legger til en enkel datepicker. Følgende attributter må oppgis:
 *      - ngModel: Modellen hvor valgt dato skal lagres
 *      - label: Nøkkel til CMS for å hente ut label-tekst
 *
 * Følgende attributter kan oppgis:
 *      - erRequired: Expression som sier om feltet er påkrevd eller ikke
 *      - requiredErrorMessage: Nøkkel til CMS for å hente ut feilmeldingstekst for required-feil
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
 *      - erRequired: Expression som sier om feltet er påkrevd eller ikke
 *
 */

angular.module('nav.datepicker', [])
    .constant('datepickerConfig', {
        altFormat: 'dd.MM.yyyy'
    })
    .directive('navDato', ['datepickerConfig', function (datepickerConfig) {
        return {
            restrict: "A",
            require: ['^form'],
            replace: true,
            templateUrl: '../js/common/directives/datepicker/singleDatepickerTemplate.html',
            scope: {
                ngModel: '=',
                erRequired:  '=',
                tilDato: '=',
                fraDato: '=',
                tilDatoFeil: '=',
                label: '@',
                requiredErrorMessage: '@'
            },
            link: function(scope, element, attrs, form) {
                var eventForAValidereHeleFormen = 'RUN_VALIDATION' + form.$name;
                var datoRegExp = new RegExp(/^\d{1,2}\.\d{1,2}\.\d\d\d\d$/);

                var input = element.find('input');
                var harHattFokus = false;
                var harFokus = false;
                var datepickerErLukket = true;
                var inputVerdiVedPressAvEnter = '';

                scope.blur = function() {
                    harHattFokus = true;
                    harFokus = false;

                    if (new Date(scope.ngModel) < new Date(scope.fraDato)) {
                        scope.ngModel = '';
                        input.val('');
                        scope.tilDatoFeil = true;
                    } else if (new Date(scope.tilDato) < new Date(scope.ngModel)) {
                        scope.tilDato = '';
                        scope.tilDatoFeil = true;
                    }
                }

                scope.enter = function() {
                    inputVerdiVedPressAvEnter = input.val();
                }

                scope.focus = function() {
                    harFokus = true;
                    inputVerdiVedPressAvEnter = '';
                    if (scope.tilDatoFeil != undefined) {
                        scope.tilDatoFeil = false;
                    }
                }

                scope.$on(eventForAValidereHeleFormen, function() {
                    harHattFokus = true;
                });

                scope.harRequiredFeil = function() {
                    return scope.erRequired && !input.val() && !harFokus && harHattFokus && datepickerErLukket && !scope.tilDatoFeil;
                }

                scope.harTilDatoFeil = function() {
                    return scope.erRequired && !input.val() && !harFokus && harHattFokus && datepickerErLukket && scope.tilDatoFeil;
                }

                scope.harFormatteringsFeil = function() {
                    return input.val() && !datoRegExp.test(input.val()) && !harFokus;
                }

                scope.harFeil = function() {
                    return scope.harRequiredFeil() || scope.harFormatteringsFeil() || scope.harTilDatoFeil();
                }

                function datepickerOptions() {
                    return angular.extend({}, datepickerConfig, scope.options);
                };

                function leggTilDatepicker() {
                    var opts = datepickerOptions();

                    opts.onSelect = function () {
                        if (!inputVerdiVedPressAvEnter || datoRegExp.test(inputVerdiVedPressAvEnter)) {
                            scope.ngModel = input.datepicker("getDate");

                        } else {
                            input.val(inputVerdiVedPressAvEnter);
                            scope.ngModel = '';
                        }

                        input.datepicker('hide');
                        input.blur();
                    };

                    opts.beforeShow = function () {
                        datepickerErLukket = false;
                    };
                    opts.onClose = function () {
                        datepickerErLukket = true;
                    };

                    input.datepicker('destroy');
                    input.datepicker(opts);
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
                erRequired:  '=',
                label: '@'
            },
            controller: function($scope) {
                $scope.fraLabel = $scope.label + ".fra";
                $scope.tilLabel = $scope.label + ".til";
                $scope.fraFeilmelding = $scope.fraLabel + ".feilmelding";
                $scope.tilFeilmelding = $scope.tilLabel + ".feilmelding";
                $scope.tilDatoFeil = false;
            }
        }
    }])
    .directive('datoFormat', ['$filter', function ($filter) {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function(scope, element, attrs, ngModel) {
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
    }]);