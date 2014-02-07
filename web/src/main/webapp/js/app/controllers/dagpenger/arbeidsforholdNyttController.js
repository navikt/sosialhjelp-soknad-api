angular.module('nav.arbeidsforhold.nyttarbeidsforhold.controller', [])
	.controller('ArbeidsforholdNyttCtrl', ['$scope', 'data', 'Faktum', '$location', '$cookieStore', '$resource', function ($scope, data, Faktum, $location, $cookieStore, $resource) {

		$scope.templates = {
            'Sagt opp av arbeidsgiver': {url: '../html/templates/arbeidsforhold/sagt-opp-av-arbeidsgiver.html'},
            'Permittert'              : {url: '../html/templates/arbeidsforhold/permittert.html'},
            'Kontrakt utgått'         : {url: '../html/templates/arbeidsforhold/kontrakt-utgaatt.html'},
            'Sagt opp selv'           : {url: '../html/templates/arbeidsforhold/sagt-opp-selv.html'},
            'Redusert arbeidstid'     : {url: '../html/templates/arbeidsforhold/redusertarbeidstid.html'},
            'Arbeidsgiver er konkurs' : {url: '../html/templates/arbeidsforhold/konkurs.html'},
			'Avskjediget'             : {url: '../html/templates/arbeidsforhold/avskjediget.html'}
		};
		$scope.land = data.land;
        $scope.soknadId = data.soknad.soknadId;

		$scope.sluttaarsakUrl = data.config["soknad.sluttaarsak.url"];
		$scope.lonnskravSkjema = data.config["soknad.lonnskravskjema.url"];
		$scope.permiteringUrl = data.config["soknad.permitteringsskjema.url"];

		var url = $location.$$url;
		var endreModus = url.indexOf('endrearbeidsforhold') !== -1;
		var arbeidsforholdData;

		if (endreModus) {
			var faktumId = url.split('/').pop();
			var arbeidsforhold = data.finnFakta('arbeidsforhold');

			angular.forEach(arbeidsforhold, function (value) {
				if (value.faktumId === parseInt(faktumId)) {
					arbeidsforholdData = value;
				}
			});


			angular.forEach($scope.templates, function (template, index) {
				if (arbeidsforholdData.properties.type === index) {
					$scope.sluttaarsakType = index;
				}
			});

		} else {
			arbeidsforholdData = {
				key       : 'arbeidsforhold',
				properties: {
					'arbeidsgivernavn': undefined,
					'datofra'         : undefined,
					'datotil'         : undefined,
                    'type': undefined,
                    'eosland': "false"
				}
			};
		}
		$scope.arbeidsforhold = new Faktum(arbeidsforholdData);
		$scope.sluttaarsak = $scope.arbeidsforhold;

		$scope.$watch(function () {
            if ($scope.arbeidsforhold.properties.land) {
                return $scope.arbeidsforhold.properties.land;
            }
        }, function () {
          	$resource('/sendsoknad/rest/ereosland/:landkode').get(
				{landkode: $scope.arbeidsforhold.properties.land},
	            function (eosdata) { // Success
	                $scope.arbeidsforhold.properties.eosland = eosdata.result;
	        });
        });


		$scope.lagreArbeidsforhold = function (form) {
			var eventString = 'RUN_VALIDATION' + form.$name;
			$scope.$broadcast(eventString);
			$scope.runValidation(true);

			if (form.$valid) {
				lagreArbeidsforholdOgSluttaarsak();
			}
		};

		function lagreArbeidsforholdOgSluttaarsak() {
            $scope.arbeidsforhold.$save({soknadId: data.soknad.soknadId}).then(function (arbeidsforholdData) {
				$scope.arbeidsforhold = arbeidsforholdData;
				oppdaterFaktumListe('arbeidsforhold');
				oppdaterCookieValue(arbeidsforholdData.faktumId);
                $location.path('soknad/');
			});
		}

		function oppdaterCookieValue(faktumId) {
			var arbeidsforholdCookie = $cookieStore.get('scrollTil');

			$cookieStore.put('scrollTil', {
				aapneTabs   : arbeidsforholdCookie.aapneTabs,
				gjeldendeTab: arbeidsforholdCookie.gjeldendeTab,
				faktumId    : faktumId
			});
		}

		function oppdaterFaktumListe(type) {
			if (!endreModus) {
				data.fakta.push($scope[type]);
			}
		}

		$scope.skalVisePermitteringInfo = false;

		$scope.settPermitteringsflagg = function ($event) {
			$scope.skalVisePermitteringInfo = $event.currentTarget.value < 50;
		};

	}]);
