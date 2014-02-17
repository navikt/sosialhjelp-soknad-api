angular.module('nav.personalia', [])
    .controller('PersonaliaCtrl', ['$scope', 'data', function ($scope, data) {
        $scope.personalia = data.finnFaktum('personalia').properties;

        $scope.brukerprofilUrl = data.config["soknad.brukerprofil.url"];
        $scope.erMann = function() {
            if($scope.personalia.kjonn) {
                return $scope.personalia.kjonn == 'm';
            }
            return false;
        };

        $scope.erKvinne = function() {
            if($scope.personalia.kjonn) {
                return $scope.personalia.kjonn == 'k';
            }
            return false;
        };

        $scope.harHentetPersonalia = function () {
            return $scope.personalia !== null;
        };

        $scope.erUtenlandskStatsborger = function() {
            return $scope.personalia.statsborgerskap != 'NOR';
        };


        // TODO: Trenger jo ikke validering når vi ikke har form
        $scope.valider = function (skalScrolle) {};
    }]);