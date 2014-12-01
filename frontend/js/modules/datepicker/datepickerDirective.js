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
    .directive('dato', function () {
        return {
            restrict   : 'A',
            require    : '^form',
            replace    : true,
            templateUrl: '../js/modules/datepicker/templates/datepickerTemplate.html',
            scope      : {
                model: '=ngModel'
            },
            link: function (scope, element, attrs, form) {

            }
        };
    })
    .directive('datoInput', function () {
        return {
            restrict   : 'A',
            require    : '^form',
            replace    : true,
            templateUrl: '../js/modules/datepicker/templates/customDateInputTemplate.html',
            scope      : {
                model: '=datoInput'
            },
            link: function (scope, element, attrs, form) {

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
    .directive('datoMask', function ($filter, cms) {
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
    .directive('restrictInput', function ($filter, cms, datepickerInputKeys, datepickerNonInputKeys, datepickerInputService) {
        return {
            restrict: 'A',
            require : 'ngModel',
            link    : function (scope, element, attrs, ngModel) {
                if (!ngModel) {
                    return;
                }

                var allowedKeys = datepickerInputKeys.concat(datepickerNonInputKeys);
                var datoMask = cms.tekster['dato.format'];
                var caretPosisjonElement = element.closest('.datepicker').find('.caretPosition');
                caretPosisjonElement.hide();

                element.bind('blur', function () {
                    caretPosisjonElement.hide();
                });

                element.bind('focus', function () {
                    caretPosisjonElement.show();
                });

                element.bind('keydown', function (event) {
                    return datepickerInputService.isValidInput(event.keyCode, element.val().length, datoMask.length);
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
                        if (input.substring(0, i + 1).length > datoMask.length || input.splice(i, 1, '').length === datoMask.length) {
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
            }
        };
    });

