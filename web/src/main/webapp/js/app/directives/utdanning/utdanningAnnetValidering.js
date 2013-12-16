angular.module('nav.utdanningannet', [])
    .directive('validerUtdanningAnnet', ['data', function (data) {
    return {
        require: ['^form'],
        link: function (scope, element, attrs, ctrl) {
            var eventString = 'RUN_VALIDATION' + ctrl[0].$name;
            scope.$watch(function () {
                return scope.utdanning.skalViseFeilmeldingForUtdanningAnnet;
            }, function () {
                if (scope.utdanning.skalViseFeilmeldingForUtdanningAnnet) {
                    element.addClass('feil');
                }
            });
        }
    }
}])