angular.module('nav.startsoknad', ['nav.cmstekster'])
    .directive('startsoknadButton', function () {
        return {
            replace: true,
            require: '^form',
            templateUrl: '../js/utslagskriterier/dagpenger/directives/startsoknadButtonTemplate.html',
            link: function (scope, el, attr, ctrl) {
                scope.form = ctrl;

                scope.validerOgStartSoknad = function () {
                    if(scope.form.$valid) {
                        if(scope.gjenopptak.harMotattDagpenger === "ja") {
                            console.log("Starter gjennoptak");
                        } else {
                            console.log("starter dagpenger");
                            //soknad/a6ed47e9-528a-4ada-8670-d001d528cc11#/soknad
                        }
                    } else {
                        scope.valider(true);
                    }
                };
            }
        };
    });
