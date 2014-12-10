angular.module('nav.datepicker.directive', [])
    .directive('datepicker', function(datepickerConfig, $filter) {
        return {
            restrict   : 'A',
            require    : '^form',
            replace    : true,
            templateUrl: '../js/modules/datepicker/templates/datepickerTemplate.html',
            scope      : {
                model: '=datepicker',
                harHattFokus: '=',
                isClosed: '=',
                erFremtidigdatoTillatt: '=?'
            },
            link: function (scope, element) {
                scope.options = {};
                if (!scope.erFremtidigdatoTillatt) {
                    scope.options.maxDate = new Date();
                }

                var inputElement = element.find('input');
                scope.toggleDatepicker = function () {
                    var dateDiv = $('#ui-datepicker-div');
                    if (dateDiv.is(':hidden')) {
                        inputElement.datepicker('show');
                        var pos = dateDiv.position();
                        pos.top = pos.top + 32;
                        dateDiv.offset(pos);
                    }
                };

                var defaultDate = new Date();

                function datepickerOptions() {
                    var currentDefaultDate = defaultDate;
                    defaultDate = scope.model ? new Date(scope.model) : currentDefaultDate;

                    if (currentDefaultDate.getTime() !== defaultDate.getTime()) {
                        scope.options = angular.extend({}, {defaultDate: defaultDate}, scope.options);
                    }
                    return angular.extend({}, datepickerConfig, scope.options);
                }

                function leggTilDatepicker() {
                    var opts = datepickerOptions();

                    opts.onSelect = function () {
                        var dato = inputElement.datepicker('getDate');
                        scope.model = $filter('date')(dato, 'yyyy-MM-dd');
                    };

                    opts.beforeShow = function () {
                        scope.isClosed = false;
                        scope.harHattFokus = true;
                    };
                    opts.onClose = function () {
                        scope.isClosed = true;
                        scope.$apply();
                    };

                    inputElement.datepicker('destroy');
                    inputElement.datepicker(opts);
                }

                // Legger til datepicker på nytt dersom options endrer seg
                scope.$watch(datepickerOptions, leggTilDatepicker, true);

                scope.$watch('model', function (newValue, oldValue) {
                    if (newValue === oldValue) {
                        return;
                    }

                    if (isNaN(new Date(scope.model).getDate())) {
                        inputElement.datepicker('setDate', new Date());
                    } else {
                        inputElement.datepicker('setDate', new Date(scope.model));
                    }
                });
            }
        };
    })
    .directive('htmlDatepicker', function ($timeout) {
        return {
            restrict: 'A',
            require: '^form',
            replace: true,
            templateUrl: '../js/modules/datepicker/templates/html5DatepickerTemplate.html',
            scope: {
                model: '=htmlDatepicker',
                harFokus: '=',
                erRequired: '=?',
                disabled: '=?',
                lagre: '&',
                requiredErrorMessage: '@',
                name: '@'
            },
            link: function(scope, element, attrs, form) {
                var eventForAValidereHeleFormen = 'RUN_VALIDATION' + form.$name;
                scope.$on(eventForAValidereHeleFormen, function () {
                    form[scope.name].$touched = true;
                    form[scope.name].$untouched = false;
                });

                scope.blur = function () {
                    scope.harFokus = false;

                    if (scope.lagre) {
                        $timeout(scope.lagre, 100);
                    }
                };

                scope.focus = function () {
                    scope.harFokus = true;
                };
            }
        };
    });

