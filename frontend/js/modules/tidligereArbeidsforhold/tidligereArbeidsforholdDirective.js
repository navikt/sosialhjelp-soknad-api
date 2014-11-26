angular.module('nav.tidligerearbeidsforhold.directive', [])
    .directive('navTidligereArbeidsforhold', function () {
        return {
            replace: true,
            templateUrl: '../js/modules/tidligereArbeidsforhold/templates/tidligereArbeidsforholdTemplate.html'
        };
    });