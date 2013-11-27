angular.module('nav.datepicker', [])
    .constant('datepickerConfig', {
        altFormat: 'dd.MM.yyyy'
    })
    .directive('dato', ['data', 'datepickerConfig', function (data, datepickerConfig) {
        return {
            restrict: "A",
            require: ['ngModel', '^form'],
            replace: true,
            templateUrl: '../js/common/directives/datepicker/singleDatepickerTemplate.html',
            scope: {
                ngModel: '=',
                ngRequired:  '=',
                tilDato: '=',
                fraDato: '=',
                tilDatoFeil: '=',
                options: '=dato',
                label: '@',
                requiredErrorMessage: '@'
            },
            link: function(scope, element, attrs, ctrls) {
                var form = ctrls[1];
                var eventForAValidereHeleFormen = 'RUN_VALIDATION' + form.$name;
                var datoRegExp = new RegExp(/^\d\d\.\d\d\.\d\d\d\d$/);

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
                    scope.tilDatoFeil = false;
                    inputVerdiVedPressAvEnter = '';
                }

                scope.$on(eventForAValidereHeleFormen, function() {
                    harHattFokus = true;
                });

                scope.harRequiredFeil = function() {
                    return scope.ngRequired && !input.val() && !harFokus && harHattFokus && datepickerErLukket && !scope.tilDatoFeil;
                }

                scope.harTilDatoFeil = function() {
                    return scope.ngRequired && !input.val() && !harFokus && harHattFokus && datepickerErLukket && scope.tilDatoFeil;
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

                    // If we don't destroy the old one it doesn't update properly when the config changes
                    input.datepicker('destroy');

                    // Create the new datepicker widget
                    input.datepicker(opts);
                };

                // Watch for changes to the directives options
                scope.$watch(datepickerOptions, leggTilDatepicker, true);
            }
        }
    }])
    .directive('datoInterval', [function () {
        return {
            restrict: "A",
            replace: true,
            templateUrl: '../js/common/directives/datepicker/doubleDatepickerTemplate.html',
            scope: {
                fraDato: '=',
                tilDato: '=',
                ngRequired:  '=',
                label: '@',
                requiredErrorMessage: '@'
            },
            controller: function($scope) {
                $scope.fraLabel = $scope.label + ".fra";
                $scope.tilLabel = $scope.label + ".til";
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