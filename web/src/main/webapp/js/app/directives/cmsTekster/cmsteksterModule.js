angular.module('nav.cmstekster',['app.services'])
    .directive('cmstekster', ['data', function(data) {
        return function ($scope, element, attrs) {
            var nokkel = attrs['cmstekster'];
            element.text(data.tekster[nokkel]);
        };
    }]);
