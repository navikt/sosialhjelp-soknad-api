angular.module('nav.egennaering',[])
    .controller('EgennaeringCtrl', ['$scope', function ($scope) {
        $scope.navigering = {nesteside: 'verneplikt'};
        $scope.sidedata = {navn: 'egennaering'};

        $scope.$on('VALIDER_EGENNAERING', function (scope, form) {
            $scope.validerEgennaering(form, false);
        });

        $scope.validerEgennaering = function(form, skalScrolle) {
            $scope.validateForm(form.$invalid);
            $scope.runValidation(skalScrolle);

        }
    }]);