angular.module('nav.hjelpetekst', [])
    .directive('navHjelpetekstelement', ['$timeout', function ($timeout) {
        return {
            replace: true,
            scope: {
                tittel: '=',
                tekst: '=',
                vishjelp: '='
            },
            templateUrl: '../js/app/directives/hjelpetekst/hjelpetekstTemplate.html',
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
                        console.log(1);
                        scope.visHjelpetekst = false;
                        element.removeClass('open');
                    } else if (scope.visHjelpetekst) {
                        console.log(2);
                        scope.tittel = tittel;
                        scope.tekst = tekst;
                        element.addClass('open');
                        element.siblings('.definerer-hjelpetekst.open').removeClass('open');
                    } else {
                        console.log(3);
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

//    .directive('navHjelpetekst', ['$http', '$templateCache', '$compile', function ($http, $templateCache, $compile) {
//        return {
//            require: '^form',
//            scope: {
//                tittel: '@',
//                tekst: '@'
//            },
//            templateUrl: '../js/app/directives/hjelpetekst/hjelpetekstTemplate.html',
//            link: function(scope, element, attrs, form) {
////                console.log($('[data-ng-form=' + form.$name + ']'));
//                angular.element($('[data-ng-form=' + form.$name + ']')).append(angular.element('<div>Hei</div>'));
//                var hjelpeElement;
//                var sporsmalbolk = element.closest('.skjemainnhold');
//
//                scope.visHjelpetekst = false;
//
//                element.bind('click', function() {
//
//                    scope.visHjelpetekst = !scope.visHjelpetekst;
//                    var spmbolkWidth = sporsmalbolk.outerWidth()
//                    var left = (sporsmalbolk.position().left + spmbolkWidth + 50) + "px";
//                    var top = (element.position().top - 40) + "px";
//
//                    hjelpeElement.css({top: top, left: left});
//                    scope.$apply();
//                });
//            }
//        }
//    }]);