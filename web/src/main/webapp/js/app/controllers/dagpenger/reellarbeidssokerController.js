angular.module('nav.reellarbeidssoker',[])
    .controller('ReellarbeidssokerCtrl', ['$scope', function ($scope) {
        $scope.navigering = {nesteside: 'egennaering'};
        $scope.sidedata = {navn: 'reellarbeidssoker'};

        $scope.validerReellarbeidssoker = function(form) {
            console.log(form.$error);
            $scope.validateForm(form.$invalid);
        }
    }]);