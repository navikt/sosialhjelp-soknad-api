angular.module('nav.hjelpetekst', ['nav.animation'])
    .directive('navHjelpetekstelement', [function () {
        return {
            replace: true,
            scope: {
                tittel: '=',
                tekst: '='
            },
            templateUrl: '../js/common/directives/hjelpetekst/hjelpetekstTemplate.html',
            link: function (scope) {
                scope.visHjelp = false;
                scope.toggleHjelpetekst = function() {
                    scope.visHjelp = !scope.visHjelp;
                }

                scope.lukk = function () {
                    scope.visHjelp = false;
                }
            }
        }
    }])
    .directive('navHjelpetekstTooltip', ['$timeout', function ($timeout) {
        return function (scope, element) {
            $timeout(function() {
                var posisjon = element.prev().position();

                var topp = posisjon.top - element.outerHeight() - 15;
                var venstre = posisjon.left - 20;
                element.css({top: topp + "px", left: venstre + "px"});
            });
        }
    }])