angular.module('nav.barn',['app.services'])

.controller('BarneCtrl', ['$scope', 'BrukerData','data', '$cookieStore', '$location', function ($scope,BrukerData,data,$cookieStore,$location) {
	var url = $location.$$url;
	var endreModus = url.indexOf("endrebarn") != -1;
	var barnetilleggModus = url.indexOf("sokbarnetillegg") != -1;

	$scope.nyttbarn = {barneinntekttall:undefined};	
	$scope.barnetillegg = {value: "true"}
	var barnetilleggsData;
	var ikkebarneinntekt;
	var barneinntekttall;
	
	if(endreModus) {
		var faktumId = url.split("/").pop();
		var barnUnderEndring = {};
		
		if ($scope.soknadData.fakta.barn) {
			angular.forEach($scope.soknadData.fakta.barn.valuelist, function(value) { 
				if(value.faktumId == faktumId) {
					barnUnderEndring = value;
				}
			});
		}

		if(barnUnderEndring.value) {
			barnUnderEndring.value = angular.fromJson(barnUnderEndring.value)
			barnUnderEndring.value.fodselsdato = new Date(barnUnderEndring.value.fodselsdato);
		}
	}

	if(endreModus || barnetilleggModus) {
		var faktumId = url.split("/").pop();
		if ($scope.soknadData.fakta.barnetillegg) {
			angular.forEach($scope.soknadData.fakta.barnetillegg.valuelist, function(value) { 
				if(value.parrentFaktum == faktumId) {
					$scope.barnetillegg = value;
					barnetilleggsData = value;
				}
			});
		}
		if ($scope.soknadData.fakta.ikkebarneinntekt) {
			angular.forEach($scope.soknadData.fakta.ikkebarneinntekt.valuelist, function(value) { 
				if(value.parrentFaktum == faktumId) {
					$scope.ikkebarneinntekt = value;
					ikkebarneinntekt = value;
				}
			});
		}
		if ($scope.soknadData.fakta.barneinntekttall) {
			angular.forEach($scope.soknadData.fakta.barneinntekttall.valuelist, function(value) { 
				if(value.parrentFaktum == faktumId) {
					$scope.nyttbarn.barneinntekttall = value;
					barneinntekttall = value;
				}
			});
		}
	}

	if(barnUnderEndring) {
		var barneData = barnUnderEndring;
		$scope.barn = new BrukerData(barneData);
		$scope.land = data.land;
	} else if(barnetilleggModus) {
		angular.forEach($scope.soknadData.fakta.barn.valuelist, function(value) { 
				if(value.faktumId == faktumId) {
					$scope.barnenavn = angular.fromJson(value.value).sammensattnavn;
				}
			});
	} else {
		var barneData = {
			key: 'barn',
			value: {
				"fodselsnummer":undefined,
				"fornavn":undefined,
				"etternavn":undefined,
				"sammensattnavn":undefined,
				"alder": undefined
			}
		};
		$scope.barn = new BrukerData(barneData);
		$scope.land = data.land;
	}

	$scope.barnetilleggErRegistrert = function() {
        return $scope.barnetillegg.value == 'true';
    }

	$scope.barnetHarInntekt = function() {
		if($scope.ikkebarneinntekt == undefined) {
			return false;
		}
		return sjekkOmGittEgenskapTilObjektErFalse($scope.ikkebarneinntekt);
	}

	$scope.barnetHarIkkeInntekt = function() {
		return !$scope.barnetHarInntekt();
	}

	$scope.lagreBarn = function (form) {
		var eventString = 'RUN_VALIDATION' + form.$name;
		$scope.$broadcast(eventString);
		if (form.$valid) {
			$scope.barn.value.alder = finnAlder();
			$scope.barn.value.sammensattnavn = finnSammensattNavn();
			lagreBarnOgBarnetilleggFaktum();
		}
	}

	$scope.lagreBarneFaktum = function(form) {
		var eventString = 'RUN_VALIDATION' + form.$name;
		$scope.$broadcast(eventString);
		if (form.$valid) {
			lagreTilleggsFaktum(url.split("/").pop());
		}
	}

	$scope.endrerSystemregistrertBarn = function() {
		return barnetilleggModus;
	}

	$scope.leggerTilNyttBarnEllerEndrerBarn = function() {
		return !$scope.endrerSystemregistrertBarn();
	}


	function oppdaterCookieValue(faktumId) {
		var barneCookie = $cookieStore.get('barneCookie');

		$cookieStore.put('barneCookie', {
			aapneTabs: barneCookie.aapneTabs,
			gjeldendeTab: barneCookie.gjeldendeTab,
			barneFaktumId: faktumId
		});
	}

        /**
		* Lagrer barnefaktum, tar vare på faktumId-en man får tilbake for så å lagre barnetilleggsfaktum basert på returnerte faktumID.
		* Til slutt legges de to faktumene inn i sine respektive lister for at de skal vises i "oppsummeringsmodus"
		**/
		function lagreBarnOgBarnetilleggFaktum() {
			$scope.barn.$jsoncreate({soknadId: $scope.soknadData.soknadId}).then(function(barnData) {
				$scope.barn = barnData;
				$scope.barn.value = angular.fromJson(barnData.value);
				oppdaterFaktumListe("barn");
				oppdaterCookieValue(barnData.faktumId);

				lagreTilleggsFaktum(barnData.faktumId);

			});

		}

		function lagreTilleggsFaktum(parrentFaktumId) {
			if(barnetilleggsData == undefined) {
				barnetilleggsData = {
					key: 'barnetillegg',
					value: true,
					parrentFaktum: parrentFaktumId
				};
			}

			$scope.barnetillegg = new BrukerData(barnetilleggsData);
			$scope.barnetillegg.$create({soknadId: $scope.soknadData.soknadId}).then(function(data) {
				$scope.barnetillegg = data;
				oppdaterFaktumListe("barnetillegg");

				$scope.barn = new BrukerData(barneData);

				if(ikkebarneinntekt == undefined) {
					ikkebarneinntekt = {
						key: 'ikkebarneinntekt',
						value: $scope.ikkebarneinntekt.value,
						parrentFaktum: parrentFaktumId
					};
				}

				$scope.nyttBarnIkkeBarneInntekt =  new BrukerData(ikkebarneinntekt);
				$scope.nyttBarnIkkeBarneInntekt.$create({soknadId: $scope.soknadData.soknadId}).then(function(data) {
					$scope.ikkebarneinntekt = data;
					oppdaterFaktumListe("ikkebarneinntekt");

					if($scope.barnetHarInntekt()) {
						if(barneinntekttall == undefined) {
							barneinntekttall = {
								key: 'barneinntekttall',
								value: $scope.nyttbarn.barneinntekttall.value,
								parrentFaktum: parrentFaktumId
							};
						}

						$scope.nyttBarnBarneInntektTall =  new BrukerData(barneinntekttall);
						$scope.nyttBarnBarneInntektTall.$create({soknadId: $scope.soknadData.soknadId}).then(function(data) {
							$scope.nyttbarn.barneinntekttall = data;

							if($scope.soknadData.fakta.barneinntekttall && $scope.soknadData.fakta.barneinntekttall) {
								$scope.soknadData.fakta.barneinntekttall.valuelist.push($scope.nyttbarn.barneinntekttall);
							} else {
								$scope.soknadData.fakta.barneinntekttall = {};
								$scope.soknadData.fakta.barneinntekttall.valuelist = [$scope.nyttbarn.barneinntekttall];
							}

							$scope.ikkebarneinntekt=false;
							$location.path('dagpenger/' + $scope.soknadData.soknadId);
						});
					} else {
						$scope.ikkebarneinntekt=false;
						$location.path('dagpenger/' + $scope.soknadData.soknadId);
					}

				});
});


}

function oppdaterFaktumListe(type) {
	if($scope.soknadData.fakta[type] && $scope.soknadData.fakta[type].valuelist) {
		if(endreModus) {
			angular.forEach($scope.soknadData.fakta[type].valuelist, function(value, index) {
				if(value.faktumId == $scope[type].faktumId){ 
					$scope.soknadData.fakta[type].valuelist[index] = $scope[type];
				} 
			})
		} else {
			$scope.soknadData.fakta[type].valuelist.push($scope[type]);
		}

	} else {
		$scope.soknadData.fakta[type] = {};
		$scope.soknadData.fakta[type].valuelist = [$scope[type]];
	}
}

function finnSammensattNavn() {
	return $scope.barn.value.fornavn + " " + $scope.barn.value.etternavn;
}

        //TODO: FIX Tester
        function finnAlder() {
        	if($scope.barn.value.fodselsdato) {
        		var year = $scope.barn.value.fodselsdato.getFullYear();
        		var maaned = $scope.barn.value.fodselsdato.getMonth();
        		var dag = $scope.barn.value.fodselsdato.getDate()
        		var dagensDato =  new Date();

        		var result = dagensDato.getFullYear() - year;

        		if(dagensDato.getMonth() < maaned) {
        			result--;
        		}

        		if(dagensDato.getMonth() == maaned && dagensDato.getDate() < dag) {
        			result--;
        		}

        		return result;
        	}
        }


    }]);