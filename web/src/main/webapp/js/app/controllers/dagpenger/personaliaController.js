angular.module('nav.personalia', [])
    .controller('PersonaliaCtrl', ['$scope', '$routeParams', 'data', 'personalia', function ($scope, $routeParams, data, personalia) {
        $scope.personalia = personalia;

        $scope.erMann = function() {
            if($scope.personalia && $scope.personalia.fakta && $scope.personalia.fakta.kjonn) {
                return $scope.personalia.fakta.kjonn.value == 'gutt';
            }
            return false;
        }

        $scope.erKvinne = function() {
            if($scope.personalia && $scope.personalia.fakta && $scope.personalia.fakta.kjonn) {
                return $scope.personalia.fakta.kjonn.value == 'jente';
            }
            return false;
        }

        $scope.harHentetPersonalia = function () {
            return $scope.personalia.fakta != undefined;
        }

        $scope.erUtenlandskStatsborger = function() {
            return $scope.personalia.statsborgerskap != 'NOR';
        }

        $scope.validerPersonalia = function (form) {
            // Har ikke form her ennå
            $scope.validateForm(false);
        }
    }]);