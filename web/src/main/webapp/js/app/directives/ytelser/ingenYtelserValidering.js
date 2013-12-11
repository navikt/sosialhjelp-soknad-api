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
                   // console.log("Ytelser2");
                    return scope.ytelser.skalViseFeilmeldingForIngenYtelser;
                }, function () {
                    if (scope.ytelser.skalViseFeilmeldingForIngenYtelser) {
                     //   console.log("Ytelser3");
                        element.addClass('feil');
                    } else {
                       // console.log("Ytelser4");
                        fadeBakgrunnsfarge(element, element.find('.ingen-ytelse-melding'), 'feil', scope);
                    }
                });
            }
        }
    }])