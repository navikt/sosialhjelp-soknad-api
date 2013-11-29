angular.module('nav.hjelpetekst', [])
    .directive('navHjelpetekstelement', ['$timeout', function ($timeout) {
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
    .directive('definererHjelpetekst', ['data', function (data) {
        return {
            restrict: 'AC',
            scope: false,
            link: function(scope, element, attrs) {
                var tittelNokkel = attrs['nokkel'] + ".hjelpetekst.tittel";
                var tekstNokkel = attrs['nokkel'] + ".hjelpetekst.tekst";

                var tittel = data.tekster[tittelNokkel];
                var tekst = data.tekster[tekstNokkel];

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
    }])
    .animation('.animate', [function() {
        return {
            addClass: function(element, className, done) {
                if (className == 'ng-hide') {
                    element.slideUp();
                } else {
                    done();
                }
            },
            removeClass: function(element, className, done) {
                if (className == 'ng-hide') {
                    element.removeClass('ng-hide');
                    element.slideDown();
                } else {
                    done();
                }
            }
        }
    }]);