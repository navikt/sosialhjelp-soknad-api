angular.module('nav.datepicker.dato', [])
    .directive('navDatoIntervall', function () {
        return {
            restrict   : 'A',
            replace    : true,
            templateUrl: '../js/modules/datepicker/templates/doubleDatepickerTemplate.html',
            scope      : {
                fraDato               : '=',
                tilDato               : '=',
                erFradatoRequired     : '=',
                erTildatoRequired     : '=',
                erFremtidigdatoTillatt: '=',
                lagre                 : '&',
                label                 : '@',
                disabled              : '=?'
            },
            controller : function ($scope) {
                $scope.fraLabel = $scope.label + '.fra';
                $scope.tilLabel = $scope.label + '.til';
                $scope.fraFeilmelding = $scope.fraLabel + '.feilmelding';
                $scope.tilFeilmelding = $scope.tilLabel + '.feilmelding';
                $scope.tilDatoFeil = false;
            }
        };
    })
    .directive('dato', function (cms, deviceService, guidService) {
        return {
            restrict   : 'A',
            require    : '^form',
            replace    : true,
            templateUrl: '../js/modules/datepicker/templates/dateTemplate.html',
            scope      : {
                model: '=ngModel',
                erRequired: '=?',
                erFremtidigdatoTillatt: '=?',
                tilDato: '=?',
                fraDato: '=?',
                disabled: '=?',
                label: '@',
                name: '@?',
                requiredErrorMessage: '@?',
                lagre: '&?'

            },
            link: {
                pre: function(scope) {
                    scope.name = scope.name !== undefined ? scope.name : guidService.getGuid();
                    scope.vars = {
                        model: scope.model,
                        harFokus: false,
                        datepickerClosed: true,
                        erRequired: scope.erRequired,
                        requiredErrorMessage: scope.requiredErrorMessage,
                        lagre: scope.lagre,
                        name: scope.name,
                        erFremtidigdatoTillatt: scope.erFremtidigdatoTillatt
                    };
                },
                post: function (scope, element, attrs, form) {
                    scope.form = form;
                    scope.$watch('model', function(newValue, oldValue) {
                        if (newValue === oldValue) {
                            return;
                        }

                        var toInputFieldName = element.next().find('input').first().attr('name');
                        if (new Date(scope.tilDato) < new Date(newValue)) {
                            scope.tilDato = '';
                            form[toInputFieldName].$setValidity('toDate', false);
                        }

                        scope.vars.model = scope.model;
                    });

                    scope.$watch('vars.model', function(newValue, oldValue) {
                        if (newValue === oldValue) {
                            return;
                        }

                        if (scope.endret) {
                            scope.endret();
                        }

                        if (new Date(newValue) < new Date(scope.fraDato)) {
                            scope.vars.model = '';
                            form[scope.name].$setValidity('toDate', false);
                            form[scope.name].$touched = true;
                            form[scope.name].$untouched = false;
                        } else if (!isNaN(new Date(newValue))) {
                            form[scope.name].$setValidity('toDate', true);
                        }

                        scope.model = scope.vars.model;
                    });

                    scope.navDatepicker = function() {
                        return !scope.vanligDatepicker();
                    };

                    scope.vanligDatepicker = function() {
                        return deviceService.isTouchDevice();
                    };

                    scope.harRequiredFeil = function () {
                        var input = form[scope.name];
                        return input && input.$error.required && input.$touched && !scope.vars.harFokus;
                    };

                    scope.harTilDatoFeil = function () {
                        var input = form[scope.name];
                        return input && input.$error.toDate && input.$touched;
                    };

                    scope.harFormatteringsFeil = function () {
                        var input = form[scope.name];
                        return input && input.$error.dateFormat && input.$touched && !scope.vars.harFokus;
                    };

                    scope.erUloveligFremtidigDato = function() {
                        var input = form[scope.name];
                        return input && input.$error.futureDate && input.$touched && !scope.vars.harFokus;
                    };

                    scope.erIkkeGyldigDato = function () {
                        var input = form[scope.name];
                        return input && input.$error.validDate && input.$touched && !scope.vars.harFokus;
                    };

                    scope.harFeil = function () {
                        if (scope.navDatepicker()) {
                            return harFeilMedNavDatepicker();
                        } else {
                            return harFeilMedDateInput();
                        }
                    };

                    function harFeilMedNavDatepicker() {
                        return scope.harRequiredFeil() || scope.harFormatteringsFeil() || scope.harTilDatoFeil() || scope.erIkkeGyldigDato() || scope.erUloveligFremtidigDato();
                    }

                    function harFeilMedDateInput() {
                        return scope.harRequiredFeil() || scope.harTilDatoFeil();
                    }
                }
            }
        };
    });
