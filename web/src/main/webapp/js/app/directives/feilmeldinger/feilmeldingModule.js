angular.module('nav.feilmeldinger', [])

    // settes på inputfeltene som skal gi feilmeldinger
    .directive('errorMessages', [function () {
        return {
            require: 'ngModel',
            link: function(scope, elem, attrs, ctrl) {
                // attrs.errorMessages can be:
                //    1) "must be filled out."
                //    2) "'must be filled out.'"
                //    3) "{ required: 'must be filled out.' }"
                try {
                    ctrl.$errorMessages = scope.$eval(attrs.errorMessages);
                } catch(e) {
                    ctrl.$errorMessages = attrs.errorMessages;
                }
            }
        };
    }])

    // For å bruke, legg til <div form-errors></div>
    .directive('formErrors', ['data', '$timeout', function (data, $timeout) {
        return {
            // only works if embedded in a form or an ngForm (that's in a form).
            // It does use its closest parent that is a form OR ngForm
            require: '^form',
            templateUrl: '../js/app/directives/feilmeldinger/feilmeldingerTemplate.html',
            replace: true,
            transclude: true,
            restrict: 'A',
            link: function postLink(scope, elem, attrs, ctrl) {
                var eventString = 'RUN_VALIDATION' + ctrl.$name;

                scope.feilmeldinger = [];
                scope.runValidation = function () {
                    scope.feilmeldinger = [];
                    var skalViseFlereFeilmeldinger = true;

                    angular.forEach(ctrl.$error, function (verdi, feilNokkel) {
                        if (skalViseFlereFeilmeldinger) {
                            skalViseFlereFeilmeldinger = leggTilFeilmeldingerVedValidering(verdi, feilNokkel);
                        }
                    });

                    if (scope.feilmeldinger.length > 0) {
                        $timeout(function() {
                            scrollToElement(elem);
                        }, 1);
                    }

                    if ( skalViseFlereFeilmeldinger) {
                        scope.$broadcast(eventString);
                    }
                }

                scope.$watch(function() { return ctrl.$error; }, function() {
                    scope.fjernFeilmeldingerSomErFikset();
                }, true);

                scope.fjernFeilmeldingerSomErFikset = function() {
                    var fortsattFeilListe = [];
                    angular.forEach(ctrl.$error, function(verdi, feilNokkel) {
                        fortsattFeilListe = fortsattFeilListe.concat(leggTilFeilSomFortsattSkalVises(verdi, feilNokkel));
                    });
                    scope.feilmeldinger = fortsattFeilListe;
                }

                scope.skalViseFeilmeldinger = function() {
                    return scope.feilmeldinger.length > 0;
                }

                scope.scrollTilElementMedFeil = function(feilmelding) {
                    if (scope.erKlikkbarFeil(feilmelding)) {
                        scrollToElement(feilmelding.elem);
                        scope.giFokus(feilmelding.elem);
                    }
                }

                scope.giFokus = function(element) {
                    element.focus();
                }

                scope.erKlikkbarFeil = function(feilmelding) {
                    return feilmelding.elem && feilmelding.elem.length > 0;
                }

                /*
                 * Dersom vi har en egendefinert feil med der $skalVisesAlene er satt til true så skal kun denne feilmeldingen vises. I det tilfellet fjernes alle andre feilmeldinger
                 * og vi skal ikke loope mer. Return false dersom vi skal stoppe loopen, ellers true.
                 */
                function leggTilFeilmeldingerVedValidering(verdi, feilNokkel) {
                    var skalViseFlereFeilmeldinger = true;
                    angular.forEach(verdi, function (feil) {
                        var feilmelding = finnFeilmelding(feil, feilNokkel);

                        if (feil && feil.$skalVisesAlene === true && skalViseFlereFeilmeldinger) { // == Egendefinert feilmelding
                            scope.feilmeldinger = [feilmelding];
                            skalViseFlereFeilmeldinger = false;
                        } else if (skalViseFlereFeilmeldinger && feil) {
                            leggTilFeilmeldingHvisIkkeAlleredeLagtTil(scope.feilmeldinger, feilmelding);
                        }

                    });
                    return skalViseFlereFeilmeldinger;
                }

                function leggTilFeilSomFortsattSkalVises(verdi, feilNokkel) {
                    var fortsattFeilListe = [];
                    angular.forEach(verdi, function(feil) {
                        var feilmelding = finnFeilmelding(feil, feilNokkel);
                        if (scope.feilmeldinger.indexByValue(feilmelding.feil) > -1 && feil) {
                            leggTilFeilmeldingHvisIkkeAlleredeLagtTil(fortsattFeilListe, feilmelding);
                        }
                    });
                    return fortsattFeilListe;
                }

                function leggTilFeilmeldingHvisIkkeAlleredeLagtTil(feilListe, feilmelding) {
                    if (feilListe.indexByValue(feilmelding.feil) < 0){
                        feilListe.push(feilmelding);
                    }
                }

                function finnFeilmelding(feil, feilNokkel) {
                    var feilmeldingNokkel = finnFeilmeldingsNokkel(feil, feilNokkel);
                    var feilmelding = data.tekster[feilmeldingNokkel];
                    if (feilmelding === undefined) {
                        return {feil: "Fant ikke feilmelding med key " + feilmeldingNokkel, elem: finnTilhorendeElement(feil)};
                    }
                    return {feil: feilmelding, elem: finnTilhorendeElement(feil)};
                }

                function finnFeilmeldingsNokkel(feil, feilNokkel) {
                    if (feil) {
                        if(typeof feil.$errorMessages === 'object') {
                           return feil.$errorMessages[feilNokkel];
                        } else if(typeof feil.$errorMessages === 'string') {
                            return feil.$errorMessages;
                        }
                    }
                    return feilNokkel;
                }

                function finnTilhorendeElement(feil) {
                    var navn = '';
                    if (feil) {
                        navn = feil.$name;
                    }
                    return elem.closest('[data-ng-form]').find('[name=' + navn + ']');
                }
            }
        };
    }]);
