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
                        console.log("XFDFDD" + ctrl.$errorMessages);
                        if (skalViseFlereFeilmeldinger) {
                            skalViseFlereFeilmeldinger = leggTilFeilmeldingerVedValidering(verdi, feilNokkel);
                        }
                    });

                    if (scope.feilmeldinger.length > 0) {
                        $timeout(function() {
                            scrollToElement(elem);
                        }, 1);
                    }

                    scope.$broadcast(eventString);
                }

                scope.$watch(function() { return ctrl.$error; }, function() {
                    fjernFeilmeldingerSomErFikset();
                }, true);

                function fjernFeilmeldingerSomErFikset() {
                    var fortsattFeilListe = [];

                    angular.forEach(ctrl.$error, function(verdi, feilNokkel) {
                        console.log("test" + ctrl.elem);
                        fortsattFeilListe = fortsattFeilListe.concat(leggTilFeilSomFortsattSkalVises(verdi, feilNokkel));
                    });
                    console.log("tEst" + fortsattFeilListe);
                    scope.feilmeldinger = fortsattFeilListe;
                }

                scope.skalViseFeilmeldinger = function() {
                    return scope.feilmeldinger.length > 0;
                }

                scope.scrollTilElementMedFeil = function(feilmelding) {
                    if (scope.erKlikkbarFeil(feilmelding)) {
                        scrollToElement(feilmelding.elem);
                    }
                }

                scope.erKlikkbarFeil = function(feilmelding) {
                    console.log("tEsEt" + feilmelding.elem);
                    if (feilmelding.elem != undefined)
                    {
                            return feilmelding.elem.length > 0;
                     }
                }

                /*
                 * Dersom vi har en egendefinert feil skal vi bare vise denne. I det tilfellet fjernes alle andre feilmeldinger
                 * og vi skal ikke loope mer. Return false dersom vi skal stoppe loopen, ellers true.
                 */
                function leggTilFeilmeldingerVedValidering(verdi, feilNokkel) {

                    angular.forEach(verdi, function (feil) {
                        var feilmelding = finnFeilmelding(feil, feilNokkel);

                        if (feil === undefined) { // == Egendefinert feilmelding
                            scope.feilmeldinger = [feilmelding];
                            return false;
                        }

                        leggTilFeilmeldingHvisIkkeAlleredeLagtTil(scope.feilmeldinger, feilmelding);
                    });
                    return true;
                }

                function leggTilFeilSomFortsattSkalVises(verdi, feilNokkel) {
                    console.log("tEsting" + verdi + "xx " + feilNokkel);
                    var fortsattFeilListe = [];
                    angular.forEach(verdi, function(feil) {
                        var feilmelding = finnFeilmelding(feil, feilNokkel);
                        if (scope.feilmeldinger.indexByValue(feilmelding.feil) > -1) {
                            leggTilFeilmeldingHvisIkkeAlleredeLagtTil(fortsattFeilListe, feilmelding);
                        }
                    });
                    return fortsattFeilListe;
                }

                function leggTilFeilmeldingHvisIkkeAlleredeLagtTil(fortsattFeilListe, feilmelding) {
                    if (fortsattFeilListe.indexByValue(feilmelding.feil) < 0){
                        fortsattFeilListe.push(feilmelding);
                    }
                }

                function finnFeilmelding(feil, feilNokkel) {
                    var feilmeldingNokkel = finnFeilmeldingsNokkel(feil, feilNokkel);

                    var feilmelding = data.tekster[feilmeldingNokkel];

                    if (feilmelding === undefined) {
                        return "Fant ikke feilmelding med key " + feilmeldingNokkel;
                    }

                    return {feil: feilmelding, elem: finnTilhorendeElement(feil)};
                }

                function finnFeilmeldingsNokkel(feil, feilNokkel) {

                    if (feil.$errorMessages != undefined) {
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
