angular.module('nav.fokus', [])
    .directive('fokus', [function () {
        return {
            /*
             Brukes for å sette fokus til neste element da spesielt mtp elementer
             som blir fjernet fra DOMen og bruker tabber seg videre. Hvis ikke denne brukes
             vil bruker havne på toppen og må tabbe seg tilbake
             */
            link: function (scope, elm) {
                elm.bind("click", function () {
                    settFokusTilNesteElement(elm);
                })
            }
        };
    }])
    /*
    setter fokus på legg-tilknappen hvis et element blir slettet mest mtp tabbing
     */
    .directive('fokusSlettmoduler', [function () {
        return {
            link: function (scope, elm, attrs) {
            var id = attrs.fokusSlettmoduler;
                elm.bind("click", function () {
                    angular.element("#" + id + " .knapp-leggtil-liten").focus();
                })
            }
        };
    }])
    /*
    Fikser autoscrolling ved tabbing for elementer med zero-area (height, width osv = 0)
     */
    .directive('tabAutoscroll', [function () {
        return {
            link: function (scope, elm) {
                elm.bind("keydown keypress", function (event) {
                    if(event.which === 9) {
                        console.log("hei");
                    }
                })
            }
        };
    }]);
