angular.module('nav.verneplikt', [])
	.controller('VernepliktCtrl', ['$scope', function ($scope) {

		$scope.navigering = {nesteside: 'utdanning'};
		$scope.sidedata = {navn: 'vernepliktig'};

		$scope.$on('VALIDER_VERNEPLIKT', function () {
			$scope.validerVerneplikt(false);
		});

		$scope.validerOgSettModusOppsummering = function (form) {
			$scope.validateForm(form.$invalid);
			$scope.validerVerneplikt(true);
		};

		$scope.validerVerneplikt = function (skalScrolle) {
			$scope.runValidation(skalScrolle);
		};
	}]);
