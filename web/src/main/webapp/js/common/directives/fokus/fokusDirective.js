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
     Fikser autoscrolling ved tabbing for elementer som skjules bak sticky-lenken
     */
    .directive('tabAutoscroll', [function () {
        return {
            link: function (scope, elm) {
                elm.bind("keyup keypress", function (event) {
                    if (event.which === 9) {
                        var stickyElement = elm.next().find('.sticky-bunn');
                        var stickyPosisjon = stickyElement[0].getBoundingClientRect();

                        var elementMedFokus = document.activeElement;
                        var posisjon = "";

                        if ($(elementMedFokus).is("[type=radio]") || $(elementMedFokus).is("[type=checkbox]")) {
                            posisjon = $(elementMedFokus).closest('div')[0].getBoundingClientRect();
                        } else {
                            posisjon = elementMedFokus.getBoundingClientRect();
                        }

                        if (posisjon.top + 10 >= stickyPosisjon.top) {
                            scrollToElement(stickyElement, stickyPosisjon.top / 2);
                        }
                    }
                })
            }
        };
    }]);
