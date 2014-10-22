angular.module('nav.routingForGjenopptak', [])
    .controller('routingForGjenopptakCtrl', function ($scope) {
        $scope.gjenopptak = {
            harMotattDagpenger: null,
            harArbeidet: null
        };

        $scope.valider = function (skalScrolle) {
            $scope.runValidation(skalScrolle);
        };
    });