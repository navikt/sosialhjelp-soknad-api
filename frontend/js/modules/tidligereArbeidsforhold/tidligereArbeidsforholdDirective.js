angular.module('nav.tidligerearbeidsforhold.directive', [])
    .directive('navTidligereArbeidsforhold', function () {
        return {
            replace: true,
            templateUrl: '../js/modules/tidligereArbeidsforhold/templates/tidligereArbeidsforholdTemplate.html'
        };
    })
    .directive('permittertSporsmal', function (data) {
        return {
            replace: true,
            scope: true,
            templateUrl: '../js/modules/tidligereArbeidsforhold/templates/varpermittertsist.html',
            link: function(scope) {
                scope.varPermittert = data.finnFaktum('tidligerearbeidsforhold.permittert');
                scope.erPermittert = function() {
                    return scope.varPermittert.value == "permittert";
                }
            }
        };
    })
    .directive('tidligereArbeidsgiver', function(data) {
        return {
            replace: true,
            templateUrl: '../js/modules/tidligereArbeidsforhold/templates/tidligereArbeidsgiver.html',
            link: function(scope) {
                scope.cmsArgumenter = {
                    modelFaktum: data.finnFaktum('tidligerearbeidsforhold.tidligerearbeidsgiver'),
                    default: "[arbeidsgiver]"
                };
            }
        };
    });