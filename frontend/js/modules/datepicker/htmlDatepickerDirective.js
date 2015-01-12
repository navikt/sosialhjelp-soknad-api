angular.module('nav.datepicker.directive', [])
    .directive('htmlDatepicker', function ($timeout) {
        return {
            restrict: 'A',
            require: '^form',
            replace: true,
            templateUrl: '../js/modules/datepicker/templates/html5DatepickerTemplate.html',
            scope: {
                model: '=htmlDatepicker',
                harFokus: '=',
                erRequired: '=?',
                disabled: '=?',
                lagre: '&',
                requiredErrorMessage: '@',
                name: '@'
            },
            link: function(scope, element, attrs, form) {
                var eventForAValidereHeleFormen = 'RUN_VALIDATION' + form.$name;
                scope.$on(eventForAValidereHeleFormen, function () {
                    form[scope.name].$touched = true;
                    form[scope.name].$untouched = false;
                });

                scope.blur = function () {
                    scope.harFokus = false;

                    if (scope.lagre) {
                        $timeout(scope.lagre, 100);
                    }
                };

                scope.focus = function () {
                    scope.harFokus = true;
                };
            }
        };
    });

