angular.module('nav.booleanradio',['nav.cmstekster', 'nav.input'])
    .directive('booleanradio', [function() {
        return {
            restrict: "A",
            replace: true,
            require: 'ngModel',
            scope: {
                model: '=ngModel',
                modus: '=',
                nokkel: '@'
            },
            controller: function($scope) {
                $scope.sporsmal = $scope.nokkel + ".sporsmal";
                $scope.trueLabel = $scope.nokkel + ".true";
                $scope.falseLabel = $scope.nokkel + ".false";
                $scope.feilmelding = $scope.nokkel + ".feilmelding";
                $scope.inputname = $scope.nokkel.split('.').last();

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
            templateUrl: '../js/common/directives/booleanradio/booleanradioTemplate.html'


        }
    }]);
