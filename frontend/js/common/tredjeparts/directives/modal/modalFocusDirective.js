angular.module('nav.modal.focus', [])
    .directive('modalFocus', ['$modalStack', '$document', function($modalStack, $document) {
        return function(scope, element) {
            var fokusElement;

            console.log($modalStack.getTop());

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

//            $modalStack.getTop().key.result
//                .finally(function() {
//                    console.log("Hei og hå");
//                    $document.unbind('touchmove');
//                })
//
//            $document.bind('touchmove', function (evt) {
//                evt.preventDefault();
//            });
        };
    }]);