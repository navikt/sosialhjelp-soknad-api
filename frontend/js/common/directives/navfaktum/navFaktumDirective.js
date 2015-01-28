angular.module('nav.navfaktum', [])
	.directive('navFaktumProperty', [function () {
		return {
			replace   : false,
			scope     : true,
			controller: function ($scope, $attrs) {
				var val = $scope.faktum.properties[$attrs.navFaktumProperty];
				$scope.parentFaktum = $scope.faktum;
				$scope.faktum = {key: $attrs.navFaktumProperty, value: val};

                $scope.$watch('faktum.value', function (newValue) {
					if (newValue) {
						$scope.parentFaktum.properties[$attrs.navFaktumProperty] = newValue;
					}
				});
			}};
	}])
	.directive('navFaktum', [function () {
		return {
			replace   : false,
			scope     : true,
			controller:  function ($scope, $attrs, $filter, data, Faktum) {
				var faktumNavn = $attrs.navFaktum.replace(/_/g, '.');
				$scope.ikkeAutoLagre = $attrs.ikkeAutoLagre;
				var satt = false;

				if ($scope[$attrs.navFaktum]) {
					$scope.faktum = $scope[$attrs.navFaktum];
					satt = true;
				} else if (!$attrs.navNyttFaktum) {
                    data.fakta.forEach(function (faktum) {
                        if (faktum.key === faktumNavn) {
							$scope.faktum = faktum;
							satt = true;
                        }
					});
				}

				if (!satt) {
                    $scope.faktum = new Faktum({
							key       : faktumNavn,
							soknadId  : data.soknad.soknadId,
							properties: {}
						}
					);
					data.fakta.push($scope.faktum);
				}
				$scope.parentFaktum = $scope.faktum;

				$scope.lagreFaktum = function () {
                    if($scope.$parent.faktum && $scope.faktum.key.indexOf($scope.$parent.faktum.key) >= 0){
                        $scope.parentFaktum.parrentFaktum = $scope.$parent.faktum.faktumId;
                    }

                    if (!$scope.ikkeAutoLagre) {
                        $scope.parentFaktum.$save();
					}
				};
				this.lagreFaktum = $scope.lagreFaktum;
			}
		};
	}]);
