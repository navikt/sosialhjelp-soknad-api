angular.module('nav.barn',['app.services'])
.controller('BarneCtrl', ['$scope', 'BrukerData', 'landService', function ($scope,BrukerData, landService) {
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
	$scope.nyttbarn = {barnetillegg:false};

	$scope.formAapent = false;
	$scope.barn = new BrukerData(barneData);

	landService.get().$promise.then(function (result) {
		$scope.landService = result;
	});

	$scope.nyttBarn = function() {
		$scope.formAapent = true;
	}

	$scope.slettBarn = function(barn) {

	}

	$scope.avbrytBarn = function() {
		$scope.barn = new BrukerData(barneData);
		$scope.formAapent = false;
	}

	$scope.lagreBarn = function (form) {
		$scope.runValidation();
		if (form.$valid) {
			$scope.barn.value.alder = finnAlder();
			$scope.barn.value.sammensattnavn = finnSammensattNavn();
			lagreBarnOgBarnetilleggFaktum();
		}
	}

        /**
		* Lagrer barnefaktum, tar vare på faktumId-en man får tilbake for så å lagre barnetilleggsfaktum basert på returnerte faktumID.
		* Til slutt legges de to faktumene inn i sine respektive lister for at de skal vises i "oppsummeringsmodus"
		**/
		function lagreBarnOgBarnetilleggFaktum() {
			$scope.barn.$jsoncreate({soknadId: $scope.soknadData.soknadId}).then(function(barnData) {
				$scope.barn = barnData;
				$scope.barn.value = angular.fromJson(barnData.value);

				var barnetilleggsData = {
					key: 'barnetillegg',
					value: $scope.nyttbarn.barnetillegg,
					parrentFaktum: barnData.faktumId
				};

				$scope.barnetillegg = new BrukerData(barnetilleggsData);
				$scope.barnetillegg.$create({soknadId: $scope.soknadData.soknadId}).then(function(data) {
					$scope.barnetillegg = data;
					oppdaterFaktumLister();

					$scope.barn = new BrukerData(barneData);
					$scope.nyttbarn.barnetillegg = false;
					$scope.formAapent = false;
				});
			});
		}

		function oppdaterFaktumLister() {
			if($scope.soknadData.fakta.barnetillegg && $scope.soknadData.fakta.barnetillegg.valuelist) {
				$scope.soknadData.fakta.barnetillegg.valuelist.push($scope.barnetillegg);
			} else {
				$scope.soknadData.fakta.barnetillegg = {};
				$scope.soknadData.fakta.barnetillegg.valuelist = [$scope.barnetillegg];
			}

			if($scope.soknadData.fakta.barn && $scope.soknadData.fakta.barn.valuelist) {
				$scope.soknadData.fakta.barn.valuelist.push($scope.barn);
			} else {
				$scope.soknadData.fakta.barn = {};
				$scope.soknadData.fakta.barn.valuelist = [$scope.barn];
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