angular.module('nav.reberegning', [])
	.controller('ReberegningCtrl', ['$scope', function ($scope) {

		$scope.navigering = {nesteside: 'egennaering'};
		$scope.sidedata = {navn: 'reberegning'};

		$scope.valider = function (skalScrolle) {
            var valid = $scope.runValidation(skalScrolle);
            if (valid) {
                $scope.lukkTab('reberegning');
                $scope.settValidert('reberegning');
            } else {
                $scope.apneTab('reberegning');
            }
		};
	}]);
