angular.module('nav.arbeidsforhold.controller',[])
 .controller('ArbeidsforholdCtrl', function ($scope, soknadService, landService, $routeParams) {
        $scope.arbeidsforhold = [];


        soknadService.get({param: $routeParams.soknadId}).$promise.then(function (result) {
            $scope.soknadData = result;
            if($scope.soknadData.fakta.arbeidsforhold) {
        		$scope.arbeidsforhold = angular.fromJson($scope.soknadData.fakta.arbeidsforhold.value);	
        	}

            if($scope.soknadData.fakta.harIkkeJobbet && $scope.soknadData.fakta.harIkkeJobbet.value == "true") {
                $scope.$broadcast("SETT_OPPSUMERINGSMODUS");
            }

            $scope.kanLeggeTilArbeidsforhold = function() {
                return $scope.arbeidsforholdskjemaErIkkeAapent() && $scope.harIkkeJobbetErIkkeSatt();
            }

            $scope.harIkkeJobbetErIkkeSatt = function() {
                if($scope.soknadData.fakta && $scope.soknadData.fakta.harIkkeJobbet) {
                    return $scope.soknadData.fakta.harIkkeJobbet.value != "true";
                } else {
                    return true;
                }
            }

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
                return $scope.arbeidsforhold.length == 0 &&  $scope.arbeidsforholdskjemaErIkkeAapent();
            }


	        $scope.nyttArbeidsforhold = function () {
	            $scope.arbeidsforholdaapen = true;
	            $scope.arbeidsgiver = {};
	        }

	        $scope.avbrytArbeidsforhold = function () {
	        	$scope.arbeidsforholdaapen = false;
	        }

            $scope.slettArbeidsforhold = function(af) {
                var i = $scope.arbeidsforhold.indexOf(af);                
                $scope.arbeidsforhold.splice(i,1);
                $scope.$emit("OPPDATER_OG_LAGRE_ARBEIDSFORHOLD", {key: 'arbeidsforhold', value: $scope.arbeidsforhold});
            }

            $scope.arbeidsforholdskjemaErIkkeAapent = function() {
                return !$scope.arbeidsforholdaapen;
            }

	        $scope.toggleRedigeringsmodus = function() {
        		if(harIkkeJobbet12SisteMaaneder()) {
        			$scope.$broadcast("SETT_OPPSUMERINGSMODUS");
        		}
        	}

            $scope.$on("ENDRET_TIL_REDIGERINGS_MODUS", function() {
                $scope.soknadData.fakta.harIkkeJobbet = false;
                $scope.$emit("OPPDATER_OG_LAGRE", {key: 'harIkkeJobbet', value: false});
            });

        	function harIkkeJobbet12SisteMaaneder() {
                if($scope.soknadData.fakta && $scope.soknadData.fakta.harIkkeJobbet) {
        		  return $scope.soknadData.fakta.harIkkeJobbet.value == "false";
                }
                //skjønte ikke heeeelt hvordan dette henger sammen.... Men nå funka det ved første trykk på ikkeJobbet også.
                return true;
        	}



            landService.get().$promise.then(function (result) {
                $scope.landService = result;
            });

              
        });
    })