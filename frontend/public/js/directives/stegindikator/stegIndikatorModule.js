angular.module('nav.stegindikator', ['nav.cmstekster'])
    .directive('stegindikator', [function () {
        return {
            restrict: 'A',
            replace: true,
            templateUrl: '../js/app/directives/stegindikator/stegIndikatorTemplate.html',
            scope: {
                'aktivIndex': '@',
                'stegListe': '@'
            },
            controller: function($scope) {
                $scope.stegListe = $scope.stegListe.replace(/ /g, '');
                $scope.data = {
                    'liste': $.trim($scope.stegListe).split(',')
                };
            }
        }
    }]);
