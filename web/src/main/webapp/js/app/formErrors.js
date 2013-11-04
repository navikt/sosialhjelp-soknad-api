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

            // Henter feilmelding fra CMS
            var hentFeilmelding = function (feil, nokkel) {

                // Dersom feil er undefined brukes nokkel som key for feilmeldingen
                var feilmeldingNokkel = nokkel;
                if (feil) {
                    feilmeldingNokkel = feil.$errorMessages;
                }

                var feilmelding = data.tekster[feilmeldingNokkel];
                var fantIkkeFeilmelding = "Fant ikke feilmelding";

                /*
                 * Dersom feilmeldingen ikke ble funnet, gi en standard tekst
                 * Skal ikke skje i produksjon, så mest for debugging
                 */
                if (feilmelding === undefined) {
                    return fantIkkeFeilmelding;
                }

                return feilmelding;
            };

            scope.runValidation = function () {
                scope.feilmeldinger = [];
                var fortsettLoop = true;

                angular.forEach(ctrl.$error, function (verdi, nokkel) {
                    if (fortsettLoop) {
                        leggerTilFeilmeldinger(verdi, nokkel);
                    }
                });
            }

            // Watcher for å kunne fjerne feilmeldinger når de er fikset :)
            scope.$watch(function() { return ctrl.$error; }, function() {
                var fortsattFeilListe = [];
                angular.forEach(ctrl.$error, function(verdi, nokkel) {
                    angular.forEach(verdi, function(feil) {
                        var feilmelding = hentFeilmelding(feil, nokkel);

                        if (scope.feilmeldinger.contains(feilmelding) && !fortsattFeilListe.contains(feilmelding)) {
                            fortsattFeilListe.push(feilmelding);
                        }
                    });
                });
                scope.feilmeldinger = fortsattFeilListe;
            }, true);

            scope.skalViseFeilmeldinger = function() {
                return elem.children().length;
            }

            /*
             * Dersom vi har en egendefinert feil skal vi bare vise denne. I det tilfellet fjernes alle andre feilmeldinger
             * og vi skal ikke loope mer. Kan ikke breake en angular.forEach...
             */
            function leggerTilFeilmeldinger(verdi, nokkel) {
                angular.forEach(verdi, function (feil) {
                    var feilmelding = hentFeilmelding(feil, nokkel);

                    if (feil === undefined) {
                        // Egendefinert feilmelding
                        scope.feilmeldinger = [feilmelding];
                        fortsettLoop = false;
                    }

                    if (!scope.feilmeldinger.contains(feilmelding)){
                        scope.feilmeldinger.push(feilmelding);
                    }
                });
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
