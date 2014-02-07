angular.module('nav.sporsmalferdig', [])
    .directive('spmblokkferdig', ['$timeout', function ($timeout) {
        return {
            require: '^form',
            replace: true,
            templateUrl: '../js/app/directives/sporsmalferdig/spmblokkFerdigTemplate.html',
            scope: {
                nokkel: '@',
                submitMethod: '&'
            },
            link: function (scope, element, attrs, form) {
                var tab = element.closest('.accordion-group');

                scope.validerOgGaaTilNeste = function () {
                    scope.submitMethod();

                    if (form.$valid) {
                        gaaTilTab(tab);
                        lukkTab(tab);
                        apneTab(tab.next());

                    }
                };

                function gaaTilTab(nyTab) {
                    if (nyTab.length > 0) {
                        $timeout(function () {
                            scrollToElement(nyTab, 0);
                        }, 0);
                    }
                }

                function apneTab(apneTab) {
                    scope.$emit('OPEN_TAB', apneTab.attr('id'));
                }

                function lukkTab(lukkTab) {
                    scope.$emit('CLOSE_TAB', lukkTab.attr('id'));
                }
            }
        }
    }]);
