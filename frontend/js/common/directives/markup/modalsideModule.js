angular.module('nav.markup.modalside', [])
    .directive('modalside', [function () {
        return {
            restrict   : 'A',
            replace    : true,
            transclude : true,
            templateUrl: '../js/common/directives/markup/modalsideTemplate.html'
        };
    }]);