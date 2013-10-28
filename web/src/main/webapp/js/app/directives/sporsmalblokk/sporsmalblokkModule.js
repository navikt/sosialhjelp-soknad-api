angular.module('nav.sporsmalblokk',['nav.cmstekster'])
    .directive('sporsmalblokk', ['data', function(data) {
        return {
            restrict: "E",
            replace: true,
            templateUrl: '../js/app/directives/sporsmalblokk/sporsmalblokkTemplate.html',
            link: function(scope, element) {
                var group = element.closest('.accordion-group');
                var forrigeGroup = group.prev();
                var nesteGroup = group.next();
                var forrige = element.find('.forrige');
                var neste = element.find('.neste');

                setOppLenke(forrigeGroup, forrige);
                setOppLenke(nesteGroup, neste);


                function setOppLenke(gruppe, lenke) {
                    if (gruppe.length > 0) {
                        lenke.click(function() {
                            scrollAndOpen(gruppe);
                        });
                    } else {
                        lenke.changeElementType("span");
                    }
                }

                function scrollAndOpen(element) {
                    scrollToElement(element);
                    var groupTab = element.find('.accordion-toggle');
                    var groupBody = element.find('.accordion-body');
                    setTimeout(function() {
                        if (groupBody.height() == 0) {
                            groupTab.click();
                        }
                    },1);
                }
            }
        }
    }]);
