angular.module('nav.arbeidsforhold.controller')
    .controller('ArbeidsforholdCtrl', function ($scope, $cookieStore, $location, data) {

        $scope.soknadId = data.soknad.soknadId;
        $scope.land = data.land;

        var arbeidsforhold = data.finnFakta('arbeidsforhold');
        $scope.arbeidsliste = [];

        angular.forEach(arbeidsforhold, function (af) {
            $scope.arbeidsliste.push({'arbeidsforhold': af, 'sluttaarsak': af});
        });

        $scope.settBreddeSlikAtDetFungererIIE = function() {
            setTimeout(function() {
                $("#land").width($("#land").width());
            }, 50);
        };

        $scope.settBreddeSlikAtDetFungererIIE();

        function compareArbeidsforholdDate(a1, a2) {
            if (a1.sluttaarsak.properties.datofra > a2.sluttaarsak.properties.datofra) {
                return 1;
            }
            if (a1.sluttaarsak.properties.datofra < a2.sluttaarsak.properties.datofra) {
                return -1;
            }
            return 0;
        }

        $scope.harArbeidsforhold = function() {
            return $scope.arbeidsliste.length > 0;
        };

        $scope.arbeidsliste.sort(compareArbeidsforholdDate);

        if ($scope.arbeidsliste.length > 0) {
            $scope.harLagretArbeidsforhold = true;
            $scope.harKlikketKnapp = false;
        } else {
            $scope.harLagretArbeidsforhold = undefined;
        }

        $scope.finnLandFraLandkode = function(landkode) {
            for(var i=0; i<data.land.result.length; i++) {
                if(data.land.result[i].value === landkode) {
                    return data.land.result[i].text;
                }
            }
            return "";
        };

        $scope.skalViseFeil = function () {
            return $scope.harKlikketKnapp === true && !$scope.harLagretArbeidsforhold;
        };

        $scope.harSvart = function () {
            return $scope.hvisHarJobbet() || $scope.hvisHarIkkeJobbet();
        };

        $scope.$watch(function () {
            if (data.finnFaktum('arbeidstilstand')) {
                return data.finnFaktum('arbeidstilstand').value === 'harIkkeJobbet';
            }
        }, function () {
            $scope.harKlikketKnapp = false;
        });


        $scope.hvisHarJobbet = function () {
            var faktum = data.finnFaktum('arbeidstilstand');
            return !sjekkOmGittEgenskapTilObjektErVerdi(faktum, "harIkkeJobbet");
        };

        $scope.hvisHarIkkeJobbet = function () {
            return !$scope.hvisHarJobbet();
        };

        $scope.hvisHarJobbetVarierende = function () {
            var faktum = data.finnFaktum('arbeidstilstand');
            return sjekkOmGittEgenskapTilObjektErVerdi(faktum, "varierendeArbeidstid");
        };

        $scope.hvisHarJobbetFast = function () {
            var faktum = data.finnFaktum('arbeidstilstand');
            return sjekkOmGittEgenskapTilObjektErVerdi(faktum, "fastArbeidstid");
        };

        $scope.valider = function (skalScrolle) {
            var valid = $scope.runValidation(skalScrolle);
            if (valid) {
                $scope.lukkTab('arbeidsforhold');
                $scope.settValidert('arbeidsforhold');
            } else {
                $scope.apneTab('arbeidsforhold');
            }

            $scope.harKlikketKnapp = $scope.hvisHarJobbet();
        };

        $scope.nyttArbeidsforhold = function ($event) {
            $event.preventDefault();
            settArbeidsforholdCookie();
            $location.path(data.soknad.brukerBehandlingId + '/nyttarbeidsforhold');
        };

        $scope.endreArbeidsforhold = function (af, $index, $event) {
            $event.preventDefault();
            settArbeidsforholdCookie(af.arbeidsforhold.faktumId);
            $location.path(data.soknad.brukerBehandlingId + '/endrearbeidsforhold/' + af.arbeidsforhold.faktumId);
        };

        $scope.slettArbeidsforhold = function (af, index, $event) {
            $event.preventDefault();
            $scope.arbeidsliste.splice(index, 1);

            data.slettFaktum(af.arbeidsforhold);

            if ($scope.arbeidsliste.length === 0) {
                $scope.harLagretArbeidsforhold = undefined;
            }
            $scope.harKlikketKnapp = false;
        };

        $scope.erUtenlandskStatsborger = function() {
            var personalia = data.finnFaktum('personalia');
            if(personalia && personalia.properties){
                return personalia.properties.statsborgerskap !== 'NOR';
            }
            return false;
        };


        function settArbeidsforholdCookie(faktumId) {
            var aapneTabIds = [];
            angular.forEach($scope.grupper, function (gruppe) {
                if (gruppe.apen) {
                    aapneTabIds.push(gruppe.id);
                }
            });

            $cookieStore.put('scrollTil', {
                aapneTabs: aapneTabIds,
                gjeldendeTab: '#arbeidsforhold',
                faktumId: faktumId
            });
        }

    });
