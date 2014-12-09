angular.module('nav.arbeidsforhold.grensearbeid.directive',[])
    .directive('grensearbeid', function() {
        return {
            templateUrl: '../js/common/arbeidsforhold/templates/grensearbeidTemplate.html',
            replace: true,
            scope: false
        };
    });