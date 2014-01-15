angular.module('nav.barn', ['app.services'])

	.controller('BarneCtrl', ['$scope', 'Faktum', 'data', '$cookieStore', '$location', function ($scope, Faktum, data, $cookieStore, $location) {
		var url = $location.$$url;
		var endreModus = url.indexOf('endrebarn') !== -1;
		var barnetilleggModus = url.indexOf('sokbarnetillegg') !== -1;

		$scope.nyttbarn = {barneinntekttall: undefined};
		$scope.barnetillegg = {value: 'true'};
		var barnetilleggsData;
		var ikkebarneinntekt;
		var barneinntekttall;
		var faktumId;

		if (endreModus) {
			faktumId = url.split('/').pop();
			var barnUnderEndring = {};

			if ($scope.soknadData.fakta.barn) {
				angular.forEach($scope.soknadData.fakta.barn.valuelist, function (value) {
					if (value.faktumId.toString() === faktumId) {
						barnUnderEndring = value;
					}
				});
			}
		}

		if (endreModus || barnetilleggModus) {
			faktumId = url.split('/').pop();
			if ($scope.soknadData.fakta.barnetillegg) {
				angular.forEach($scope.soknadData.fakta.barnetillegg.valuelist, function (value) {
					if (value.parrentFaktum.toString() === faktumId) {
						$scope.barnetillegg = value;
						barnetilleggsData = value;
					}
				});
			}
			if ($scope.soknadData.fakta.ikkebarneinntekt) {
				angular.forEach($scope.soknadData.fakta.ikkebarneinntekt.valuelist, function (value) {
					if (value.parrentFaktum.toString() === faktumId) {
						$scope.ikkebarneinntekt = value;
						ikkebarneinntekt = value;
					}
				});
			}
			if ($scope.soknadData.fakta.barneinntekttall) {
				angular.forEach($scope.soknadData.fakta.barneinntekttall.valuelist, function (value) {
					if (value.parrentFaktum.toString() === faktumId) {
						$scope.nyttbarn.barneinntekttall = value;
						barneinntekttall = value;
					}
				});
			}
		}

		var barneData;
		if (barnUnderEndring) {
			barneData = barnUnderEndring;
			$scope.barn = new Faktum(barneData);
			$scope.land = data.land;
		} else if (barnetilleggModus) {
			angular.forEach($scope.soknadData.fakta.barn.valuelist, function (value) {
				if (value.faktumId.toString() === faktumId) {
					$scope.barnenavn = value.properties.sammensattnavn;
				}
			});
		} else {
			barneData = {
				key       : 'barn',
				properties: {
					'fnr'           : undefined,
					'fornavn'       : undefined,
					'etternavn'     : undefined,
					'sammensattnavn': undefined,
					'alder'         : undefined
				}
			};
			$scope.barn = new Faktum(barneData);
			$scope.land = data.land;
		}

		$scope.barnetilleggErRegistrert = function () {
			return $scope.barnetillegg.value === 'true';
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

		$scope.lagreBarn = function (form) {
			var eventString = 'RUN_VALIDATION' + form.$name;
			$scope.$broadcast(eventString);
			$scope.validateForm(form.$invalid);
			$scope.runValidation(true);

			if (form.$valid) {
				$scope.barn.properties.alder = finnAlder();
				$scope.barn.properties.sammensattnavn = finnSammensattNavn();
				lagreBarnOgBarnetilleggFaktum();
			}
		};

		$scope.lagreBarneFaktum = function (form) {
			var eventString = 'RUN_VALIDATION' + form.$name;
			$scope.$broadcast(eventString);
			if (form.$valid) {
				lagreTilleggsFaktum(url.split('/').pop());
			}
		};

		$scope.endrerSystemregistrertBarn = function () {
			return barnetilleggModus;
		};

		$scope.leggerTilNyttBarnEllerEndrerBarn = function () {
			return !$scope.endrerSystemregistrertBarn();
		};

		function oppdaterCookieValue(faktumId) {
			var barneCookie = $cookieStore.get('barn');
			$cookieStore.put('barn', {
				aapneTabs   : barneCookie.aapneTabs,
				gjeldendeTab: barneCookie.gjeldendeTab,
				faktumId    : faktumId
			});
		}

		/**
		 * Lagrer barnefaktum, tar vare på faktumId-en man får tilbake for så å lagre barnetilleggsfaktum basert på returnerte faktumID.
		 * Til slutt legges de to faktumene inn i sine respektive lister for at de skal vises i 'oppsummeringsmodus'
		 **/
		function lagreBarnOgBarnetilleggFaktum() {
			$scope.barn.$save({soknadId: $scope.soknadData.soknadId}).then(function (barnData) {
				$scope.barn = barnData;
				oppdaterFaktumListe('barn');
				oppdaterCookieValue(barnData.faktumId);
				lagreTilleggsFaktum(barnData.faktumId);
			});
		}

		function lagreTilleggsFaktum(parrentFaktumId) {
			if (barnetilleggsData === undefined) {
				barnetilleggsData = {
					key          : 'barnetillegg',
					value        : true,
					parrentFaktum: parrentFaktumId
				};
			}

			$scope.barnetillegg = new Faktum(barnetilleggsData);

			$scope.barnetillegg.$save({soknadId: $scope.soknadData.soknadId}).then(function (data) {
				$scope.barnetillegg = data;
				oppdaterFaktumListe('barnetillegg');
				$scope.barn = new Faktum(barneData);

				if (ikkebarneinntekt === undefined) {
					ikkebarneinntekt = {
						key          : 'ikkebarneinntekt',
						value        : $scope.ikkebarneinntekt.value,
						parrentFaktum: parrentFaktumId
					};
				}

				$scope.nyttBarnIkkeBarneInntekt = new Faktum(ikkebarneinntekt);
				$scope.nyttBarnIkkeBarneInntekt.$save({soknadId: $scope.soknadData.soknadId}).then(function (data) {
					$scope.ikkebarneinntekt = data;
					oppdaterFaktumListe('ikkebarneinntekt');

					if ($scope.barnetHarInntekt()) {
						if (barneinntekttall === undefined) {
							barneinntekttall = {
								key          : 'barneinntekttall',
								value        : $scope.nyttbarn.barneinntekttall.value,
								parrentFaktum: parrentFaktumId
							};
						}

						$scope.nyttBarnBarneInntektTall = new Faktum(barneinntekttall);
						$scope.nyttBarnBarneInntektTall.$save({soknadId: $scope.soknadData.soknadId}).then(function (data) {
							$scope.nyttbarn.barneinntekttall = data;

							if ($scope.soknadData.fakta.barneinntekttall && $scope.soknadData.fakta.barneinntekttall) {
								$scope.soknadData.fakta.barneinntekttall.valuelist.push($scope.nyttbarn.barneinntekttall);
							} else {
								$scope.soknadData.fakta.barneinntekttall = {};
								$scope.soknadData.fakta.barneinntekttall.valuelist = [$scope.nyttbarn.barneinntekttall];
							}

							$scope.ikkebarneinntekt = false;
							$location.path('dagpenger/' + $scope.soknadData.soknadId);
						});
					} else {
						$scope.ikkebarneinntekt = false;
						$location.path('dagpenger/' + $scope.soknadData.soknadId);
					}
				});
			});
		}

		function oppdaterFaktumListe(type) {
			if ($scope.soknadData.fakta[type] && $scope.soknadData.fakta[type].valuelist) {
				if (endreModus) {
					angular.forEach($scope.soknadData.fakta[type].valuelist, function (value, index) {
						if (value.faktumId.toString() === $scope[type].faktumId) {
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
			return $scope.barn.properties.fornavn + ' ' + $scope.barn.properties.etternavn;
		}

		//TODO: FIX Tester
		function finnAlder() {
			if ($scope.barn.properties.fodselsdato) {
				var year = $scope.barn.properties.fodselsdato.substring(0, 4);
				var maaned = $scope.barn.properties.fodselsdato.substring(5, 7);
				var dag = $scope.barn.properties.fodselsdato.substring(8, 10);
				var dagensDato = new Date();
				var result = dagensDato.getFullYear() - year;

				if (dagensDato.getMonth() + 1 < maaned) {
					result--;
				}

				if (dagensDato.getMonth() + 1 === maaned && dagensDato.getDate() < dag) {
					result--;
				}
				return result;
			}
			return 'undefined';
		}
	}]);
