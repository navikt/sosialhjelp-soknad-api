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
                elm.bind("click", function () {

                    console.log(elm.closest('.spm-boks'))
                    settFokusTilNesteElement(elm);
                })
            }
        };
    }])
    .directive('fokusSlettmoduler', [function () {
        return {
            replace: true,

            link: function (scope, elm, attrs) {
            var id = attrs.fokusSlettmoduler;
                elm.bind("click", function () {
                    angular.element("#" + id + " .knapp-leggtil-liten").focus();
                })
            }
        };
    }]);
