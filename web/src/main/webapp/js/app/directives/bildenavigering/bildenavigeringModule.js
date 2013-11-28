angular.module('nav.bildenavigering',[])
    .directive('bildenavigering', [function() {
        return {
            restrict: "A",
            replace: true,
            scope: {
                vedlegg: '='
            },
            link: function(scope, element) {
                scope.naviger = function(retning){
                    element.find('img').attr('src', '');
                    scope.vedlegg.side = scope.vedlegg.side + retning;
                }
            },
            templateUrl: '../js/app/directives/bildenavigering/bildenavigeringTemplate.html'


        }
    }]);
