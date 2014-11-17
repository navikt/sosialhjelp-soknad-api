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
        $scope.permitteringsPeriodeUrl = '/' + data.soknad.brukerBehandlingId + '/permitteringsperiode';
        $scope.barnefaktum = [];
        $scope.permitteringsperioder = [];
        $scope.permitteringsperioderTilSletting = [];

        datapersister.remove("permitteringsperiode");

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
		}
        $scope.settBreddeSlikAtDetFungererIIE();

        if(datapersister.get("arbeidsforholdData")) {
            arbeidsforholdData = datapersister.get("arbeidsforholdData");
            $scope.barnefaktum = datapersister.get("barnefaktum") || [];

            $scope.permitteringsperioder = $scope.permitteringsperioder.concat($scope.barnefaktum);
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
                slettPermitteringsperioder();
                lagreArbeidsforholdOgSluttaarsak();
            }
        };

        $scope.aapneEndrePermitteringsperiode = function(permitteringsperiode) {
            var index = $scope.permitteringsperioder.indexOf(permitteringsperiode)
            datapersister.set("permitteringsperiode", $scope.permitteringsperioder[index]);
            datapersister.set("permitteringsperioderTilSletting", $scope.permitteringsperioderTilSletting);
            $location.path($scope.permitteringsPeriodeUrl);
        };

        $scope.aapneLeggTilNyPermitteringsperiode = function() {
            datapersister.set("permitteringsperioderTilSletting", $scope.permitteringsperioderTilSletting);
            $location.path($scope.permitteringsPeriodeUrl);
        }


        $scope.leggPermitteringsperiodeTilSletting = function (permitteringsperiode){
            if(permitteringsperiode.faktumId) {
                $scope.permitteringsperioderTilSletting.push(permitteringsperiode);
            }
            removeFromList($scope.permitteringsperioder, permitteringsperiode);
            removeFromList($scope.barnefaktum, permitteringsperiode);
        };

        $scope.getForstePermitteringsperiode = function() {
            if($scope.permitteringsperioder.length === 0) {
                var periode = {
                    key: 'arbeidsforhold.permitteringsperiode',
                    properties: {}
                };
                var faktum = new Faktum(periode);
                $scope.barnefaktum.push(faktum);
                $scope.permitteringsperioder = [faktum];
            }
            return $scope.permitteringsperioder[0];
        };

        $scope.erLonnspliktigperiodeRequired = function() {
            return $scope.arbeidsforhold.properties.lonnspliktigperiodevetikke !== "true";
        };

        settPerioderKlarTilSletting();

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
                aapneTabs: arbeidsforholdCookie.aapneTabs,
                gjeldendeTab: arbeidsforholdCookie.gjeldendeTab,
                faktumId: faktumId
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

        function slettPermitteringsperioder() {
            angular.forEach($scope.permitteringsperioderTilSletting, function(permitteringsperiode){
               if(permitteringsperiode.faktumId){
                   data.slettFaktum(permitteringsperiode);
               }
            });

        }

        function removeFromList(list, element) {
            var index = list.indexOf(element);
            if(index >= 0) {
                list.splice(index, 1);
            }
        }

        function settPerioderKlarTilSletting() {
            var tilSletting = datapersister.get("permitteringsperioderTilSletting") || [];
            angular.forEach(tilSletting, function(periode) {
               $scope.leggPermitteringsperiodeTilSletting(periode);
            });
            datapersister.remove("permitteringsperioderTilSletting");
        }
	});
