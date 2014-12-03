angular.module('nav.arbeidsforhold.nyttarbeidsforhold.controller', [])
    .controller('ArbeidsforholdNyttCtrl', function ($scope, data, Faktum, $location, $cookieStore, $resource, cms, $q, permitteringPopup) {

        $scope.templates = {
            'Sagt opp av arbeidsgiver': {url: '../js/common/arbeidsforhold/templates/undersporsmaal/sagt-opp-av-arbeidsgiver.html'},
            'Permittert'              : {url: '../js/common/arbeidsforhold/templates/undersporsmaal/permittert.html'},
            'Kontrakt utgått'         : {url: '../js/common/arbeidsforhold/templates/undersporsmaal/kontrakt-utgaatt.html'},
            'Sagt opp selv'           : {url: '../js/common/arbeidsforhold/templates/undersporsmaal/sagt-opp-selv.html'},
            'Redusert arbeidstid'     : {url: '../js/common/arbeidsforhold/templates/undersporsmaal/redusertarbeidstid.html'},
            'Arbeidsgiver er konkurs' : {url: '../js/common/arbeidsforhold/templates/undersporsmaal/konkurs.html'},
			'Avskjediget'             : {url: '../js/common/arbeidsforhold/templates/undersporsmaal/avskjediget.html'}
		};
		$scope.land = data.land;
        $scope.soknadId = data.soknad.soknadId;
        $scope.behandlingId = data.soknad.brukerBehandlingId;
        $scope.soknadUrl = '/' + data.soknad.brukerBehandlingId + '/soknad';
        $scope.permitteringsperioder = [];
        $scope.permitteringsperioderTilSletting = [];

        $scope.settBreddeSlikAtDetFungererIIE = function () {
            setTimeout(function () {
                $("#land").width($("#land").width());
            }, 50);
        };

        var url = $location.$$url;

        var endreModus = url.indexOf('endrearbeidsforhold') !== -1;
        var arbeidsforholdData;

        if (endreModus) {
            var faktumId = url.split('/').pop();
            arbeidsforholdData = angular.copy(data.finnFaktumMedId(parseInt(faktumId)));
            $scope.permitteringsperioder = getPermitteringsPerioderMedParentFaktum(faktumId);

            angular.forEach($scope.templates, function (template, index) {
                if (arbeidsforholdData.properties.type === index) {
                    $scope.sluttaarsakType = index;
                }
            });
        } else {
            arbeidsforholdData = {
                key: 'arbeidsforhold',
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
		$scope.arbeidsforhold = new Faktum(arbeidsforholdData);
		$scope.sluttaarsak = $scope.arbeidsforhold;

		$scope.$watch(function () {
            if ($scope.arbeidsforhold.properties.land) {
                return $scope.arbeidsforhold.properties.land;
            }
        }, function () {
            if ($scope.arbeidsforhold.properties.land && $scope.arbeidsforhold.properties.land !== '') {
                $resource('/sendsoknad/rest/ereosland/:landkode').get(
                    {landkode: $scope.arbeidsforhold.properties.land},
                    function (eosdata) {
                        $scope.arbeidsforhold.properties.eosland = eosdata.result;
                    }
                );
            }
        });

        $scope.lagreArbeidsforhold = function (form, subform) {
            angular.forEach(subform, function(formname) {
              validerForm(formname);
            });

            validerForm(form.$name);
            $scope.runValidation(true);

            if (form.$valid) {
                settStartetForrigeAarProperty();
                slettPermitteringsperioder();
                lagreArbeidsforholdOgSluttaarsak();
            }
        };

        function validerForm(formname) {
            var eventString = 'RUN_VALIDATION' + formname;
            $scope.$broadcast(eventString);
        }

        $scope.aapneEndrePermitteringsperiode = function (permitteringsperiode) {
            permitteringPopup.openPopup($scope.permitteringsperioder, permitteringsperiode);
        };

        $scope.aapneLeggTilNyPermitteringsperiode = function (form) {
            if (form.$valid) {
                  permitteringPopup.openPopup($scope.permitteringsperioder);
            } else {
                validerForm(form.$name);
            }
        };

        $scope.leggPermitteringsperiodeTilSletting = function (permitteringsperiode) {
            if (permitteringsperiode.faktumId) {
                $scope.permitteringsperioderTilSletting.push(permitteringsperiode);
            }
            removeFromList($scope.permitteringsperioder, permitteringsperiode);
        };

        $scope.getForstePermitteringsperiode = function () {
            if ($scope.permitteringsperioder.length === 0) {
                var periode = {
                    key: 'arbeidsforhold.permitteringsperiode',
                    properties: {}
                };
                var faktum = new Faktum(periode);
                $scope.permitteringsperioder = [faktum];
            }
            return $scope.permitteringsperioder[0];
        };

        $scope.erLonnspliktigperiodeRequired = function () {
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
            console.log($scope.permitteringsperioder);
            $scope.arbeidsforhold.$save({soknadId: data.soknad.soknadId}).then(function (arbeidsforholdData) {
                var promises = [];

                $scope.arbeidsforhold = arbeidsforholdData;
                oppdaterFaktumListe(arbeidsforholdData, !endreModus);

                oppdaterCookieValue(arbeidsforholdData.faktumId);
                angular.forEach($scope.permitteringsperioder, function(permitteringsperiode) {
                    permitteringsperiode.properties.permitteringsperiodeTittel = permitteringsperiodeTittel(permitteringsperiode);
                    permitteringsperiode.parrentFaktum = arbeidsforholdData.faktumId;
                    oppdaterFaktumListe(permitteringsperiode, !permitteringsperiode.faktumId);
                    promises.push(permitteringsperiode.$save({soknadId: data.soknad.soknadId}));
                });

                $q.all(promises).then(function () {
                    $location.path($scope.soknadUrl);
                });

                function permitteringsperiodeTittel(faktum){
                    var navn = arbeidsforholdData.properties.arbeidsgivernavn;
                    var fra = lagNorskDatoformatFraIsoStandard(faktum.properties.permiteringsperiodedatofra);
                    var til = lagNorskDatoformatFraIsoStandard(faktum.properties.permiteringsperiodedatotil) || cms.tekster['arbeidsforhold.permitteringsperiode.vedlegg.paagaaende'];
                    var tilTekst = cms.tekster['arbeidsforhold.permitteringsperiode.vedlegg.til'];
                    return navn + " (" + fra + tilTekst + " " + til + ")";
                }

			});
        }

        function getPermitteringsPerioderMedParentFaktum(parentFaktumId) {
            var permitteringsperioder = [];
            angular.forEach(data.fakta, function(periode) {
                if(periode.parrentFaktum == parseInt(parentFaktumId)) {
                    permitteringsperioder.push(angular.copy(periode));
                }
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

        function oppdaterFaktumListe(objektMedNyData, skalIkkeEndre) {
            if (skalIkkeEndre) {
                data.fakta.push(objektMedNyData);
            } else {
                angular.forEach(data.fakta, function (value, index) {
                    if (value.faktumId === parseInt(objektMedNyData.faktumId)) {
                        data.fakta[index] = objektMedNyData;
                    }
                });
            }
        }

        function slettPermitteringsperioder() {
            angular.forEach($scope.permitteringsperioderTilSletting, function (permitteringsperiode) {
                if (permitteringsperiode.faktumId) {
                    data.slettFaktum(permitteringsperiode);
                }
            });
        }

        function removeFromList(list, element) {
            var index = list.indexOf(element);
            if (index >= 0) {
                list.splice(index, 1);
            }
        }

        function settPerioderKlarTilSletting() {
            angular.forEach($scope.permitteringsperioderTilSletting, function (periode) {
                $scope.leggPermitteringsperiodeTilSletting(periode);
            });
        }
    });
