angular.module('nav.barn', ['app.services'])

	.controller('BarneCtrl', ['$scope', 'Faktum', 'data', '$cookieStore', '$location', function ($scope, Faktum, data, $cookieStore, $location) {
		var soknadId = data.soknad.soknadId;
		var url = $location.$$url;
		var endreModus = url.indexOf('endrebarn') !== -1;
		var barnetilleggModus = url.indexOf('sokbarnetillegg') !== -1;

        $scope.soknadId = data.soknad.soknadId;
		$scope.nyttbarn = {barneinntekttall: undefined};
		$scope.barnetillegg = {value: 'true'};

        var barnetilleggsData;
		var ikkebarneinntekt;
		var barneinntekttall;
		var faktumId;

		if (endreModus) {
			faktumId = url.split('/').pop();
			var barnUnderEndring = {};

			if (data.finnFakta('barn').length > 0) {
				angular.forEach(data.finnFakta('barn'), function (value) {
					if (value.faktumId.toString() === faktumId) {
						barnUnderEndring = value;
					}
				});
			}
		}

		if (endreModus || barnetilleggModus) {
			faktumId = url.split('/').pop();
            var barnetillegg = data.finnFakta('barnetillegg')
			if (barnetillegg) {
				angular.forEach(barnetillegg, function (value) {
					if (value.parrentFaktum.toString() === faktumId) {
						$scope.barnetillegg = value;
						barnetilleggsData = value;
					}
				});
			}
            var ikkebarneinntektFaktum = data.finnFakta('ikkebarneinntekt')
			if (ikkebarneinntektFaktum) {
				angular.forEach(ikkebarneinntektFaktum, function (value) {
					if (value.parrentFaktum.toString() === faktumId) {
						$scope.ikkebarneinntekt = value;
						ikkebarneinntekt = value;
					}
				});
			}
            var barneinntekttallFaktum = data.finnFakta('barneinntekttall')
			if (barneinntekttallFaktum) {
				angular.forEach(barneinntekttallFaktum, function (value) {
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
            var barn = data.finnFakta('barn');
			angular.forEach(barn, function (value) {
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
					'alder'         : undefined,
                    'land'          : undefined
				}
			};
			$scope.barn = new Faktum(barneData);
            console.log(data.land);
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
				$scope.barn.properties.alder = $scope.finnAlder();
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
			$scope.barn.$save({soknadId: soknadId}).then(function (barnData) {
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

			$scope.barnetillegg.$save({soknadId: soknadId}).then(function (value) {
				$scope.barnetillegg = value;
				oppdaterFaktumListe('barnetillegg');
				//$scope.barn = new Faktum(barneData);

				if (ikkebarneinntekt === undefined) {
					ikkebarneinntekt = {
						key          : 'ikkebarneinntekt',
						value        : $scope.ikkebarneinntekt.value,
						parrentFaktum: parrentFaktumId
					};
				}

				$scope.nyttBarnIkkeBarneInntekt = new Faktum(ikkebarneinntekt);
				$scope.nyttBarnIkkeBarneInntekt.$save({soknadId: $scope.soknadId}).then(function (value) {
					$scope.ikkebarneinntekt = value;
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
						$scope.nyttBarnBarneInntektTall.$save({soknadId: $scope.soknadId}).then(function (value) {
							$scope.nyttbarn.barneinntekttall = value;

							data.leggTilFaktum($scope.nyttbarn.barneinntekttall);

							$scope.ikkebarneinntekt = false;
							$location.path('soknad/');
						});
					} else {
						$scope.ikkebarneinntekt = false;
						$location.path('soknad/');
					}
				});
			});
		}

		function oppdaterFaktumListe(type) {
            var faktaType = data.finnFakta(type);
			if (faktaType.length > 0) {
				if (endreModus) {
					angular.forEach(faktaType, function (value, index) {
						if (value.faktumId.toString() === $scope[type].faktumId) {
                            faktaType[index] = $scope[type];
						}
					})
				} else {
                    data.leggTilFaktum($scope[type]);
				}
			} else {
                data.leggTilFaktum($scope[type]);
			}
		}

		function finnSammensattNavn() {
			return $scope.barn.properties.fornavn + ' ' + $scope.barn.properties.etternavn;
		}

		//TODO: FIX Tester
		$scope.finnAlder =function() {
			if ($scope.barn.properties.fodselsdato) {
				var year = parseInt($scope.barn.properties.fodselsdato.split(".")[0]);
				var maaned = parseInt($scope.barn.properties.fodselsdato.split(".")[1]);
				var dag = parseInt($scope.barn.properties.fodselsdato.split(".")[2]);
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
