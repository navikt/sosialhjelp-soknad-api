angular.module('nav.avbryt.soknadSlettetDirective', [])
    .directive('soknadSlettet', function () {
        return {
            scope: false,
            templateUrl: '../js/modules/avbryt/templates/soknadSlettetTemplate.html'
        };
    });