angular.module('nav.arbeidsforhold.reberegning.directive',[])
    .directive('reberegningDagpenger', function() {
        return {
            templateUrl: '../js/common/arbeidsforhold/templates/reberegningTemplate.html',
            replace: true,
            scope: false
        };
    });