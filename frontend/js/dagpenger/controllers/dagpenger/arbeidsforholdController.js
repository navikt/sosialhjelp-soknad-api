angular.module('nav.arbeidsforhold.controller', [])
    .controller('ArbeidsforholdCtrl', ['$scope', 'soknadService', '$cookieStore', '$location', 'data', function ($scope, soknadService, $cookieStore, $location, data) {
        $scope.soknadId = data.soknad.soknadId;

        $scope.templates = {
            'Sagt opp av arbeidsgiver': { oppsummeringsurl: '../views/templates/arbeidsforhold/sagt-opp-av-arbeidsgiver-oppsummering.html' },
            'Permittert': {oppsummeringsurl: '../views/templates/arbeidsforhold/permittert-oppsummering.html' },
            'Kontrakt utgått': {oppsummeringsurl: '../views/templates/arbeidsforhold/kontrakt-utgaatt-oppsummering.html'},
            'Sagt opp selv': {oppsummeringsurl: '../views/templates/arbeidsforhold/sagt-opp-selv-oppsummering.html' },
            'Redusert arbeidstid': {oppsummeringsurl: '../views/templates/arbeidsforhold/redusertarbeidstid-oppsummering.html' },
            'Arbeidsgiver er konkurs': {oppsummeringsurl: '../views/templates/arbeidsforhold/konkurs-oppsummering.html'},
            'Avskjediget': {oppsummeringsurl: '../views/templates/arbeidsforhold/avskjediget-oppsummering.html' }
        };

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
            var faktum = data.finnFaktum('arbeidstilstand');
            return sjekkOmGittEgenskapTilObjektErVerdi(faktum, "harIkkeJobbet");

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

            $scope.harKlikketKnapp = false;

            if (!$scope.hvisHarIkkeJobbet()) {
                $scope.harKlikketKnapp = true;
            }
        };

        $scope.nyttArbeidsforhold = function ($event) {
            $event.preventDefault();
            settArbeidsforholdCookie();
            $location.path('nyttarbeidsforhold');
        };

        $scope.endreArbeidsforhold = function (af, $index, $event) {
            $event.preventDefault();
            settArbeidsforholdCookie(af.arbeidsforhold.faktumId);
            $location.path('endrearbeidsforhold/' + af.arbeidsforhold.faktumId);
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

    }]);
