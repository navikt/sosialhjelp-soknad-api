angular.module('nav.sporsmalferdig', [])
    .directive('spmblokkferdig', ['$timeout', 'data', function ($timeout, data) {
        return {
            require: '^form',
            replace: true,
            templateUrl: '../js/app/directives/sporsmalferdig/spmblokkFerdigTemplate.html',
            scope: {
                nokkel: '@',
                modus: '=',
                submitMethod: '&'
            },
            link: function (scope, element) {
                var tab = element.closest('.accordion-group');
                var nesteTab = tab.next();

                scope.soknadId = data.soknad.soknadId;

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

                scope.lukkOgGaaTilNeste = function () {
                    lukkTab(tab);
                    gaaTilTab(nesteTab);
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