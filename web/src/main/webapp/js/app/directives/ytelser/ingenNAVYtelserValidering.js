angular.module('nav.ingennavytelser', [])
    .directive('validerNavYtelser', ['data', function (data) {
        return {
            require: ['^form'],
            link: function (scope, element, attrs, ctrl) {
                var eventString = 'RUN_VALIDATION' + ctrl[0].$name;
                scope.$on(eventString, function () {
                    fadeFeilmelding(element, element.find('.ingen-navytelse-melding'), 'feil', scope);
                })
                scope.$watch(function () {
                    return scope.ytelser.skalViseFeilmeldingForIngenNavYtelser;
                }, function () {
                    if (scope.ytelser.skalViseFeilmeldingForIngenNavYtelser) {
                        console.log("Legger til feil");
                        element.addClass('feil');
                    } else {
                        fadeFeilmelding(element, element.find('.ingen-navytelse-melding'), 'feil', scope);
                    }
                });
            }
        }
    }])