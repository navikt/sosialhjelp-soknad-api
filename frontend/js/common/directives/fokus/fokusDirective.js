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
                });
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
                    if(id) {
                        angular.element("#" + id + " .knapp-leggtil-liten").focus();
                    } else {
                        angular.element(".knapp-leggtil-liten").focus();
                    }
                });
            }
        };
    }])
    .directive('fokusSlettAnnet', [function () {
        return {
            link: function (scope, elm) {
                elm.bind("click", function () {
                    angular.element(".knapp-stor").focus();
                });
            }
        };
    }])
    .directive('leggtilOrgnr', [function () {
        return {
            link: function (scope, elm) {
                elm.bind("click", function () {
                    elm.prev().find('.orgnummer-repeat input').focus();
                });
            }
        };
    }])
    /*
     Fikser autoscrolling ved tabbing for elementer som skjules bak sticky-lenken
     */
    .directive('tabAutoscroll', [function () {
        return {
            link: function (scope, elm) {
                var stickyElementBunn;
                var stickyPosisjonTopp;
                var elementMedFokus;
                var posisjon = "";
                var TAB_BUTTON = 9;

                elm.bind("keyup keypress", function (event) {
                    sjekkOmElementHarKlasseDagpenger();
                    sjekkOmTab();
                    sjekkOmTabOgShift();

                    function sjekkOmElementHarKlasseDagpenger() {
                        if (elm.hasClass('dagpenger')) {
                            stickyElementBunn = elm.next().find('.sticky-bunn');
                            var stickyElementTopp = elm.find('.sticky-feilmelding');
                            stickyPosisjonTopp = stickyElementTopp[0].getBoundingClientRect();

                        } else {
                            stickyElementBunn = elm.find('.sticky-bunn');
                            stickyPosisjonTopp = {top: 0, bottom: 60};
                        }
                    }

                    function sjekkOmTab() {
                        if (event.which === TAB_BUTTON) {
                            var stickyPosisjonBunn = stickyElementBunn[0].getBoundingClientRect();
                            elementMedFokus = document.activeElement;
                            posisjon = "";

                            if ($(elementMedFokus).is("[type=radio]") || $(elementMedFokus).is("[type=checkbox]")) {
                                posisjon = $(elementMedFokus).closest('div')[0].getBoundingClientRect();
                            } else {
                                posisjon = elementMedFokus.getBoundingClientRect();
                            }

                            if (posisjon.top + 10 >= stickyPosisjonBunn.top) {
                                scrollToElement($(elementMedFokus), $(window).height() / 2);
                            }
                        }
                    }

                    function sjekkOmTabOgShift() {
                        if (event.which === TAB_BUTTON && event.shiftKey) {
                            elementMedFokus = document.activeElement;
                            posisjon = "";

                            if ($(elementMedFokus).is("[type=radio]") || $(elementMedFokus).is("[type=checkbox]")) {
                                posisjon = $(elementMedFokus).closest('div')[0].getBoundingClientRect();
                            } else {
                                posisjon = elementMedFokus.getBoundingClientRect();
                            }

                            if (posisjon.bottom - 20 <= stickyPosisjonTopp.bottom) {
                                scrollToElement($(elementMedFokus), $(window).height() / 2);
                            }
                        }
                    }
                });
            }
        };

    }])
    .directive('scrollingTittel', ['$timeout', function ($timeout) {
        return {
            link: function (scope, elm) {
                $timeout(function() {
                    scrollToElement(elm, 250);
                }, 50);
            }
        };
    }]);