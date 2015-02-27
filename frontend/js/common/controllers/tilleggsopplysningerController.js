angular.module('nav.tilleggsopplysninger', [])
    .controller('TilleggsopplysningerCtrl', ['$scope', function ($scope) {
        $scope.sidedata = {navn: 'tilleggsopplysninger'};

        $scope.valider = function (skalScrolle) {
            var valid = $scope.runValidation(skalScrolle);
            if (valid) {
                $scope.lukkTab('tilleggsopplysninger');
                $scope.settValidert('tilleggsopplysninger');
            } else {
                $scope.apneTab('tilleggsopplysninger');
            }
        };

        $scope.leggTilValideringsmetode('tilleggsopplysninger', $scope.valider);
    }]);
