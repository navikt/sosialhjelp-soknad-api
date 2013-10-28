angular.module('nav.arbeidsforhold.controller',[])
 .controller('ArbeidsforholdCtrl', function ($scope, soknadService, landService, $routeParams) {
        $scope.arbeidsforhold = [];
        $scope.posisjonForArbeidsforholdUnderRedigering = -1;
        $scope.arbeidsforholdaapen = false;

        $scope.navigering = {nesteside: 'egennaering'};
        $scope.sidedata = {navn: 'arbeidsforhold'};

        $scope.validerArbeidsforhold = function(form) {
            $scope.validateForm(form.$invalid);
        }


        $scope.arbeidsforholdetErIkkeIRedigeringsModus = function(index) {
            return $scope.posisjonForArbeidsforholdUnderRedigering != index;
        }

        $scope.templates = [{navn: 'Kontrakt utgått', url: '../html/templates/arbeidsforhold/kontrakt_utgaatt.html'}];
        $scope.template = $scope.templates[0];
        soknadService.get({param: $routeParams.soknadId}).$promise.then(function (result) {
            $scope.soknadData = result;
            if($scope.soknadData.fakta.arbeidsforhold) {
        		$scope.arbeidsforhold = angular.fromJson($scope.soknadData.fakta.arbeidsforhold.value);	
        	}



            if($scope.soknadData.fakta.harIkkeJobbet && $scope.soknadData.fakta.harIkkeJobbet.value == "true") {
                $scope.validateForm();
            }

            $scope.erSluttaarsakValgt = function() {
                if ($scope.sluttaarsak && $scope.sluttaarsak.navn) {
                    console.log("Sluttaarsak valgt");
                    return true;
                }else{
                    return false;
                }
            }

            $scope.kanLeggeTilArbeidsforhold = function() {
                return $scope.harIkkeRelevanteArbeidsforhold() && $scope.harIngenSkjemaAapne();
            }

            $scope.harIngenSkjemaAapne = function() {
                return $scope.posisjonForArbeidsforholdUnderRedigering == -1 && $scope.arbeidsforholdaapen == false;
            }

            $scope.harIkkeRelevanteArbeidsforhold = function() {
                if($scope.soknadData.fakta && $scope.soknadData.fakta.harIkkeJobbet) {
                    return $scope.soknadData.fakta.harIkkeJobbet.value != "true";
                } else {
                    return true;
                }
            }

            $scope.lagreArbeidsforhold = function(af) {
                $scope.$emit("OPPDATER_OG_LAGRE_ARBEIDSFORHOLD", {key: 'arbeidsforhold', value: $scope.arbeidsforhold});
                $scope.posisjonForArbeidsforholdUnderRedigering = -1;

                //todo refaktorer
                $scope.endreError = false;
            }


            $scope.harIkkeLagretArbeidsforhold = function () {
                return $scope.arbeidsforhold.length == 0 &&  $scope.arbeidsforholdskjemaErIkkeAapent();
            }

            $scope.avbrytEndringAvArbeidsforhold = function(af) {
                  $scope.posisjonForArbeidsforholdUnderRedigering = -1;
                    soknadService.get({param: $routeParams.soknadId}).$promise.then(function (result) {
                        $scope.soknadData = result;
                        if($scope.soknadData.fakta.arbeidsforhold) {
                            $scope.arbeidsforhold = angular.fromJson($scope.soknadData.fakta.arbeidsforhold.value); 
                        }
                    });

                    //todo refaktorer
                    $scope.endreError = false;
            }

            $scope.slettArbeidsforhold = function(af) {
                var i = $scope.arbeidsforhold.indexOf(af);                
                $scope.arbeidsforhold.splice(i,1);
                $scope.$emit("OPPDATER_OG_LAGRE_ARBEIDSFORHOLD", {key: 'arbeidsforhold', value: $scope.arbeidsforhold});
            }


            $scope.nyttArbeidsforhold = function () {
	            $scope.arbeidsforhold.push({});
                $scope.endreArbeidsforhold($scope.arbeidsforhold.length - 1);
	        }

            $scope.endreArbeidsforhold = function(index) {
                if($scope.posisjonForArbeidsforholdUnderRedigering == -1 && $scope.arbeidsforholdaapen == false) {
                    $scope.posisjonForArbeidsforholdUnderRedigering = index;
                    $scope.arbeidsforholdaapen = false;

                    //todo refaktorer
                    $scope.endreError = false;
                } else {
                    //todo refaktorer
                    $scope.endreError = true;
                }
                
            }

            $scope.arbeidsforholdskjemaErIkkeAapent = function() {
                return !$scope.arbeidsforholdaapen;
            }

	        $scope.toggleRedigeringsmodus = function() {
        		if(harIkkeJobbet12SisteMaaneder()) {
                    $scope.validateForm();
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


            $scope.$watch("arbeidsgiver.varighetFra", function(nyVerdi, gammelVerdi) {
                if($scope.arbeidsgiver && ($scope.arbeidsgiver.varighetTil <= $scope.arbeidsgiver.varighetFra)) {
                    $scope.arbeidsgiver.varighetTil = '';
                    $scope.datoError = true;
                } else {
                    $scope.datoError = false;
                }
            });
            $scope.resolvUrl = function (){
                return "../html/templates/kontrakt_utgaatt.html"
            }

            $scope.$watch("arbeidsgiver.varighetTil", function(nyVerdi, gammelVerdi) {
                if($scope.arbeidsgiver && ($scope.arbeidsgiver.varighetTil <= $scope.arbeidsgiver.varighetFra)) {
                    $scope.arbeidsgiver.varighetTil = '';
                    $scope.datoError = true;
                } else {
                    $scope.datoError = false;
                }
            });

            $scope.validateTilFraDato = function(af) {
                if(af && (af.varighetTil <= af.varighetFra)) {
                   af.varighetTil = '';
                    $scope.datoError = true;
                } else {
                    $scope.datoError = false;
                }
            }

            landService.get().$promise.then(function (result) {
                $scope.landService = result;
            });

              
        });
    })