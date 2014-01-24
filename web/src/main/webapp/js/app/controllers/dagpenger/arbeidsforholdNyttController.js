angular.module('nav.arbeidsforhold.nyttarbeidsforhold.controller', [])
	.controller('ArbeidsforholdNyttCtrl', ['$scope', 'data', 'Faktum', '$location', '$cookieStore', function ($scope, data, Faktum, $location, $cookieStore) {

		$scope.templates = {
			'Kontrakt utgått'         : {url: '../html/templates/arbeidsforhold/kontrakt-utgaatt.html'},
			'Avskjediget'             : {url: '../html/templates/arbeidsforhold/avskjediget.html'},
			'Redusert arbeidstid'     : {url: '../html/templates/arbeidsforhold/redusertarbeidstid.html'},
			'Arbeidsgiver er konkurs' : {url: '../html/templates/arbeidsforhold/konkurs.html'},
			'Sagt opp av arbeidsgiver': {url: '../html/templates/arbeidsforhold/sagt-opp-av-arbeidsgiver.html'},
			'Sagt opp selv'           : {url: '../html/templates/arbeidsforhold/sagt-opp-selv.html'},
			'Permittert'              : {url: '../html/templates/arbeidsforhold/permittert.html'}
		};
		$scope.land = data.land;
        $scope.soknadId = data.soknad.soknadId;

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
                    'type': undefined
				}
			};
		}
		$scope.arbeidsforhold = new Faktum(arbeidsforholdData);
		$scope.sluttaarsak = $scope.arbeidsforhold;

		$scope.lagreArbeidsforhold = function (form) {
			var eventString = 'RUN_VALIDATION' + form.$name;
			$scope.$broadcast(eventString);
			$scope.validateForm(form.$invalid);
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
			var arbeidsforholdCookie = $cookieStore.get('arbeidsforhold');

			$cookieStore.put('arbeidsforhold', {
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
