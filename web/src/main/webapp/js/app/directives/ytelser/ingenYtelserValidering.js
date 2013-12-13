angular.module('nav.ingenytelser', [])
    .directive('validerIngenYtelser', ['data', function (data) {
        return {
            require: ['^form'],
            link: function (scope, element, attrs, ctrl) {
                var eventString = 'RUN_VALIDATION' + ctrl[0].$name;
                scope.$on(eventString, function () {
                    fadeBakgrunnsfarge(element, element.find('.ingen-ytelse-melding'), 'feil', scope);
                })
                scope.$watch(function () {
                    console.log("ytelserXXX1");
                    return scope.ytelser.skalViseFeilmeldingForIngenYtelser;
                }, function () {
                    if (scope.ytelser.skalViseFeilmeldingForIngenYtelser) {
                        element.addClass('feil');
                    } else {
                        fadeBakgrunnsfarge(element, element.find('.ingen-ytelse-melding'), 'feil', scope);
                    }
                });
            }
        }
    }])