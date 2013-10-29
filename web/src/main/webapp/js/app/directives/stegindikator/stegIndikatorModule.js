angular.module('nav.stegindikator', ['nav.cmstekster'])
    .directive('stegindikator', [function () {
        return {
            restrict: "E",
            replace: true,
            templateUrl: '../js/app/directives/stegindikator/stegIndikatorTemplate.html',
            scope: {
                aktivIndex: '@',
                stegListe: '@'
            },
            controller: function($scope) {
                $scope.stegListe = $scope.stegListe.replace(/ /g, '');
                $scope.data = {
                    liste: $scope.stegListe.trim().split(',')
                };


            }
        }
    }]);