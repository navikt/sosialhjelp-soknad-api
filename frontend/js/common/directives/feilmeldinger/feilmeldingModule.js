angular.module('nav.feilmeldinger', [])
    // settes på inputfeltene som skal gi feilmeldinger
    .directive('errorMessages', [function () {
        return {
            require: 'ngModel',
            link: function (scope, elem, attrs, ctrl) {
                // attrs.errorMessages can be:
                //    1) "must be filled out."
                //    2) "'must be filled out.'"
                //    3) "{ required: 'must be filled out.' }"
                try {
                    ctrl.$errorMessages = scope.$eval(attrs.errorMessages);
                } catch (e) {
                    ctrl.$errorMessages = attrs.errorMessages;
                }
                ctrl.$elementErrorAttr = attrs.errorMessages;
            }
        };
    }])

    // For å bruke, legg til <div form-errors></div>
    .directive('formErrors', ['cmsService', '$timeout', function (cmsService, $timeout) {
        return {
            // only works if embedded in a form or an ngForm (that's in a form).
            // It does use its closest parent that is a form OR ngForm
            require: '^form',
            templateUrl: '../js/common/directives/feilmeldinger/feilmeldingerTemplate.html',
            replace: true,
            transclude: true,
            restrict: 'A',
            link: function postLink(scope, elem, attrs, ctrl) {
                var eventString = 'RUN_VALIDATION' + ctrl.$name;

                scope.skalViseFeilmeldingerMedSammeTekst = attrs.visFlereLikeFeilmeldinger === "true";
                scope.feilmeldinger = [];
                scope.runValidation = function (skalScrolle) {
                    scope.$broadcast(eventString);
                    scope.broadcastValideringTilSubforms(ctrl);

                    $timeout(function() {
                        scope.feilmeldinger = [];
                        var skalViseFlereFeilmeldinger = true;
                        angular.forEach(ctrl.$error, function (verdi, feilNokkel) {
                            if (skalViseFlereFeilmeldinger) {
                                skalViseFlereFeilmeldinger = leggTilFeilmeldingerVedValidering(verdi, feilNokkel);
                            }
                        });

                        if (scope.feilmeldinger.length > 0 && skalScrolle) {
                            $timeout(function () {
                                scrollToElement(elem, 100);
                            }, 1);
                        }
                    }, 50);

                    return ctrl.$valid;
                };

                scope.$watch(function () {
                    return ctrl.$error;
                }, function () {
                    scope.fjernFeilmeldingerSomErFikset();
                }, true);

                scope.fjernFeilmeldingerSomErFikset = function () {
                    var fortsattFeilListe = [];
                    angular.forEach(ctrl.$error, function (verdi, feilNokkel) {
                        fortsattFeilListe = fortsattFeilListe.concat(leggTilFeilSomFortsattSkalVises(verdi, feilNokkel, []));
                    });
                    scope.feilmeldinger = fortsattFeilListe;
                };

                scope.skalViseFeilmeldinger = function () {
                    return scope.feilmeldinger.length > 0;
                };

                scope.scrollTilElementMedFeil = function (feilmelding) {
                    if (scope.erKlikkbarFeil(feilmelding)) {
                        var formLinje = feilmelding.elem.closest('.form-linje');
                        scrollToElement(formLinje, 250);
                        if (formLinje.hasClass("andelsfordeling-container")) {
                            formLinje.find('.form-linje').first().addClass('aktiv-feilmelding');
                        } else {
                            formLinje.addClass('aktiv-feilmelding');
                        }

                        if (feilmelding.elem.is('[type=hidden]')) {
                            if (feilmelding.elem.hasClass('tekstfelt')) {
                                scope.giFokus(formLinje.find('input[type=text]').filter(':visible').first());
                            } else if (feilmelding.elem.hasClass('under-atten-dato')) {
                                scope.giFokus(formLinje.find('input[type=text]').first());
                            } else if (feilmelding.elem.hasClass('hidden-vedlegg')) {
                                scope.giFokus(formLinje.find('a.knapp-link').first());
                            } else if (feilmelding.elem.hasClass('legg-til-arbeidsforhold')) {
                                scope.giFokus(formLinje.find('button'));
                            }
                            else {
                                scope.giFokus(formLinje.find('input[type=checkbox]').first());
                            }
                        } else {
                            scope.giFokus(feilmelding.elem);
                        }
                    }
                };

                /*
                 Ved ng-repeat så må vi sjekke hvilket element som inneholder feil først. Sjekker at lengden er større
                 enn 1 for at checkbokser som bruker hidden-felt og ikke har klassen ng-invalid får riktig fokus
                 */
                scope.giFokus = function (element) {
                    $timeout(function () {
                        if (typeof element === 'object' && element.length > 1) {
                            for (var i = 0; i < element.length; i++) {
                                if ($(element[i]).hasClass('ng-invalid')) {
                                    element[i].focus();
                                    return;
                                }
                            }
                        }
                        else {
                            element.focus();
                        }
                    });

                };

                scope.erKlikkbarFeil = function (feilmelding) {
                    return feilmelding.elem && feilmelding.elem.length > 0;
                };

                scope.broadcastValideringTilSubforms = function(controller) {
                    angular.forEach(controller.$error, function(verdi) {
                        if(Array.isArray(verdi)){
                            angular.forEach(verdi, function(subform) {
                                scope.$broadcast("RUN_VALIDATION" + subform.$name);
                            });
                        }
                    });
                };

                /*
                 * Dersom vi har en egendefinert feil med der $skalVisesAlene er satt til true så skal kun denne feilmeldingen vises. I det tilfellet fjernes alle andre feilmeldinger
                 * og vi skal ikke loope mer. Return false dersom vi skal stoppe loopen, ellers true.
                 */
                function leggTilFeilmeldingerVedValidering(verdi, feilNokkel) {
                    var skalViseFlereFeilmeldinger = true;
                    angular.forEach(verdi, function (feil) {
                        var feilmeldinger = finnFeilmelding(feil, feilNokkel);
                        angular.forEach(feilmeldinger, function(feilmelding) {
                            if (feil && feil.$skalVisesAlene === true && skalViseFlereFeilmeldinger) {
                                scope.feilmeldinger = [feilmelding];
                                skalViseFlereFeilmeldinger = false;
                            } else if (skalViseFlereFeilmeldinger && feil && feilmelding) {
                                leggTilFeilmeldingHvisIkkeAlleredeLagtTil(scope.feilmeldinger, feilmelding);
                            }
                        });
                    });
                    return skalViseFlereFeilmeldinger;
                }

                function leggTilFeilSomFortsattSkalVises(verdi, feilNokkel, fortsattFeilListe) {
                    angular.forEach(verdi, function (feil) {
                        var feilmeldinger = finnFeilmelding(feil, feilNokkel);

                        angular.forEach(feilmeldinger, function(feilmelding){
                            if (feilmelding === undefined && feilErSubform(feil, feilNokkel)) {
                                leggTilFeilSomFortsattSkalVises(feil.$error[feilNokkel], feilNokkel, fortsattFeilListe);
                            } else if (feilmelding && !skalLeggeTilFeilIFeillisten(scope.feilmeldinger, feilmelding) && feil) {
                                leggTilFeilmeldingHvisIkkeAlleredeLagtTil(fortsattFeilListe, feilmelding);
                            }
                        });
                    });
                    return fortsattFeilListe;
                }

                function leggTilFeilmeldingHvisIkkeAlleredeLagtTil(feilListe, feilmelding) {
                    if (skalLeggeTilFeilIFeillisten(feilListe, feilmelding)) {
                        feilListe.push(feilmelding);
                    }
                }

                function skalLeggeTilFeilIFeillisten(feilliste, feilmelding) {
                    var skalLeggeTilFeilIListe = true;
                    angular.forEach(feilliste, function(feil){
                        if(feil.elem.is(feilmelding.elem) || (!scope.skalViseFeilmeldingerMedSammeTekst && (feilliste.indexByValue(feilmelding.feil) > -1))) {
                            skalLeggeTilFeilIListe = false;
                        }
                    });
                    return skalLeggeTilFeilIListe;
                }

                function finnFeilmelding(feil, feilNokkel) {
                    var feilmeldingNokkel = finnFeilmeldingsNokkel(feil, feilNokkel);
                    var feilmelding = cmsService.getTextSafe(feilmeldingNokkel);
                    if (feilmelding === undefined) {
                        if (feilErSubform(feil, feilNokkel)) {
                            return finnFeilmelding(feil.$error[feilNokkel][0], feilNokkel);
                        } else {
                            feilmelding = feilmeldingNokkel;
                        }
                    }

                    var feilFunnet = [];
                    angular.forEach(finnTilhorendeElement(feil), function(element){
                        feilFunnet.push({feil: feilmelding, elem: $(element)});
                    });
                    return feilFunnet;
                }

                function feilErSubform(feil, feilNokkel) {
                    return feil.$error && Array.isArray(feil.$error[feilNokkel]);
                }

                function finnFeilmeldingsNokkel(feil, feilNokkel) {
                    if (feil && feil.$errorMessages !== undefined) {
                        if (typeof feil.$errorMessages === 'object') {
                            return feil.$errorMessages[feilNokkel];
                        } else if (typeof feil.$errorMessages === 'string') {
                            return feil.$errorMessages;
                        }
                    }
                    return feilNokkel;
                }

                function finnTilhorendeElement(feil) {
                    if (feil && feil.$linkId) {
                        return elem.closest('[data-ng-form]').find('[name=' + feil.$linkId + ']');
                    }
                    if (feil && feil.$elementErrorAttr) {
                        return elem.closest('[data-ng-form]').find(".feil, .feilstyling").find("[data-error-messages=\"" + feil.$elementErrorAttr + "\"], [error-messages=\"" + feil.$elementErrorAttr + "\"]");
                    }
                }
            }
        };
    }
    ])
    .directive('aktivFeilmelding', ['$timeout', function ($timeout) {
        return {
            link: function (scope, element) {
                $(element).bind('blur', function () {
                    var formLinje = $(element).closest('.form-linje');
                    if (formLinje.hasClass('aktiv-feilmelding')) {
                        $timeout(function () {
                            formLinje.removeClass('aktiv-feilmelding');
                        }, 100);
                    }
                });
            }
        };
    }]);

