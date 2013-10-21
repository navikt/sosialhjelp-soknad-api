angular.module('nav.sporsmalblokk',['nav.cmstekster'])
    .directive('sporsmalblokk', ['data', function(data) {
        return {
            restrict: "E",
            replace: true,
            scope: {
                nokkel: '@',
                form: '=',
                redigeringsModus: '=',
                nesteside: '@',
                validermetode: '&'
            },
            link: function(scope, element, attrs) {
                scope.data = {
                    soknadId: data.soknad.soknadId,
                    showErrorMessage: false
                }

                scope.validateForm = function () {
                    if (scope.validermetode) {
                        scope.validermetode({form: scope.form});
                    }
                    scope.data.showErrorMessage = scope.form.$invalid;
                    scope.redigeringsModus = scope.form.$invalid;

                }

                scope.gaTilRedigeringsmodus = function () {
                    scope.redigeringsModus = true;
                }

                scope.hvisIRedigeringsmodus = function () {
                    return scope.redigeringsModus;
                }

                scope.hvisIOppsummeringsmodus = function () {
                    return !scope.hvisIRedigeringsmodus();
                }

                scope.hvisIkkeFormValiderer = function () {
//                    console.log(scope.form.$error);
                    return scope.data.showErrorMessage;
                }
            },
            templateUrl: '../js/app/directives/sporsmalblokk/sporsmalblokkTemplate.html'
        }
    }]);
