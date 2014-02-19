angular.module('nav.markup.modalside', [])
    .directive('modalside', [function () {
        return {
            restrict   : 'A',
            replace    : true,
            transclude : true,
            templateUrl: '../js/app/directives/markup/modalsideTemplate.html'
        };
    }]);