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
                console.log("Utdanning2x" + scope.utdanning.skalViseFeilmeldingForUtdanningAnnet);
                return scope.utdanning.skalViseFeilmeldingForUtdanningAnnet;
            }, function () {
                if (scope.utdanning.skalViseFeilmeldingForUtdanningAnnet) {
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