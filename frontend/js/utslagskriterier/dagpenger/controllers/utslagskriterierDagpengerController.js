angular.module('nav.utslagskriterierDagpenger', [])
    .controller('utslagskritererDagpengerCtrl', function ($scope) {
        $scope.gjenopptak = {
            harMotattDagpenger: null
        };

        $scope.valider = function (skalScrolle) {
            var valid = $scope.runValidation(skalScrolle);
        };
    });