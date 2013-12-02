angular.module('nav.bildenavigering', [])
    .directive('bildenavigering', [function () {
        return {
            restrict: "A",
            replace: true,
            scope: {
                vedlegg: '='
            },
            link: function (scope, element) {
                scope.side = 0;
                scope.naviger = function (retning) {
                    element.find('img').attr('src', '');
                    scope.side = scope.side + retning;
                    if (scope.side < 0) {
                        scope.side = scope.vedlegg.antallSider - 1;
                    } else if (scope.side >= scope.vedlegg.antallSider) {
                        scope.side = 0;
                    }
                }
            },
            templateUrl: '../js/app/directives/bildenavigering/bildenavigeringTemplate.html'
        }
    }]);
