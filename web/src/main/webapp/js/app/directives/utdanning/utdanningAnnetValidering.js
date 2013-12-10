angular.module('nav.utdanningannet', [])
    .directive('validerUtdanningAnnet', ['data', function (data) {
    return {

        require: ['^form'],
        link: function (scope, element, attrs, ctrl) {
            var eventString = 'RUN_VALIDATION' + ctrl[0].$name;

            scope.$on(eventString, function () {
                console.log("Utdanning1");
                fadeBakgrunnsfarge(element, element.find('.utdanning-annet-melding'), 'feil');
            })

            scope.$watch(function () {
                console.log("Utdanning2");
                return scope.ytelser.skalViseFeilmeldingForUtdanningAnnet;
            }, function () {
                if (scope.ytelser.skalViseFeilmeldingForUtdanningAnnet) {
                    console.log("Utdanning3");
                    element.addClass('feil');
                } else {
                    console.log("Utdanning4");
                    fadeBakgrunnsfarge(element, element.find('.utdanning-annet-melding'), 'feil');
                }
            });
        }
    }
}])