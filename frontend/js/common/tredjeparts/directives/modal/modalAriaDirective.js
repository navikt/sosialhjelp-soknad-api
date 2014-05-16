angular.module('nav.modal.aria', [])
    .directive('modalAria', [function() {
        return function(scope, element, attrs) {
            var defaultLabelledby = attrs.ariaLabelledby;
            var defaultDescribedby = attrs.ariaDescribedby;
            var header = element.find('h1');
            var descriptionElement = header.next();

            if (header.attr('id')) {
                element.attr('data-aria-labelledby', header.attr('id'));
            } else {
                header.attr('id', defaultLabelledby);
            }

            if (descriptionElement.attr('id')) {
                element.attr('data-aria-describedby', descriptionElement.attr('id'));
            } else {
                descriptionElement.attr('id', defaultDescribedby);
            }
        };
    }]);