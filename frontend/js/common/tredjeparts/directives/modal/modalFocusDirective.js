angular.module('nav.modal.focus', [])
    .directive('modalFocus', ['$modalStack', function($modalStack) {
        return function(scope, element) {
            var fokusElement;

            var watchListener = scope.$watch(
                function() {
                    return $modalStack.getTop().value.modalDomEl.hasClass('in');
                },
                function(val) {
                    if (val) {
                        var fokuserbareElementer = element.find('input, a, select, button, textarea').filter(function () {
                            return $(this).is(':visible');
                        });

                        fokusElement = fokuserbareElementer.eq(1);
                        fokusElement.focus();
                        watchListener();
                    }
                }
            );

            element.bind('keydown', function (evt) {
                if (evt.which !== 9) {
                    return;
                }

                evt.preventDefault();
                var fokuserbareElementer = element.find('input, a, select, button, textarea').filter(function () {
                    return $(this).is(':visible');
                });

                var focusIdx = fokuserbareElementer.index(fokusElement);

                if (evt.shiftKey) {
                    focusIdx--;
                } else {
                    focusIdx++;
                }

                focusIdx = focusIdx % fokuserbareElementer.length;

                fokusElement = fokuserbareElementer.eq(focusIdx);
                fokusElement.focus();
            });
        };
    }]);