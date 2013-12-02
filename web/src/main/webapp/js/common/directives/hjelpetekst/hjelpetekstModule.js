angular.module('nav.hjelpetekst', ['nav.animation'])
    .directive('navHjelpetekstelement', [function () {
        return {
            replace: true,
            scope: {
                tittel: '=',
                tekst: '=',
                vishjelp: '='
            },
            templateUrl: '../js/common/directives/hjelpetekst/hjelpetekstTemplate.html',
            link: function(scope, element) {
                element.hide();

                scope.lukk = function() {
                    scope.vishjelp = false;
                    element.parent().find('.definerer-hjelpetekst.open, [data-definerer-hjelpetekst].open').removeClass('open');
                }
            }
        }
    }])
    .directive('definererHjelpetekst', ['cms', function (cms) {
        return {
            restrict: 'AC',
            scope: false,
            link: function(scope, element, attrs) {
                var tittelNokkel = attrs['nokkel'] + ".hjelpetekst.tittel";
                var tekstNokkel = attrs['nokkel'] + ".hjelpetekst.tekst";

                var tittel = cms.tekster[tittelNokkel];
                var tekst = cms.tekster[tekstNokkel];

                scope.visHjelpetekst = false;
                scope.tittel = tittel;
                scope.tekst = tekst;

                element.bind('click', function() {
                    if (element.hasClass('open')) {
                        scope.visHjelpetekst = false;
                        element.removeClass('open');
                    } else if (scope.visHjelpetekst) {
                        scope.tittel = tittel;
                        scope.tekst = tekst;
                        element.addClass('open');
                        element.siblings('.definerer-hjelpetekst.open').removeClass('open');
                    } else {
                        scope.tittel = tittel;
                        scope.tekst = tekst;
                        element.addClass('open');
                        scope.visHjelpetekst = true;
                    }
                    scope.$apply();
                });
            }
        }
    }]);