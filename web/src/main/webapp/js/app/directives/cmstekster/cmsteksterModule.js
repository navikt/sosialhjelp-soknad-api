angular.module('nav.cmstekster',['app.services'])
    .directive('cmstekster', ['data', function(data) {
        return function ($scope, element, attrs) {
            var nokkel = attrs['cmstekster'];

            if (element.is('input')) {
                element.attr('value', data.tekster[nokkel]);
            } else {
                element.text(data.tekster[nokkel]);
            }
        };
    }]);
