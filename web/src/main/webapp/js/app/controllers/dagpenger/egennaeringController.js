angular.module('nav.egennaering',[])
    .controller('EgennaeringCtrl', ['$scope', function ($scope) {
        $scope.navigering = {nesteside: 'verneplikt'};
        $scope.sidedata = {navn: 'egennaering'};

        $scope.validerEgennaering = function(form) {
            $scope.validateForm(form.$invalid);
        }
    }]);