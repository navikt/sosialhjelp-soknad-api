angular.module('nav.verneplikt',[])
    .controller('VernepliktCtrl', ['$scope', function ($scope) {
        $scope.navigering = {nesteside: 'utdanning'};
        $scope.sidedata = {navn: 'vernepliktig'};

        $scope.$on('VALIDER_VERNEPLIKT', function (scope, form) {
            $scope.validerVerneplikt(form, false);
        });

        $scope.validerVerneplikt = function(form, skalScrolle ) {
            $scope.validateForm(form.$invalid);
            $scope.runValidation(skalScrolle);

        }
    }]);