angular.module('nav.tidligerearbeidsforhold.directive', [])
    .directive('navTidligereArbeidsforhold', function () {
        return {
            replace: true,
            templateUrl: '../js/modules/tidligereArbeidsforhold/templates/tidligereArbeidsforholdTemplate.html'
        };
    })
    .directive('permittertSporsmal', function () {
        return {
            replace: true,
            scope: true,
            templateUrl: '../js/modules/tidligereArbeidsforhold/templates/varpermittertsist.html'
        };
    });