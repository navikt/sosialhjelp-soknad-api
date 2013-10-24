angular.module('nav.reellarbeidssoker',[])
    .controller('ReellarbeidssokerCtrl', ['$scope', function ($scope) {
        $scope.navigering = {nesteside: 'arbeidsforhold'};
        $scope.sidedata = {navn: 'reellarbeidssoker'};

        $scope.validerReellarbeidssoker = function(form) {
            $scope.validateForm(form.$invalid);
        }
    }]);