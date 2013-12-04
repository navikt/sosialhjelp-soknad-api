angular.module('nav.apneaccordion', [])

    .directive('apneAccordion', [function () {
        return {
            require: 'accordion',
            restrict: 'A',
            link: function (scope, element, attrs, accordion) {
                scope.$on("OPEN_TAB", function (e, ider) {
                    endreAccordionVisning(true, ider)
                });

                scope.$on("CLOSE_TAB", function (e, ider) {
                    endreAccordionVisning(false, ider)
                });

                function endreAccordionVisning(skalApne, ider) {
                    for (var j = 0; j < ider.length; j++) {
                        var scopeId = $('#' + ider[j]).scope().$id;
                        for (var i = 0; i < accordion.groups.length; i++) {
                            var accordionBolk = accordion.groups[i];
                            if (accordionBolk.$id == scopeId) {
                                accordionBolk.isOpen = skalApne;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }]);