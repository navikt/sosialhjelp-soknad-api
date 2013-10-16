angular.module('nav.checkbox',['nav.cmstekster', 'nav.input'])
    .directive('checkbox', [function() {
        return {
            restrict: "E",
            replace: true,
            scope: {
                model: '=',
                modus: '=',
                nokkel: '@'
            },
            controller: function($scope) {
                $scope.sporsmal = $scope.nokkel + ".sporsmal";
                $scope.name = $scope.nokkel.split('.').last();

                $scope.hvisIRedigeringsmodus = function() {
                    return $scope.modus;
                }

                $scope.hvisIOppsummeringsmodus = function () {
                    return !$scope.hvisIRedigeringsmodus();
                }

                $scope.hvisModelErTrue = function() {
                    return $scope.model == 'true';
                }

                $scope.hvisModelErFalse = function() {
                    return !$scope.hvisModelErTrue();
                }
            },
            templateUrl: '../js/app/directives/booleanradio/checkboxTemplate.html'
        }
    }]);
