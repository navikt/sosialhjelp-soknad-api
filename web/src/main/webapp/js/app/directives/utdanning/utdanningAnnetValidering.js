angular.module('nav.utdanningannet', [])
    .directive('validerUtdanningAnnet', ['data', function (data) {
    return {
        require: ['^form'],
        link: function (scope, element, attrs, ctrl) {
            var eventString = 'RUN_VALIDATION' + ctrl[0].$name;
            scope.$on(eventString, function () {
                fadeFeilmelding(element, element.find('.utdanning-annet-melding'), 'feil', scope);
            })
            scope.$watch(function () {
                return scope.utdanning.skalViseFeilmeldingForUtdanningAnnet;
            }, function () {
                if (scope.utdanning.skalViseFeilmeldingForUtdanningAnnet) {
                    console.log("skalvisefeil");
                    element.addClass('feil');
                }else {
                    fadeFeilmelding(element, element.find('.utdanning-annet-melding'), 'feil', scope);
                }
            });
        }
    }
}])