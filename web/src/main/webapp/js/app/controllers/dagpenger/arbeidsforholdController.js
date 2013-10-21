/* .controller('ArbeidsforholdCtrl', function ($scope, soknadService, $routeParams) {
        $scope.arbeidsforhold = [];

        soknadService.get({param: $routeParams.soknadId}).$promise.then(function (result) {
            $scope.soknadData = result;
            if($scope.soknadData.fakta.arbeidsforhold) {
        		$scope.arbeidsforhold = angular.fromJson($scope.soknadData.fakta.arbeidsforhold.value);	
        	}
        	console.log($scope.arbeidsforhold);
        	$scope.lagreArbeidsforhold = function() {
	            $scope.arbeidsforhold.push({
                     navn: $scope.arbeidsgiver.navn,
                     land: $scope.arbeidsgiver.land,
                     varighetFra: $scope.arbeidsgiver.varighetFra,
                     varighetTil: $scope.arbeidsgiver.varighetTil,
                     sluttaarsak: $scope.arbeidsgiver.sluttaarsak 
                    });
	            $scope.arbeidsforholdaapen = false;
	            $scope.$emit("OPPDATER_OG_LAGRE_ARBEIDSFORHOLD", {key: 'arbeidsforhold', value: $scope.arbeidsforhold});
	        }

            $scope.harIkkeLagretArbeidsforhold = function () {
                return $scope.arbeidsforhold.length == 0;
            }


	        $scope.nyttArbeidsforhold = function () {
	            $scope.arbeidsforholdaapen = true;
	            $scope.arbeidsgiver = {};
	        }

	        $scope.avbrytArbeidsforhold = function () {
	        	$scope.arbeidsforholdaapen = false;
	        }

	        $scope.toggleRedigeringsmodus = function() {
        		if(harIkkeJobbet12SisteMaaneder()) {
        			$scope.$broadcast("SETT_OPPSUMERINGSMODUS");
        		}
        	}

        	function harIkkeJobbet12SisteMaaneder() {
                if($scope.soknadData.fakta && $scope.soknadData.fakta.harIkkeJobbet) {
        		  return $scope.soknadData.fakta.harIkkeJobbet.value == "false";
                }
                //skjønte ikke heeeelt hvordan dette henger sammen.... Men nå funka det ved første trykk på ikkeJobbet også.
                return true;
        	}
        });
    })*/