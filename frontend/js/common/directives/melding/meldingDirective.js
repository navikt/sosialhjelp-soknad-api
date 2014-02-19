angular.module('nav.melding', [])
    .directive('melding', [function () {
        return {
            transclude: true,
            replace: true,
            templateUrl: '../js/common/directives/melding/meldingTemplate.html',
            link: function (scope, element) {
                var formLinje = element.closest('.form-linje');
                scope.skalVises = function() {
                    return formLinje.hasClass('feil') || formLinje.hasClass('feilstyling');
                };
            }
        };
    }]);