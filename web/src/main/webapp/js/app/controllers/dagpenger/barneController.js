angular.module('nav.barn',['app.services'])
    .controller('BarneCtrl', ['$scope', 'BrukerData', 'landService', function ($scope,BrukerData, landService) {
    	var barneData = {
            key: 'barn',
            value: {
                    "fodselsnummer":undefined,
                    "fornavn":undefined,
                    "mellomnavn":undefined,
                    "etternavn":undefined,
                    "sammensattnavn":undefined,
                    "alder": undefined
                }
        };

		$scope.barn = new BrukerData(barneData);

	 	landService.get().$promise.then(function (result) {
            $scope.landService = result;
        });

        $scope.lagreBarn = function (form) {
            $scope.runValidation();
        	if (form.$valid) {
        		$scope.barn.value.alder = finnAlder();
        		$scope.barn.value.sammensattnavn = finnSammensattNavn();
	        	$scope.barn.$create({soknadId: $scope.soknadData.soknadId}).then(function(data) {
					$scope.barn = data;
					$scope.barn.value = angular.fromJson(data.value);
					if($scope.soknadData.fakta.barn && $scope.soknadData.fakta.barn.valuelist) {
						$scope.soknadData.fakta.barn.valuelist.push($scope.barn);
					} else {
						$scope.soknadData.fakta.barn = {};
						$scope.soknadData.fakta.barn.valuelist = [$scope.barn];

					}
					$scope.barn = new BrukerData(barneData);
	    		});
        	}
        }

        function finnSammensattNavn() {
        	if($scope.barn.value.mellomnavn) {
        		return $scope.barn.value.fornavn + " " + $scope.barn.value.mellomnavn + " " + $scope.barn.value.etternavn;
        	} else {
        		return $scope.barn.value.fornavn + " " + $scope.barn.value.etternavn;
        	}
        }

        //TODO: FIX Tester
        function finnAlder() {
			if($scope.barn.value.fodselsdato) {
				var year = $scope.barn.value.fodselsdato.getFullYear();
				var maaned = $scope.barn.value.fodselsdato.getMonth();
				var dag = $scope.barn.value.fodselsdato.getDate()
				var dagensDato =  new Date();

				var result = dagensDato.getFullYear() - year;

				if(maaned < dagensDato.getMonth()) {
					result--;
				}

				if(maaned == dagensDato.getMonth() && dag < dagensDato.getDate()) {
					result--;
				}

				return result;
			}
        }
    }]);