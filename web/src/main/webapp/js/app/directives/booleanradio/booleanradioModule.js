angular.module('nav.booleanradio',[])
    .directive('booleanradio', function() {
        return {
            restrict: "E",
            replace: true,
            scope: {
                model: '=',
                modus: '=',
                sporsmal: '=',
                svarAlternativ1: '=',
                svarAlternativ2: '=',
                name: '@'
            },
            controller: function($scope) {
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
            templateUrl: '../js/app/directives/booleanradio/booleanradioTemplate.html'
        }
    })
