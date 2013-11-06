angular.module('FormErrors', ['app.services'])

// just put <form-errors><form-errors> wherever you want form errors to be displayed!
.directive('formErrors', ['data', function (data) {
    return {
        // only works if embedded in a form or an ngForm (that's in a form).
        // It does use its closest parent that is a form OR ngForm
        require: '^form',
        template:
            '<ul class="form-errors" data-ng-show="skalViseFeilmeldinger()">' +
                '<li class="form-error" ng-repeat="feilmelding in feilmeldinger track by $index">' +
                    '{{ feilmelding }}' +
                '</li>' +
            '</ul>',
        replace: true,
        transclude: true,
        restrict: 'AE',
        link: function postLink(scope, elem, attrs, ctrl) {
            scope.feilmeldinger = [];
            scope.runValidation = function () {
                scope.feilmeldinger = [];
                var fortsettLoop = true;

                angular.forEach(ctrl.$error, function (verdi, feilNokkel) {
                    if (fortsettLoop) {
                        fortsettLoop = leggTilFeilmeldingerVedValidering(verdi, feilNokkel);
                    }
                });
            }

            /*
             * Watcher på feilmeldinger for å oppdage endringer. Legger alle feilmeldinger som fortsatt gjelder OG som
             * ligger i scope.feilmeldinger i en list som scope.feilmeldinger blir satt til. Er for å fjerne feil som blir
             * fikset, men IKKE legge til nye feil i listen.
             */
            scope.$watch(function() { return ctrl.$error; }, function() {
                var fortsattFeilListe = [];
                angular.forEach(ctrl.$error, function(verdi, feilNokkel) {
                    fortsattFeilListe = fortsattFeilListe.concat(leggTilFeilSomFortsattSkalVises(verdi, feilNokkel));
                });
                scope.feilmeldinger = fortsattFeilListe;
            }, true);

            scope.skalViseFeilmeldinger = function() {
                var skalViseFeilmeldinger = elem.children().length;
                return skalViseFeilmeldinger;
            }

            /*
             * Dersom vi har en egendefinert feil skal vi bare vise denne. I det tilfellet fjernes alle andre feilmeldinger
             * og vi skal ikke loope mer. Return false dersom vi skal stoppe loopen, ellers true.
             */
            function leggTilFeilmeldingerVedValidering(verdi, feilNokkel) {
                angular.forEach(verdi, function (feil) {
                    var feilmelding = hentFeilmelding(feil, feilNokkel);

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
                    var feilmelding = hentFeilmelding(feil, feilNokkel);

                    // Legg bare til dersom feilmeldingen vises
                    if (scope.feilmeldinger.contains(feilmelding)) {
                        leggTilFeilmeldingHvisIkkeAlleredeLagtTil(fortsattFeilListe, feilmelding);
                    }
                });
                return fortsattFeilListe;
            }

            function leggTilFeilmeldingHvisIkkeAlleredeLagtTil(liste, feilmelding) {
                if (!liste.contains(feilmelding)){
                    liste.push(feilmelding);
                }
            }

            // Henter feilmelding fra CMS
            function hentFeilmelding(feil, feilNokkel) {

                // Dersom feil er undefined brukes nokkel som key for feilmeldingen
                var feilmeldingNokkel = feilNokkel;
                if (feil) {
                    if(typeof feil.$errorMessages === 'object') {
                        feilmeldingNokkel = feil.$errorMessages[feilNokkel];
                    } else if(typeof feil.$errorMessages === 'string') {
                        feilmeldingNokkel = feil.$errorMessages;
                    }
                }

                var feilmelding = data.tekster[feilmeldingNokkel];
                var fantIkkeFeilmelding = "Fant ikke feilmelding med key " + feilmeldingNokkel;

                /*
                 * Dersom feilmeldingen ikke ble funnet, gi en standard tekst
                 * Skal ikke skje i produksjon, så mest for debugging
                 */
                if (feilmelding === undefined) {
                    return fantIkkeFeilmelding;
                }

                return feilmelding;
            };
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
