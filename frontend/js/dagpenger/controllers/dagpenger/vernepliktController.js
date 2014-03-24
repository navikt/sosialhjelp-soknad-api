angular.module('nav.verneplikt', [])
	.controller('VernepliktCtrl', ['$scope', function ($scope) {

		$scope.navigering = {nesteside: 'utdanning'};
		$scope.sidedata = {navn: 'vernepliktig'};

		$scope.valider = function (skalScrolle) {
            var valid = $scope.runValidation(skalScrolle);
            if (valid) {
                $scope.lukkTab('verneplikt');
                $scope.settValidert('verneplikt');
            } else {
                $scope.apneTab('verneplikt');
            }
		};
	}]);
