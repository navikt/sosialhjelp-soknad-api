angular.module('FormErrors', ['app.services'])

// just put <form-errors><form-errors> wherever you want form errors to be displayed!
.directive('formErrors', ['data', function (data) {
    return {
        // only works if embedded in a form or an ngForm (that's in a form). 
        // It does use its closest parent that is a form OR ngForm
        require: '^form',
        template:
            '<ul class="form-errors" data-ng-show="skalViseFeilmeldinger()">' +
                '<li class="form-error" ng-repeat="error in errors track by $index">' +
                    '{{error}}' +
                '</li>' +
            '</ul>',
        replace: true,
        transclude: true,
        restrict: 'AE',
        link: function postLink(scope, elem, attrs, ctrl) {
            // list of some default error reasons
            var defaultErrorReasons = {
                    required: 'er påkrevd.',
                    minlength: 'er for kort.',
                    maxlength: 'er for langt.',
                    email: 'er ikke en gyldig e-postadresse.',
                    pattern: 'inneholder ugyldige tegn.',
                    number: 'er ikke et tall.',

                    fallback: 'er ikke gyldig.'
                },
                // this is where we form our message
                errorMessage = function (feilmeldingNokkel, nokkel) {
                    // get the nice name if they used the niceName 
                    // directive or humanize the name and call it good
                    var defaultReason = defaultErrorReasons[nokkel] || defaultErrorReasons.fallback;

                    var feilmelding = data.tekster[feilmeldingNokkel]; // Henter fra cmstekster

                    if(feilmelding === undefined) {
                        return defaultReason;
                    }


                    return feilmelding;
                };

            scope.errors = [];
            scope.runValidation = function() {
                scope.errors = [];
                console.log(ctrl);

                var loopErrors = true;
                angular.forEach(ctrl.$error, function(verdi, nokkel) {

                    if (loopErrors) {
                        angular.forEach(verdi, function(error) {
                            var feilmeldingNokkel = nokkel;

                            if (error) {
                                feilmeldingNokkel = error.$errorMessages;
                            } else {
                                scope.errors = [errorMessage(feilmeldingNokkel, nokkel)];
                                loopErrors = false;
                            }

                            var feilmelding = errorMessage(feilmeldingNokkel, nokkel);

                            if ($.inArray(feilmelding, scope.errors) == -1) {
                                scope.errors.push(feilmelding);
                            }
                        });
                    }
                });
            }

            // Watcher for å kunne fjerne feilmeldinger når de er fikset :)
            scope.$watch(function() { return ctrl.$error; }, function() {
                var errors = [];
                angular.forEach(ctrl.$error, function(verdi, nokkel) {
                    angular.forEach(verdi, function(error) {
                        var feilmeldingNokkel = nokkel;
                        if (error) {
                            feilmeldingNokkel = error.$errorMessages;
                        }

                        try{
                            var feilmelding = errorMessage(feilmeldingNokkel, nokkel);

                            if ($.inArray(feilmelding, scope.errors) > -1 && $.inArray(feilmelding, errors) == -1) {
                                errors.push(feilmelding);
                            }
                        } catch (e) {} //duplicate key...
                    });
                });
                scope.errors = errors;
            }, true);

            scope.skalViseFeilmeldinger = function() {
                var harListeElementer = elem.children().length;
                return harListeElementer;
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
