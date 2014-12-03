/**
 * Både direktiv for å legge til enkel datepicker og til-fra dato.
 *
 * dato: Legger til en enkel datepicker. Følgende attributter må oppgis:
 *      - ngModel: Modellen hvor valgt dato skal lagres
 *      - label: Nøkkel til CMS for å hente ut label-tekst
 *
 * Følgende attributter kan oppgis:
 *      - er-required: Expression som sier om feltet er påkrevd eller ikke
 *      - required-error-message: Nøkkel til CMS for å hente ut feilmeldingstekst for required-feil
 *      - er-fremtidigdato-tilatt: Expression som sier om det er lovelig å sette datoen frem i tid.
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
 *      - er-fremtidigdato-tillatt: Expression som sier om det er lovelig å sette datoen frem i tid.
 */

angular.module('nav.datepicker.directive', [])
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
            link: function (scope, element, attrs, form) {
                scope.form = form;
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
                    return input && input.$error.required && input.$touched;
                };

                scope.harTilDatoFeil = function () {
                    var input = form[scope.name];
                    return input && input.$error.toDate && input.$touched;
                };

                scope.harFormatteringsFeil = function () {
                    var input = form[scope.name];
                    return input && input.$error.dateFormat && input.$touched;
                };

                scope.erUloveligFremtidigDato = function() {
                    var input = form[scope.name];
                    return input && input.$error.futureDate && input.$touched;
                };

                scope.erIkkeGyldigDato = function () {
                    var input = form[scope.name];
                    return input && input.$error.validDate && input.$touched;
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
        };
    })
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
    .directive('datoInput', function ($timeout) {
        return {
            restrict   : 'A',
            require    : '^form',
            replace    : true,
            templateUrl: '../js/modules/datepicker/templates/customDateInputTemplate.html',
            scope      : {
                model: '=datoInput',
                harFokus: '=',
                datepickerClosed:'=',
                erRequired: '=?',
                erFremtidigdatoTillatt: '=?',
                lagre: '&',
                requiredErrorMessage: '@',
                name: '@'
            },
            link: function (scope, element, attrs, form) {
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
    })
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
                erBeggeRequired       : '=',
                erFremtidigdatoTillatt: '=',
                lagre                 : '&',
                label                 : '@'
            },
            controller : function ($scope) {
                $scope.fraLabel = $scope.label + '.fra';
                $scope.tilLabel = $scope.label + '.til';
                $scope.fraFeilmelding = $scope.fraLabel + '.feilmelding';
                $scope.tilFeilmelding = $scope.tilLabel + '.feilmelding';
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
        };
    })
    .directive('restrictInput', function ($filter, cmsService, datepickerInputService, dateService) {
        return {
            restrict: 'A',
            require : 'ngModel',
            link    : function (scope, element, attrs, ngModel) {
                if (!ngModel) {
                    return;
                }
                var datoMask = cmsService.getTrustedHtml('dato.format');
                var caretPosisjonElement = element.closest('.datepicker').find('.caretPosition');
                caretPosisjonElement.hide();

                element.bind('blur', function () {
                    caretPosisjonElement.hide();
                });

                element.bind('focus', function () {
                    caretPosisjonElement.show();
                });

                element.bind('keydown', function (event) {
                    return datepickerInputService.isValidInput(event.keyCode, element.val().length, datoMask.length, hentCaretPosisjon(element));
                });

                ngModel.$formatters.unshift(function (dato) {
                    if (dato) {
                        var datoSomDateObjekt = new Date(dato);
                        return $filter('date')(datoSomDateObjekt, 'dd.MM.yyyy');
                    } else {
                        return '';
                    }
                });

                var oldInput = '';
                ngModel.$parsers.unshift(function (input) {
                    var slettet = input.length < oldInput.length;
                    var caretPosition = hentCaretPosisjon(element);

                    if (!slettet) {
                        var res = datepickerInputService.addPeriodAtRightIndex(input, oldInput, caretPosition);
                        input = res[0];
                        caretPosition = res[1];
                    }
                    oldInput = input;
                    element.val(input);
                    settCaretPosisjon(element, caretPosition);
                    return dateService.reverseNorwegianDateFormat(input);
                });
            }
        };
    })
    .directive('datoMask', function (cssService, maskService) {
        return {
            restrict: 'A',
            link: {
                pre: function(scope, element) {
                    element.after('<span class="caretPosition"></span>');
                },
                post: function(scope, element) {
                    var inputElement = element.prev();
                    var caretPositionElement = element.next();
                    var paddingTop = cssService.getComputedStyle(inputElement, 'padding-top')
                    var inputElementLeft = inputElement.position().left + cssService.getComputedStyle(inputElement, 'padding-left');

                    scope.$watch(function() {
                        return inputElement.val();
                    }, function(newValue) {
                        var text = newValue;
                        if (text === undefined) {
                            text = '';
                        }

                        caretPositionElement.text(text);
                        var left = inputElementLeft + caretPositionElement.outerWidth();
                        var top = inputElement.position().top + paddingTop - 3;
                        element.css({top: top + 'px', left: left + 'px'});

                        element.text(maskService.getMaskText(text));
                    });
                }
            }
        }
    });
