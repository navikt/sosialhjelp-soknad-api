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
            }
        }
    }])
    .directive('definererHjelpetekst', [function () {
        return {
            restrict: 'AC',
            scope: false,
            link: function(scope, element, attrs) {
                scope.visHjelpetekst = false;

                scope.tittel = attrs['tittel'];
                scope.tekst = attrs['tekst'];

                if (scope.tittel === undefined) {
                    scope.tittel = "Hjælp";
                }

                if (scope.tekst === undefined) {
                    scope.tekst = "Hjelp finner du hos nav.no";
                }

                element.bind('click', function() {
                    if (element.hasClass('open')) {
                        scope.visHjelpetekst = false;
                        element.removeClass('open');
                    } else if (scope.visHjelpetekst) {
                        element.addClass('open');
                        element.siblings('.definerer-hjelpetekst.open').removeClass('open');
                    } else {
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