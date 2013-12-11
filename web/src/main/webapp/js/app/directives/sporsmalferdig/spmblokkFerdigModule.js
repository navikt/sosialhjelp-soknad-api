angular.module('nav.sporsmalferdig', [])
    .directive('spmblokkferdig', ['$timeout', function ($timeout) {
        return {
            require: '^form',
            replace: true,
            templateUrl: '../js/app/directives/sporsmalferdig/spmblokkFerdigTemplate.html',
            scope: {
                nokkel: '@',
                modus: '=',
                submitMethod: '&'
            },
            link: function (scope, element, attrs, form) {
                var tab = element.closest('.accordion-group');
                var nesteTab = tab.next();

                scope.hvisIRedigeringsmodus = function () {
                    return scope.modus;
                }

                scope.hvisIOppsummeringsmodus = function () {
                    return !scope.hvisIRedigeringsmodus();
                }

                scope.gaTilRedigeringsmodus = function () {
                    scope.modus = true;
                    scope.$emit("ENDRET_TIL_REDIGERINGS_MODUS", {key: 'redigeringsmodus', value: true});
                }

                scope.validerOgGaaTilNeste = function () {
                    scope.submitMethod();

                    if (form.$valid) {
                        lukkTab(tab);
                        gaaTilTab(nesteTab);
                    }
                }

                function gaaTilTab(nyTab) {
                    if (nyTab.length > 0) {
                        apneTab(nyTab);
                        $timeout(function () {
                            scrollToElement(nyTab, 100);
                        }, 200);
                    }
                }

                function apneTab(apneTab) {
                    scope.$emit("OPEN_TAB", [apneTab.attr('id')]);
                }

                function lukkTab(lukkTab) {
                    scope.$emit("CLOSE_TAB", [lukkTab.attr('id')]);
                }
            }
        }
    }]);