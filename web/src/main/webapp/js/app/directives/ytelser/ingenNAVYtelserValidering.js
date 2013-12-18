angular.module('nav.ingennavytelser', [])
    .directive('validerNavYtelser', ['data', function (data) {
        return {
            require: ['^form'],
            link: function (scope, element, attrs, ctrl) {
                var eventString = 'RUN_VALIDATION' + ctrl[0].$name;
                scope.$on(eventString, function () {
                    fadeBakgrunnsfarge(element, element.find('.ingen-navytelse-melding'), 'feil', scope);
                })
                scope.$watch(function () {
                    return scope.ytelser.skalViseFeilmeldingForIngenNavYtelser;
                }, function () {
                    if (scope.ytelser.skalViseFeilmeldingForIngenNavYtelser) {
                        element.addClass('feil');
                    } else {
                        fadeBakgrunnsfarge(element, element.find('.ingen-navytelse-melding'), 'feil', scope);
                    }
                });
            }
        }
    }])