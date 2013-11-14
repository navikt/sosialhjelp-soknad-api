angular.module('nav.feilmeldinger', [])

// just put <form-errors><form-errors> wherever you want form errors to be displayed!
    .directive('formErrors', ['data', '$timeout', function (data, $timeout) {
        return {
            // only works if embedded in a form or an ngForm (that's in a form).
            // It does use its closest parent that is a form OR ngForm
            require: '^form',
            templateUrl: '../js/app/directives/feilmeldinger/feilmeldingerTemplate.html',
            // TODO: Når vi får karma på jenkins, bytt ut template med templateUrl
//            template: '<ul class="form-errors" data-ng-show="skalViseFeilmeldinger()"><li class="form-error" ng-repeat="feilmelding in feilmeldinger track by $index">{{ feilmelding.feilmld }}</li></ul>',
            replace: true,
            transclude: true,
            restrict: 'AE',
            link: function postLink(scope, elem, attrs, ctrl) {
                var eventString = 'RUN_VALIDATION' + ctrl.$name;

                scope.feilmeldinger = [];
                scope.runValidation = function () {
                    scope.feilmeldinger = [];
                    var fortsettLoop = true;
                    angular.forEach(ctrl.$error, function (verdi, feilNokkel) {
                        if (fortsettLoop) {
                            fortsettLoop = leggTilFeilmeldingerVedValidering(verdi, feilNokkel);
                        }
                    });

                    if (scope.feilmeldinger.length > 0) {
                        $timeout(function() {
                            scrollToElement(elem);
                        }, 1);
                    }

                    scope.$broadcast(eventString);
//                    elem.closest('[data-ng-form]').find('.form-linje:visible').has('.ng-invalid').addClass('feil');
                }

                /*
                 * Watcher for å oppdage endringer i feilmeldinger. Legger ikke til nye feilmeldinger,
                 * men fjerner de som er fikset
                 */
                scope.$watch(function() { return ctrl.$error; }, function() {
                    var fortsattFeilListe = [];
                    angular.forEach(ctrl.$error, function(verdi, feilNokkel) {
                        fortsattFeilListe = fortsattFeilListe.concat(leggTilFeilSomFortsattSkalVises(verdi, feilNokkel));
                    });
                    scope.feilmeldinger = fortsattFeilListe;
                }, true);

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
                    return feilmelding.elem.length > 0;
                }

                /*
                 * Dersom vi har en egendefinert feil skal vi bare vise denne. I det tilfellet fjernes alle andre feilmeldinger
                 * og vi skal ikke loope mer. Return false dersom vi skal stoppe loopen, ellers true.
                 */
                function leggTilFeilmeldingerVedValidering(verdi, feilNokkel) {
                    angular.forEach(verdi, function (feil) {
                        var feilmelding = finnFeilmelding(feil, feilNokkel);

                        if (feil === undefined) {
                            // Egendefinert feilmelding
                            scope.feilmeldinger = [feilmelding];
                            return false;
                        }

                        leggTilFeilmeldingHvisIkkeAlleredeLagtTil(scope.feilmeldinger, feilmelding);
                    });
                    return true;
                }

                function leggTilFeilSomFortsattSkalVises(verdi, feilNokkel) {
                    var fortsattFeilListe = [];
                    angular.forEach(verdi, function(feil) {
                        var feilmelding = finnFeilmelding(feil, feilNokkel);

                        // Legg bare til dersom feilmeldingen vises
                        if (scope.feilmeldinger.containsObjectWithValue(feilmelding.feil)) {
                            leggTilFeilmeldingHvisIkkeAlleredeLagtTil(fortsattFeilListe, feilmelding);
                        }
                    });
                    return fortsattFeilListe;
                }

                function leggTilFeilmeldingHvisIkkeAlleredeLagtTil(liste, feilmelding) {
                    if (!liste.containsObjectWithValue(feilmelding.feil)){
                        liste.push(feilmelding);
                    }
                }

                function finnFeilmelding(feil, feilNokkel) {

                    var feilmeldingNokkel = finnFeilmeldingsNokkel(feil, feilNokkel);
                    var feilmelding = data.tekster[feilmeldingNokkel];

                    /*
                     * Dersom feilmeldingen ikke ble funnet, gi en standard tekst
                     * Skal ikke skje i produksjon, så mest for debugging
                     */
                    if (feilmelding === undefined) {
                        return "Fant ikke feilmelding med key " + feilmeldingNokkel;
                    }

                    return {feil: feilmelding, elem: finnTilhorendeElement(feil)};
                };

                function finnFeilmeldingsNokkel(feil, feilNokkel) {
                    var feilmeldingNokkel = feilNokkel;
                    if (feil) {
                        if(typeof feil.$errorMessages === 'object') {
                            feilmeldingNokkel = feil.$errorMessages[feilNokkel];
                        } else if(typeof feil.$errorMessages === 'string') {
                            feilmeldingNokkel = feil.$errorMessages;
                        }
                    }
                    return feilmeldingNokkel;
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
    }])

// set an errorMessage(s) to $errorMessages on the ngModel ctrl for later use
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
    }]);
