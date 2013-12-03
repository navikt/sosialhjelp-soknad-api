angular.module('nav.verneplikt',[])
    .controller('VernepliktCtrl', ['$scope', function ($scope) {
        $scope.navigering = {nesteside: 'utdanning'};
        $scope.sidedata = {navn: 'vernepliktig'};

        $scope.$on('VALIDER_VERNEPLIKT', function (scope, form) {
            $scope.validerVerneplikt(form);
        });

        $scope.validerVerneplikt = function(form) {
            $scope.validateForm(form.$invalid);
            $scope.runValidation();
        }
    }]);