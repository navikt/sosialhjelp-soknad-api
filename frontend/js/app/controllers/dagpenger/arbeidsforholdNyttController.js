angular.module('nav.arbeidsforhold.nyttarbeidsforhold.controller', [])
	.controller('ArbeidsforholdNyttCtrl', ['$scope', 'data', 'Faktum', '$location', '$cookieStore', '$resource', 'cms', function ($scope, data, Faktum, $location, $cookieStore, $resource, cms) {

		$scope.templates = {
            'Sagt opp av arbeidsgiver': {url: '../views/templates/arbeidsforhold/sagt-opp-av-arbeidsgiver.html'},
            'Permittert'              : {url: '../views/templates/arbeidsforhold/permittert.html'},
            'Kontrakt utgått'         : {url: '../views/templates/arbeidsforhold/kontrakt-utgaatt.html'},
            'Sagt opp selv'           : {url: '../views/templates/arbeidsforhold/sagt-opp-selv.html'},
            'Redusert arbeidstid'     : {url: '../views/templates/arbeidsforhold/redusertarbeidstid.html'},
            'Arbeidsgiver er konkurs' : {url: '../views/templates/arbeidsforhold/konkurs.html'},
			'Avskjediget'             : {url: '../views/templates/arbeidsforhold/avskjediget.html'}
		};
		$scope.land = data.land;
        $scope.soknadId = data.soknad.soknadId;

        $scope.settBreddeSlikAtDetFungererIIE = function() {
            setTimeout(function() {
                $("#land").width($("#land").width());
            }, 50);
        };

		$scope.sluttaarsakUrl = data.config["soknad.sluttaarsak.url"];
		$scope.lonnskravSkjema = data.config["soknad.lonnskravskjema.url"];
		$scope.permiteringUrl = data.config["soknad.permitteringsskjema.url"];

		var url = $location.$$url;
		var endreModus = url.indexOf('endrearbeidsforhold') !== -1;
		var arbeidsforholdData;

		if (endreModus) {
			var faktumId = url.split('/').pop();
			var opprinneligData = data.finnFakta('arbeidsforhold');
            var arbeidsforhold = angular.copy(opprinneligData);

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

            $scope.settBreddeSlikAtDetFungererIIE();
		} else {
			arbeidsforholdData = {
				key       : 'arbeidsforhold',
				properties: {
					'arbeidsgivernavn': undefined,
					'datofra'         : undefined,
					'datotil'         : undefined,
                    'type': undefined,
                    'eosland': "false",
                    'land' : cms.tekster["arbeidsforhold.arbeidsgiver.landDefault"]
				}
			};
            $scope.settBreddeSlikAtDetFungererIIE();
		}
		$scope.arbeidsforhold = new Faktum(arbeidsforholdData);
		$scope.sluttaarsak = $scope.arbeidsforhold;

		$scope.$watch(function () {
            if ($scope.arbeidsforhold.properties.land) {
                return $scope.arbeidsforhold.properties.land;
            }
        }, function () {
            if($scope.arbeidsforhold.properties.land && $scope.arbeidsforhold.properties.land !== '') {
                $resource('/sendsoknad/rest/ereosland/:landkode').get(
                    {landkode: $scope.arbeidsforhold.properties.land},
                    function (eosdata) {
                        $scope.arbeidsforhold.properties.eosland = eosdata.result;
                    }
                );
            }
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
				oppdaterFaktumListe('arbeidsforhold', arbeidsforholdData);
				oppdaterCookieValue(arbeidsforholdData.faktumId);
                $location.path('soknad');
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

		function oppdaterFaktumListe(type, arbeidsforholdData) {
            if (!endreModus) {
				data.fakta.push($scope[type]);
			} else {
                angular.forEach(data.fakta, function (value, index) {
                    if (value.faktumId === parseInt(arbeidsforholdData.faktumId)) {
                        data.fakta[index] = arbeidsforholdData;
                    }
                });
            }
		}
	}]);
