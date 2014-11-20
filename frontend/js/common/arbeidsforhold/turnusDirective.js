angular.module('nav.arbeidsforhold.turnus.directive',[])
    .directive('turnusblokk', function() {
        return {
            templateUrl: '../js/common/arbeidsforhold/templates/undersporsmaal/turnusblokkTemplate.html',
            replace: true,
            scope: {
                arbeidsforhold: '='
            },
            link: function (scope) {
                scope.parentFaktum = scope.arbeidsforhold;
            }
        };
    })
    .directive('turnusblokkOppsummering', function() {
        return {
            templateUrl: '../js/common/arbeidsforhold/templates/oppsummeringer/turnusblokkOppsummeringTemplate.html',
            replace: true,
            scope: {
                arbeidsforhold: '='
            },
            link: function (scope) {
                scope.parentFaktum = scope.arbeidsforhold;
            }
        };
    });