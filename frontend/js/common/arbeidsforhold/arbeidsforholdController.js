angular.module('nav.arbeidsforhold.controller', ['nav.arbeidsforhold.turnus.directive'])
    .controller('ArbeidsforholdCtrl', function ($scope, $cookieStore, $location, data) {
        $scope.templates = {
            'Sagt opp av arbeidsgiver': { oppsummeringsurl: '../js/common/arbeidsforhold/templates/oppsummeringer/sagt-opp-av-arbeidsgiver-oppsummering.html' },
            'Permittert': {oppsummeringsurl: '../js/common/arbeidsforhold/templates/oppsummeringer/permittert-oppsummering.html' },
            'Kontrakt utgått': {oppsummeringsurl: '../js/common/arbeidsforhold/templates/oppsummeringer/arbeidsforhold/kontrakt-utgaatt-oppsummering.html'},
            'Sagt opp selv': {oppsummeringsurl: '../js/common/arbeidsforhold/templates/oppsummeringer/arbeidsforhold/sagt-opp-selv-oppsummering.html' },
            'Redusert arbeidstid': {oppsummeringsurl: '../js/common/arbeidsforhold/templates/oppsummeringer/templates/arbeidsforhold/redusertarbeidstid-oppsummering.html' },
            'Arbeidsgiver er konkurs': {oppsummeringsurl: '../js/common/arbeidsforhold/templates/oppsummeringer/views/templates/arbeidsforhold/konkurs-oppsummering.html'}
        };

        $scope.soknadId = data.soknad.soknadId;

        var arbeidsforhold = data.finnFakta('arbeidsforhold');
        $scope.arbeidsliste = [];

        angular.forEach(arbeidsforhold, function (af) {
            $scope.arbeidsliste.push({'arbeidsforhold': af, 'sluttaarsak': af});
        });

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
