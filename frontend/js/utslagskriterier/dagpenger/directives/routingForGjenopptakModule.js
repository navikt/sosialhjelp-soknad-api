angular.module('nav.routingForGjenopptakModule', ['nav.cmstekster'])
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
                        setCookie('routingGjenopptak', {
                            arbeidSiste52Uker: scope.gjenopptak.harMotattDagpenger,
                            harArbeidet: scope.gjenopptak.harArbeidet
                        });

                        if (scope.gjenopptak.harMotattDagpenger === "ja") {
                            redirectTilUrl(currentUrl.substring(0, currentUrl.indexOf('start/')) + 'start/gjenopptak');
                        } else {
                            redirectTilUrl(currentUrl.substring(0, currentUrl.indexOf('start/')) + 'start/NAV%2004-01.03#/informasjonsside');
                        }
                    } else {
                        scope.$broadcast(eventString);
                    }
                };
            }
        };
    });
