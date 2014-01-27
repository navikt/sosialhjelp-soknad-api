angular.module('nav.barnetillegg', [])
	.controller('BarnetilleggCtrl', ['$scope', '$cookieStore', '$location', '$timeout', 'Faktum', 'data', function ($scope, $cookieStore, $location, $timeout, Faktum, data) {

        $scope.soknadId = data.soknad.soknadId;
        $scope.barn = data.finnFakta('barn');

        angular.forEach($scope.barn, function (b) {
            if (b.properties.barnetillegg === undefined) {
                b.properties.barnetillegg = 'false';
            }
            if (b.properties.ikkebarneinntekt === undefined) {
                b.properties.ikkebarneinntekt = 'true'
            }
        });


		$scope.erBrukerregistrert = function (barn) {
			return barn.type === 'BRUKERREGISTRERT';
		};

		$scope.erSystemRegistrert = function (barn) {
			return  barn.type === 'SYSTEMREGISTRERT';
		};

		$scope.ingenLandRegistrert = function (barn) {
			return !barn.properties.land
		};

		$scope.leggTilBarn = function ($event) {
			$event.preventDefault();
			settBarnCookie();
			$location.path('nyttbarn/');
		};

		$scope.endreBarn = function (faktumId, $event) {
			$event.preventDefault();
			settBarnCookie(faktumId);
			$location.path('endrebarn/' + faktumId);
		};

		$scope.sokbarnetillegg = function (faktumId, $event) {
			$event.preventDefault();
			settBarnCookie(faktumId);
			$location.path('sokbarnetillegg/' + faktumId);
		};

		$scope.slettBarn = function (b, index, $event) {
			$event.preventDefault();

            var barn = data.finnFakta('barn')
            barn.splice(index, 1);
			data.slettFaktum(b);

			$scope.barn = data.finnFakta('barn');
		};

		$scope.erGutt = function (barn) {
			return barn.properties.kjonn === 'm';
		};

		$scope.erJente = function (barn) {
			return barn.properties.kjonn === 'k';
		};

        $scope.barnetHarInntekt = function (barn) {
            return barn.properties.ikkebarneinntekt === 'false';
        };

        $scope.barnetHarIkkeInntekt = function (barn) {
            return !$scope.barnetHarInntekt(barn);
        };

        $scope.barnetilleggErRegistrert = function (barn) {
            return barn.properties.barnetillegg === 'true';
        };

        $scope.barnetilleggIkkeRegistrert = function (barn) {
            return !$scope.barnetilleggErRegistrert(barn);
        };

        $scope.slettBarnetillegg = function (barn, index, $event) {
            $event.preventDefault();

            barn.properties.barnetillegg = 'false';
            barn.$save();
        };

		$scope.validerBarnetillegg = function (form) {
			$scope.validateForm(form.$invalid);
			$scope.runValidation();
		};

		function settBarnCookie(faktumId) {
			var aapneTabIds = [];
			angular.forEach($scope.grupper, function (gruppe) {
				if (gruppe.apen) {
					aapneTabIds.push(gruppe.id);
				}
			});

			$cookieStore.put('barnetillegg', {
				aapneTabs   : aapneTabIds,
				gjeldendeTab: '#barnetillegg',
				faktumId    : faktumId
			})
		}

	}]);
