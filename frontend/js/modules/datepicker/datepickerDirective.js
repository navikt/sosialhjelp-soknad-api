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
    .directive('dato', function (cms) {
        return {
            restrict   : 'A',
            require    : '^form',
            replace    : true,
            templateUrl: '../js/modules/datepicker/templates/dateTemplate.html',
            scope      : {
                model: '=ngModel',
                tilDatoFeil: '=?',
                erRequired: '=?',
                erFremtidigdatoTillatt: '=?',
                tilDato: '=?',
                fraDato: '=?',
                disabled: '=?',
                tilDatoFeil: '=?',
                label: '@',
                requiredErrorMessage: '@',
                lagre: '&'

            },
            link: function (scope, element) {
                var ugyldigFremtidigDatoFeilmelding = cms.tekster['dato.ugyldigFremtidig.feilmelding'];
                scope.vars = {
                    model: scope.model,
                    harFeil: false,
                    harFokus: false,
                    harHattFokus: false,
                    datepickerClosed: true,
                    erRequired: scope.erRequired,
                    requiredErrorMessage: scope.requiredErrorMessage,
                    lagre: scope.lagre
                };

                scope.$watch('model', function(newValue, oldValue) {
                    if (newValue === oldValue) {
                        return;
                    }

                    if (new Date(newValue) < new Date(scope.fraDato)) {
                        scope.vars.model = '';
                        scope.tilDatoFeil = true;
                    } else if (new Date(scope.tilDato) < new Date(newValue)) {
                        scope.tilDato = '';
                        scope.tilDatoFeil = true;
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

                    scope.fremtidigDatoFeil = scope.sjekkUloveligFremtidigDato();
                    if (new Date(newValue) < new Date(scope.fraDato)) {
                        scope.vars.model = '';
                        scope.tilDatoFeil = true;
                    }

                    scope.model = scope.vars.model;
                });

                var datoRegExp = new RegExp(/^\d\d\.\d\d\.\d\d\d\d$/);

                function harIkkeFokusOgHarHattFokus() {
                    return !scope.vars.harFokus && scope.vars.harHattFokus;
                }

                function inputfeltHarTekstMenIkkeGyldigDatoFormat() {
                    var tekstInput = element.find('input[type=text]');
                    return tekstInput.val() && !datoRegExp.test(tekstInput.val());
                }

                function inputfeltHarTekstOgGyldigDatoFormat() {
                    var tekstInput = element.find('input[type=text]');
                    return tekstInput.val() && datoRegExp.test(tekstInput.val());
                }

                scope.navDatepicker = function() {
                    return !scope.vanligDatepicker();
                };

                scope.vanligDatepicker = function() {
                    return erTouchDevice() && getIEVersion() < 0;
                };

                scope.harRequiredFeil = function () {
                    if (scope.navDatepicker()) {
                        return erRequiredOgHarIkkeModellSattOgHarIkkeTilDatoFeil() && inputHarIkkeInnhold() && harIkkeFokusOgHarHattFokus() && scope.vars.datepickerClosed &&
                            !inputfeltHarTekstMenIkkeGyldigDatoFormat() && !erGyldigDato(element.find('input[type=text]').val());
                    } else {
                        return erRequiredOgHarIkkeModellSattOgHarIkkeTilDatoFeil() && harIkkeFokusOgHarHattFokus() ;
                    }

                    function inputHarIkkeInnhold() {
                        return element.find('input[type=text]').val() === undefined || element.find('input[type=text]').val().trim().length === 0;
                    }

                    function erRequiredOgHarIkkeModellSattOgHarIkkeTilDatoFeil() {
                        return scope.vars.erRequired && !scope.vars.model && !scope.tilDatoFeil;
                    }
                };

                scope.harTilDatoFeil = function () {
                    return !scope.vars.model && harIkkeFokusOgHarHattFokus() && scope.vars.datepickerClosed && scope.tilDatoFeil;
                };

                scope.harFormatteringsFeil = function () {
                    return inputfeltHarTekstMenIkkeGyldigDatoFormat() && harIkkeFokusOgHarHattFokus();
                };

                scope.sjekkUloveligFremtidigDato = function () {
                    if(!scope.erFremtidigdatoTillatt && scope.vars.model !== undefined) {
                        var dateArray = scope.vars.model.split("-");
                        return erFremtidigDato(dateArray[0], dateArray[1], dateArray[2]);
                    }
                    return false;
                };

                scope.erUloveligFremtidigDato = function() {
                    var el;
                    if(scope.fremtidigDatoFeil && harIkkeFokusOgHarHattFokus()) {
                        el = element.controller('ngModel');
                        el.$setValidity(ugyldigFremtidigDatoFeilmelding, false);
                        return true;
                    } else if(!scope.fremtidigDatoFeil) {
                        el = element.controller('ngModel');
                        el.$setValidity(ugyldigFremtidigDatoFeilmelding, true);
                        return false;
                    }
                    return false;
                };

                scope.erIkkeGyldigDato = function () {
                    return !scope.ngModel && inputfeltHarTekstOgGyldigDatoFormat() &&
                        !erGyldigDato(element.find('input[type=text]').val()) && harIkkeFokusOgHarHattFokus();
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
    .directive('datoInput', function ($timeout, deviceService) {
        return {
            restrict   : 'A',
            require    : '^form',
            replace    : true,
            templateUrl: '../js/modules/datepicker/templates/customDateInputTemplate.html',
            scope      : {
                model: '=datoInput',
                harHattFokus: '=',
                harFokus: '=',
                erRequired: '=?',
                lagre: '&',
                requiredErrorMessage: '@'
            },
            link: function (scope, element, attrs, form) {
                var eventForAValidereHeleFormen = 'RUN_VALIDATION' + form.$name;
                scope.$on(eventForAValidereHeleFormen, function () {
                    scope.harHattFokus = true;
                });

                scope.blur = function () {
                    scope.harFokus = false;

                    if (new Date(scope.model) < new Date(scope.fraDato)) {
                        scope.model = '';
                        scope.tilDatoFeil = true;
                    } else if (new Date(scope.tilDato) < new Date(scope.model)) {
                        scope.tilDato = '';
                        scope.tilDatoFeil = true;
                    }

                    if (scope.lagre) {
                        $timeout(scope.lagre, 100);
                    }
                };

                scope.focus = function () {
                    scope.harFokus = true;
                    scope.harHattFokus = true;
                    if (scope.tilDatoFeil !== undefined) {
                        scope.tilDatoFeil = false;
                    }
                };

                scope.visible = function() {
                    return deviceService.isTouchDevice();
                };
            }
        };
    })
    .directive('htmlDatepicker', function () {
        return {
            restrict: 'A',
            require: '^form',
            replace: true,
            templateUrl: '../js/modules/datepicker/templates/html5DatepickerTemplate.html',
            scope: {
                model: '=htmlDatepicker',
                harHattFokus: '=',
                harFokus: '=',
                erRequired: '=?',
                lagre: '&',
                requiredErrorMessage: '@'
            },
            link: function(scope) {
                scope.blur = function () {
                    scope.harFokus = false;

                    if (new Date(scope.model) < new Date(scope.fraDato)) {
                        scope.model = '';
                        scope.tilDatoFeil = true;
                    } else if (new Date(scope.tilDato) < new Date(scope.model)) {
                        scope.tilDato = '';
                        scope.tilDatoFeil = true;
                    }

                    if (scope.lagre) {
                        $timeout(scope.lagre, 100);
                    }
                };

                scope.focus = function () {
                    scope.harFokus = true;
                    scope.harHattFokus = true;
                    if (scope.tilDatoFeil !== undefined) {
                        scope.tilDatoFeil = false;
                    }
                };
            }
        };
    })
    .directive('navDato', function ($timeout, datepickerConfig, $filter, cms) {
        return {
            restrict   : 'A',
            require    : '^form',
            replace    : true,
            templateUrl: '../js/modules/datepicker/templates/singleDatepickerTemplate.html',
            scope      : {
                ngModel               : '=',
                erRequired            : '=',
                tilDato               : '=',
                fraDato               : '=',
                tilDatoFeil           : '=',
                erFremtidigdatoTillatt: '=',
                aarBegrensning        : '=',
                endret                : '&',
                lagre                 : '&',
                label                 : '@',
                requiredErrorMessage  : '@'
            },
            link: function (scope, element, attrs, form) {
                scope.vars = {
                    date: scope.ngModel
                };

                var eventForAValidereHeleFormen = 'RUN_VALIDATION' + form.$name;
                var datoRegExp = new RegExp(/^\d\d\.\d\d\.\d\d\d\d$/);
                var harHattFokus = false;
                var datepickerErLukket = true;
                var ugyldigFremtidigDatoFeilmelding = cms.tekster['dato.ugyldigFremtidig.feilmelding'];

                scope.harFokus = false;

                scope.options = {};
                if (!scope.erFremtidigdatoTillatt) {
                    scope.options.maxDate = new Date();
                }


                scope.navDatepicker = function() {
                    return !scope.vanligDatepicker();
                };

                scope.vanligDatepicker = function() {
                    return erTouchDevice() && getIEVersion() < 0;
                };

                scope.toggleDatepicker = function () {
                    var dateDiv = $('#ui-datepicker-div');
                    if (dateDiv.is(':hidden')) {
                        element.find('input[type=hidden]').datepicker('show');
                        var pos = dateDiv.position();
                        pos.top = pos.top + 32;
                        dateDiv.offset(pos);
                    }
                };

                scope.blur = function () {
                    scope.harFokus = false;

                    if (new Date(scope.vars.date) < new Date(scope.fraDato)) {
                        scope.vars.date = '';
                        scope.tilDatoFeil = true;
                    } else if (new Date(scope.tilDato) < new Date(scope.vars.date)) {
                        scope.ngModel = scope.vars.date;
                        scope.tilDato = '';
                        scope.tilDatoFeil = true;
                    } else {
                        scope.ngModel = scope.vars.date;
                    }

                    if (scope.lagre) {
                        $timeout(scope.lagre, 100);
                    }
                };

                scope.focus = function () {
                    scope.harFokus = true;
                    harHattFokus = true;
                    if (scope.tilDatoFeil !== undefined) {
                        scope.tilDatoFeil = false;
                    }
                };

                scope.$on(eventForAValidereHeleFormen, function () {
                    harHattFokus = true;
                });

                scope.harRequiredFeil = function () {
                    if (scope.navDatepicker()) {
                        return erRequiredOgHarIkkeModellSattOgHarIkkeTilDatoFeil() && inputHarIkkeInnhold() && harIkkeFokusOgHarHattFokus() && datepickerErLukket &&
                            !inputfeltHarTekstMenIkkeGyldigDatoFormat() && !erGyldigDato(element.find('input[type=text]').val());
                    } else {
                        return erRequiredOgHarIkkeModellSattOgHarIkkeTilDatoFeil() && harIkkeFokusOgHarHattFokus() ;
                    }

                    function inputHarIkkeInnhold() {
                        return element.find('input[type=text]').val() === undefined || element.find('input[type=text]').val().trim().length === 0;
                    }

                    function erRequiredOgHarIkkeModellSattOgHarIkkeTilDatoFeil() {
                        return scope.erRequired && !scope.ngModel && !scope.tilDatoFeil;
                    }
                };

                scope.harTilDatoFeil = function () {
                    return !scope.ngModel && harIkkeFokusOgHarHattFokus() && datepickerErLukket && scope.tilDatoFeil;
                };

                scope.harFormatteringsFeil = function () {
                    return inputfeltHarTekstMenIkkeGyldigDatoFormat() && harIkkeFokusOgHarHattFokus();
                };

                scope.sjekkUloveligFremtidigDato = function () {
                    if(!scope.erFremtidigdatoTillatt && scope.ngModel !== undefined) {
                        var dateArray = scope.ngModel.split("-");
                        return erFremtidigDato(dateArray[0], dateArray[1], dateArray[2]);
                    }
                    return false;
                };

                scope.erUloveligFremtidigDato = function() {
                    var el;
                    if(scope.fremtidigDatoFeil && harIkkeFokusOgHarHattFokus()) {
                        el = element.controller('ngModel');
                        el.$setValidity(ugyldigFremtidigDatoFeilmelding, false);
                        return true;
                    } else if(!scope.fremtidigDatoFeil) {
                        el = element.controller('ngModel');
                        el.$setValidity(ugyldigFremtidigDatoFeilmelding, true);
                        return false;
                    }
                    return false;
                };

                scope.erIkkeGyldigDato = function () {
                    return !scope.ngModel && inputfeltHarTekstOgGyldigDatoFormat() &&
                        !erGyldigDato(element.find('input[type=text]').val()) && harIkkeFokusOgHarHattFokus();
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

                scope.$watch('ngModel', function (newVal, oldVal) {
                    if (newVal === oldVal) {
                        return;
                    }

                    if (newVal !== oldVal && scope.endret) {
                        scope.endret();
                    }

                    if (isNaN(new Date(newVal).getDate()) && !isNaN(new Date(oldVal))) {
                        scope.vars.date = '';
                    }

                    scope.fremtidigDatoFeil = scope.sjekkUloveligFremtidigDato();
                    if (new Date(scope.ngModel) < new Date(scope.fraDato)) {
                        scope.ngModel = '';
                        scope.vars.date = '';
                        scope.tilDatoFeil = true;
                    } else if (new Date(scope.tilDato) < new Date(scope.ngModel)) {
                        scope.tilDato = '';
                        scope.tilDatoFeil = true;
                    }

                    var datepickerInput = element.find('input[type=hidden]');
                    if (isNaN(new Date(scope.ngModel).getDate())) {
                        datepickerInput.datepicker('setDate', new Date());
                    } else {
                        datepickerInput.datepicker('setDate', new Date(scope.ngModel));
                    }
                });

                function inputfeltHarTekstOgGyldigDatoFormat() {
                    var tekstInput = element.find('input[type=text]');
                    return tekstInput.val() && datoRegExp.test(tekstInput.val());
                }

                function inputfeltHarTekstMenIkkeGyldigDatoFormat() {
                    var tekstInput = element.find('input[type=text]');
                    return tekstInput.val() && !datoRegExp.test(tekstInput.val());
                }

                var defaultDate = new Date();

                function datepickerOptions() {
                    var currentDefaultDate = defaultDate;
                    defaultDate = scope.ngModel ? new Date(scope.ngModel) : currentDefaultDate;

                    if (currentDefaultDate.getTime() !== defaultDate.getTime()) {
                        scope.options = angular.extend({}, {defaultDate: defaultDate}, scope.options);
                    }
                    return angular.extend({}, datepickerConfig, scope.options);
                }

                function leggTilDatepicker() {
                    var datepickerInput = element.find('input[type=hidden]');
                    var opts = datepickerOptions();

                    opts.onSelect = function () {
                        var dato = datepickerInput.datepicker('getDate');
                        scope.ngModel = $filter('date')(dato, 'yyyy-MM-dd');
                        scope.vars.date = scope.ngModel;
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
                }
                // Legger til datepicker på nytt dersom options endrer seg
                scope.$watch(datepickerOptions, leggTilDatepicker, true);

                function harIkkeFokusOgHarHattFokus() {
                    return !scope.harFokus && harHattFokus;
                }
            }
        };
    })
    .directive('navDatoIntervall', [function () {
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
    }])
    .directive('datoMaskOld', function ($filter, cms) {
        return {
            restrict: 'A',
            require : 'ngModel',
            link    : function (scope, element, attrs, ngModel) {
                if (!ngModel) {
                    return;
                }

                var datoMask = cms.tekster['dato.format'];
                var caretPosisjonElement = element.closest('.datepicker').find('.caretPosition');
                var maskElement = element.next();
                var inputElementVenstre = element.position().left + 7;
                var topp = getTopp();
                var venstre = inputElementVenstre;
                maskElement.css({top: topp + 'px', left: venstre + 'px'});
                maskElement.text(datoMask);
                caretPosisjonElement.hide();

                element.bind('blur', function () {
                    caretPosisjonElement.hide();
                });

                element.bind('focus', function () {
                    caretPosisjonElement.show();
                });

                element.bind('keydown', function (event) {
                    return event.keyCode !== 32;
                });

                ngModel.$formatters.unshift(function (dato) {
                    if (dato) {
                        var datoSomDateObjekt = new Date(dato);
                        return $filter('date')(datoSomDateObjekt, 'dd.MM.yyyy');
                    } else {
                        return '';
                    }
                });

                var gammelInputVerdi = '';
                ngModel.$parsers.unshift(function (input) {
                    var slettet = input.length < gammelInputVerdi.length;
                    var caretPosisjon = hentCaretPosisjon(element);

                    if (!slettet) {
                        var start = caretPosisjon - (input.length - gammelInputVerdi.length);
                        var slutt = caretPosisjon;

                        for (var i = start; i < slutt && i < input.length; i++) {
                            var skrevetTegn = input[i];

                            if(slettTegnDersomDetIkkeStemmerMedFormatet()) {
                                continue;
                            }

                            settInnPunktumDeromVedIndex1Eller4();
                        }
                    }

                    gammelInputVerdi = input;
                    element.val(input);
                    settCaretPosisjon(element, caretPosisjon);

                    return reverserNorskDatoformat(input);

                    function settInnPunktumDeromVedIndex1Eller4() {
                        if (i === 1 || i === 4) {
                            if (input[i + 1] === '.') {
                                caretPosisjon++;
                                i++;
                            } else {
                                input = input.splice(i + 1, 0, '.');
                                caretPosisjon++;
                                i++;
                                slutt++;
                            }
                        }
                    }

                    function slettTegnDersomDetIkkeStemmerMedFormatet() {
                        if (isNaN(skrevetTegn) || input.substring(0, i + 1).length > datoMask.length || input.splice(i, 1, '').length === datoMask.length) {
                            if (skrevetTegn !== '.' || (i !== 2 && i !== 5)) {
                                input = input.splice(i, 1, '');
                                caretPosisjon--;
                                i--;
                                slutt--;
                                return true;
                            }
                        }
                        return false;
                    }
                });

                scope.$watch(
                    function () {
                        return ngModel.$viewValue;
                    },
                    function (nyVerdi) {
                        var tekst = nyVerdi;
                        if (nyVerdi === undefined) {
                            tekst = '';
                        }

                        gammelInputVerdi = tekst;
                        caretPosisjonElement.text(tekst);
                        venstre = inputElementVenstre + caretPosisjonElement.outerWidth();
                        maskElement.css({top: getTopp() + 'px', left: venstre + 'px'});

                        var antallPunktum = tekst.match(/\./g) === null ? 0 : tekst.match(/\./g).length;
                        var maskTekst = '';
                        var maanedTekst = tekst.substring(tekst.indexOf('.'), tekst.length);
                        var aarTekst = tekst.substring(tekst.lastIndexOf('.'), tekst.length);

                        if (skalViseDatoFormatFraOgMedDag()) {
                            maskTekst = datoMask.substring(tekst.length, datoMask.length);
                        } else if (skalViseDatoFormatFraOgMedMaaned()) {
                            maskTekst = datoMask.substring(2 + maanedTekst.length, datoMask.length);
                        } else if (skalBareViseDatoFormatMedAar()) {
                            maskTekst = datoMask.substring(5 + aarTekst.length, datoMask.length);
                        }
                        maskElement.text(maskTekst);

                        function skalViseDatoFormatFraOgMedDag() {
                            return antallPunktum === 0 && tekst.length < 3;
                        }

                        function skalViseDatoFormatFraOgMedMaaned() {
                            var dagTekst = tekst.substring(0, tekst.indexOf('.'));
                            return antallPunktum === 1 && dagTekst.length < 3 && maanedTekst.length < 4;
                        }

                        function skalBareViseDatoFormatMedAar() {
                            var dagOgMaanedTekst = tekst.substring(0, tekst.lastIndexOf('.'));
                            return antallPunktum === 2 && dagOgMaanedTekst.length < 6 && aarTekst.length < 5;
                        }
                    }
                );

                function getTopp() {
                    return element.position().top + 6;
                }
            }
        };
    })
    .directive('restrictInput', function ($filter, cmsService, datepickerInputService) {
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

                    return reverserNorskDatoformat(input);
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
