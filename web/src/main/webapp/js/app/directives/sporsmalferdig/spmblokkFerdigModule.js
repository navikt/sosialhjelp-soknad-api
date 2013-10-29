angular.module('nav.sporsmalferdig', [])
    .directive('spmblokkferdig', ['$anchorScroll', '$location', '$timeout', function ($anchorScroll, $location, $timeout) {
        return {
            restrict: "E",
            replace: true,
            templateUrl: '../js/app/directives/sporsmalferdig/spmblokkFerdigTemplate.html',
            scope: {
                nokkel: '@',
                modus: '='
            },
            link: function (scope, element) {
                var tab = element.closest('.accordion-group');
                var nesteTab = tab.next();
                var forrigeTab = tab.prev();

                setOppLenke(forrigeTab, element, 'forrige');
                setOppLenke(nesteTab, element, 'neste');

                scope.hvisIRedigeringsmodus = function () {
                    return scope.modus;
                }

                scope.hvisIOppsummeringsmodus = function () {
                    return !scope.hvisIRedigeringsmodus();
                }

                scope.gaTilRedigeringsmodus = function () {
                    scope.modus = true;
                }

                scope.lukkOgGaaTilNeste = function () {
                    lukkTab(tab);
                    scope.gaaTilNeste();
                }

                scope.gaaTilNeste = function () {
                    gaaTilTab(nesteTab);
                }

                scope.gaaTilForrige = function () {
                    gaaTilTab(forrigeTab);
                }

                function gaaTilTab(nyTab) {
                    if (nyTab.length > 0) {
                        apneTab(nyTab);
                        $timeout(function () {
                            scrollToTab(nyTab);
                        }, 200);
                    }
                }

                function apneTab(apneTab) {
                    scope.$emit("OPEN_TAB", apneTab.attr('id'));
                }

                function lukkTab(lukkTab) {
                    scope.$emit("CLOSE_TAB", lukkTab.attr('id'));
                }

                function setOppLenke(gruppe, element, lenkeKlasse) {
                    if (gruppe.length == 0) {
                        element.find('.' + lenkeKlasse).addClass("ikke-aktiv");
                    }
                }
            }
        }
    }]);