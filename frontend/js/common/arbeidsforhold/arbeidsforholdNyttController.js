angular.module('nav.arbeidsforhold.nyttarbeidsforhold.controller', [])
	.controller('ArbeidsforholdNyttCtrl', function ($scope, data, Faktum, $location, $cookieStore, $resource, cms, $q, datapersister) {

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
        $scope.behandlingId = data.soknad.brukerBehandlingId;
        $scope.soknadUrl = '/' + data.soknad.brukerBehandlingId + '/soknad';
        $scope.barnefaktum = [];
        $scope.permitteringsperioder =[];

        $scope.settBreddeSlikAtDetFungererIIE = function() {
            setTimeout(function() {
                $("#land").width($("#land").width());
            }, 50);
        };

		var url = $location.$$url;

        var endreModus = url.indexOf('endrearbeidsforhold') !== -1;
        var arbeidsforholdData;

        if (endreModus) {
            var faktumId = url.split('/').pop();
            var opprinneligData = data.finnFakta('arbeidsforhold');
            var arbeidsforhold = angular.copy(opprinneligData);
            $scope.permitteringsperioder = getPermitteringsPerioderMedParentFaktum(faktumId);

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
        } else if(datapersister.get("arbeidsforholdData")) {
            arbeidsforholdData = datapersister.get("arbeidsforholdData");
            $scope.barnefaktum = datapersister.get("barnefaktum") || [];
            $scope.permitteringsperioder = $scope.permitteringsperioder.concat($scope.barnefaktum);
        } else {
            arbeidsforholdData = {
				key       : 'arbeidsforhold',
				properties: {
                    'startetForrigeAar': 'false',
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
        datapersister.set("arbeidsforholdData", arbeidsforholdData);
		$scope.arbeidsforhold = new Faktum(arbeidsforholdData);
		$scope.sluttaarsak = $scope.arbeidsforhold;
        datapersister.set("barnefaktum", $scope.barnefaktum);

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
                settStartetForrigeAarProperty();
                lagreArbeidsforholdOgSluttaarsak();
			}
		};

        function getArbeidsforholdSluttDato() {
            switch ($scope.arbeidsforhold.properties.type) {
                case 'Arbeidsgiver er konkurs':
                    return $scope.arbeidsforhold.properties.konkursdato;
                case 'Redusert arbeidstid':
                   return $scope.arbeidsforhold.properties.redusertfra;
                case 'Permittert':
                    return $scope.arbeidsforhold.properties.permiteringsperiodedatofra;
                default:
                    return $scope.arbeidsforhold.properties.datotil;
            }
        }

        function settStartetForrigeAarProperty() {
            var innevaerendeAar = new Date().getFullYear();
            var arbeidsforholdSluttAar = new Date(getArbeidsforholdSluttDato()).getFullYear();
            var arbeidsforholdetErFraForegaaendeAar = innevaerendeAar - arbeidsforholdSluttAar === 1;
            var startetIJanuarEllerFebruar = data.finnFaktum('lonnsOgTrekkOppgave').value === "true";

            if (startetIJanuarEllerFebruar && arbeidsforholdetErFraForegaaendeAar) {
                $scope.arbeidsforhold.properties.startetForrigeAar = 'true';
            } else {
                $scope.arbeidsforhold.properties.startetForrigeAar = 'false';
            }
        }

		function lagreArbeidsforholdOgSluttaarsak() {
            $scope.arbeidsforhold.$save({soknadId: data.soknad.soknadId}).then(function (arbeidsforholdData) {
                var promises = [];

                $scope.arbeidsforhold = arbeidsforholdData;
				oppdaterFaktumListe('arbeidsforhold', arbeidsforholdData);
				oppdaterCookieValue(arbeidsforholdData.faktumId);

                angular.forEach($scope.barnefaktum, function(faktum) {
                    console.log("lagre!");
                    faktum.parrentFaktum = arbeidsforholdData.faktumId;
                    promises.push(faktum.$save({soknadId: data.soknad.soknadId}));
                    data.fakta.push(faktum);
                });

                $q.all(promises).then(function() {
                    datapersister.remove("arbeidsforholdData");
                    $location.path($scope.soknadUrl);
                });
			});
        }

        function getPermitteringsPerioderMedParentFaktum(parentFaktumId) {
            var permitteringsperioder = data.finnFakta('arbeidsforhold.permitteringsperiode') || [];
            permitteringsperioder = permitteringsperioder.filter(function(permitteringsperiode) {
               return (permitteringsperiode.parrentFaktum === parseInt(parentFaktumId));
            });
            return permitteringsperioder;
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
	});
