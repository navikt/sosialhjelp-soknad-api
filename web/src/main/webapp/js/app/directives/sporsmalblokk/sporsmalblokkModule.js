angular.module('nav.sporsmalblokk',['nav.cmstekster'])
    .directive('sporsmalblokk', ['data', function(data) {
        return {
            restrict: "E",
            replace: true,
            templateUrl: '../js/app/directives/sporsmalblokk/sporsmalblokkTemplate.html'
        }
    }]);
