angular.module('nav.ingenytelser', [])
    .directive('validerIngenYtelser', ['data', function (data) {
        return {
            require: ['^form'],
            link: function (scope, element, attrs, ctrl) {
                var eventString = 'RUN_VALIDATION' + ctrl[0].$name;
                scope.$on(eventString, function () {
                    fadeFeilmelding(element, element.find('.ingen-ytelse-melding'), 'feil', scope);
                })
                scope.$watch(function () {
                    return scope.ytelser.skalViseFeilmeldingForIngenYtelser;
                }, function () {
                    if (scope.ytelser.skalViseFeilmeldingForIngenYtelser) {
                        element.addClass('feil');
                    } else {
                        fadeFeilmelding(element, element.find('.ingen-ytelse-melding'), 'feil', scope);
                    }
                });
            }
        }
    }])