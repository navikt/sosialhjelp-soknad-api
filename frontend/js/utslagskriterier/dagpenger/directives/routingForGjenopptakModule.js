angular.module('nav.routingForGjenopptakModule', [])
    .directive('routingForGjenopptakButton', function () {
        return {
            replace: true,
            require: '^form',
            templateUrl: '../js/utslagskriterier/dagpenger/directives/startsoknadButtonTemplate.html',
            link: function (scope, el, attr, ctrl) {
                scope.form = ctrl;
                var eventString = 'RUN_VALIDATION' + ctrl.$name;
                scope.validerOgStartSoknad = function () {
                    if (scope.form.$valid) {
                        var currentUrl = location.href;
                        if (scope.gjenopptak.harMotattDagpenger === "ja") {
                            redirectTilUrl(currentUrl.substring(0, currentUrl.indexOf('utslagskriterier/')) + 'NAV04-16.03/start#/informasjonsside');
                        } else {
                            redirectTilUrl(currentUrl.substring(0, currentUrl.indexOf('utslagskriterier/')) + 'NAV04-01.03/start#/informasjonsside');
                        }
                    } else {
                        scope.$broadcast(eventString);
                    }
                };
            }
        };
    });
