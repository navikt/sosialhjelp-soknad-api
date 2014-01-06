angular.module('nav.egennaering',[])
    .controller('EgennaeringCtrl', ['$scope', function ($scope) {
        $scope.navigering = {nesteside: 'verneplikt'};
        $scope.sidedata = {navn: 'egennaering'};

        $scope.$on('VALIDER_EGENNAERING', function () {
            $scope.validerEgennaering(false);
        });

        $scope.validerOgSettModusOppsummering = function(form) {
            $scope.validateForm(form.$invalid);
            $scope.validerEgennaering(true);
        }

        $scope.validerEgennaering = function(skalScrolle) {
            $scope.runValidation(skalScrolle);

        }
    }]);