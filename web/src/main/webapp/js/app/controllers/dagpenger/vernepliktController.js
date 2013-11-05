angular.module('nav.verneplikt',[])
    .controller('VernepliktCtrl', ['$scope', function ($scope) {
        $scope.navigering = {nesteside: 'utdanning'};
        $scope.sidedata = {navn: 'vernepliktig'};

        $scope.validerVerneplikt = function(form) {
            $scope.validateForm(form.$invalid);
            $scope.runValidation();
        }
    }]);