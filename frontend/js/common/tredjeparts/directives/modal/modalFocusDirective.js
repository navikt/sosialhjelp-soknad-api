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

            $modalStack.getTop().key.result
                .finally(function() {
                    $document.unbind('touchmove');
                    $('.modal').unbind('touchstart');
                    $('.modal').unbind('touchmove');
                });

            $document.bind('touchmove', function (evt) {
                evt.preventDefault();
            });

            $('.modal').bind('touchstart', function(evt) {
                if (evt.currentTarget.scrollTop === 0) {
                    evt.currentTarget.scrollTop = 1;
                } else if (evt.currentTarget.scrollHeigth === evt.currentTarget.scrollTop + evt.currentTarget.offsetHeigth) {
                    evt.currentTarget.scrollTop = -1;
                }
            });

            $('.modal').bind('touchmove', function(evt) {
                evt.stopPropagation();
            });
        };
    }]);