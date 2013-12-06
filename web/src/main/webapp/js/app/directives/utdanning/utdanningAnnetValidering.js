angular.module('nav.utdanningAnnet', [])
    .directive('validerUtdanningANnet', ['data', function (data) {
    return {
        require: ['^form'],
        link: function (scope, element, attrs, ctrl) {
            var eventString = 'RUN_VALIDATION' + ctrl[0].$name;

            scope.$on(eventString, function () {
                fadeBakgrunnsfarge(element, element.find('.utdanning-annet-melding'), 'feil');
            })

            scope.$watch(function () {
                return scope.ytelser.skalViseFeilmeldingForUtdanningAnnet;
            }, function () {
                if (scope.ytelser.skalViseFeilmeldingForUtdanningAnnet) {
                    element.addClass('feil');
                } else {
                    fadeBakgrunnsfarge(element, element.find('.utdanning-annet-melding'), 'feil');
                }
            });
        }
    }
}])