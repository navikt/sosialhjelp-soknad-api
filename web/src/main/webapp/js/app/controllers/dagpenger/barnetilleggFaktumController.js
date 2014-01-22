angular.module('nav.barnetilleggfaktum', ['app.services'])
	.controller('BarnetilleggFaktumCtrl', ['$scope', 'Faktum', 'data', function ($scope, Faktum, data) {
		var soknadId = data.soknad.soknadId;

		var barnetilleggsData = {
			key          : 'barnetillegg',
			value        : false,
			parrentFaktum: $scope.b.faktumId
		};

		var ikkebarneinntektData = {
			key          : 'ikkebarneinntekt',
			value        : false,
			parrentFaktum: $scope.b.faktumId
		};

		var barneinntekttallData = {
			key          : 'barneinntekttall',
			value        : undefined,
			parrentFaktum: $scope.b.faktumId
		};

        var barnetillegg = data.finnFakta('barnetillegg');
		if (barnetillegg) {
			angular.forEach(barnetillegg, function (value) {
				if (value.parrentFaktum === $scope.b.faktumId) {
					barnetilleggsData = value;
				}
			});
		}
        var ikkebarneinntekt = data.finnFakta('ikkebarneinntekt');
		if (ikkebarneinntekt) {
			angular.forEach(ikkebarneinntekt, function (value) {
				if (value.parrentFaktum === $scope.b.faktumId) {
					ikkebarneinntektData = value;
				}
			});
		}
        var barneinntekttall = data.finnFakta('barneinntekttall');
		if (barneinntekttall) {
			angular.forEach(barneinntekttall, function (value) {
				if (value.parrentFaktum === $scope.b.faktumId) {
                    barneinntekttallData = value;
				}
			});
		}

		$scope.barnetillegg = new Faktum(barnetilleggsData);
		$scope.$watch('barnetillegg.value', function (newValue, oldValue, scope) {
			if (newValue !== undefined && newValue !== oldValue) {
				scope.barnetillegg.$save({soknadId: soknadId}).then(function (data) {
					scope.barnetillegg = data;
				});
			}
		});

		$scope.ikkebarneinntekt = new Faktum(ikkebarneinntektData);
		$scope.$watch('ikkebarneinntekt.value', function (newValue, oldValue, scope) {
			if (newValue !== undefined && newValue !== oldValue) {
				scope.ikkebarneinntekt.$save({soknadId: soknadId}).then(function (data) {
					scope.ikkebarneinntekt = data;
				});
			}
		});

		$scope.barneinntekttall = new Faktum(barneinntekttallData);
		$scope.$watch('barneinntekttall.value', function (newValue, oldValue, scope) {
			if (newValue !== undefined && newValue !== oldValue) {
				scope.barneinntekttall.$save({soknadId: soknadId}).then(function (data) {
					scope.barneinntekttall = data;
				});
			}
		});

		$scope.barnetilleggErRegistrert = function () {
			return $scope.barnetillegg.value === 'true';
		};

		$scope.barnetilleggIkkeRegistrert = function () {
			return !$scope.barnetilleggErRegistrert();
		};

		$scope.barnetHarInntekt = function () {
			if ($scope.ikkebarneinntekt === undefined) {
				return false;
			}
			return sjekkOmGittEgenskapTilObjektErFalse($scope.ikkebarneinntekt);
		};

		$scope.barnetHarIkkeInntekt = function () {
			return !$scope.barnetHarInntekt();
		};

		$scope.slettBarnetillegg = function (faktumId, index, $event) {
			$event.preventDefault();
			var barnetilleggsData;

            var barnetilleggFaktum = data.finnFakta('barnetillegg')
			angular.forEach(barnetilleggFaktum, function (value) {
				if (value.parrentFaktum === faktumId) {
					barnetilleggsData = value;
				}
			});

			$scope.barnetillegg.value = 'false';
			data.slettFaktum(barnetilleggsData);

		};

	}]);
