angular.module('nav.fokus', [])
    .directive('fokus', [function () {
        return {
            replace: true,

            /*
            Brukes for å sette fokus til neste element da spesielt mtp elementer
            som blir fjernet fra DOMen og bruker tabber seg videre. Hvis ikke denne brukes
            vil bruker havne på toppen og må tabbe seg tilbake
             */
            link: function (scope, elm) {
                elm.bind("click", function() {
                    settFokusTilNesteElement(elm);
                })
            }
        };
    }]);
