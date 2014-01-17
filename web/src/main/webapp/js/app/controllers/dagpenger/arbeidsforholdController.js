angular.module('nav.arbeidsforhold.controller', [])
    .controller('ArbeidsforholdCtrl', function ($scope, soknadService, landService, $routeParams, $cookieStore, $location, data, Faktum) {

        $scope.$on('VALIDER_ARBEIDSFORHOLD', function () {
            $scope.validerArbeidsforhold(false);
        });

        $scope.templates = {
            'Kontrakt utgått': {oppsummeringsurl: '../html/templates/arbeidsforhold/kontrakt-utgaatt-oppsummering.html'},
            'Avskjediget': {oppsummeringsurl: '../html/templates/arbeidsforhold/avskjediget-oppsummering.html' },
            'Redusert arbeidstid': {oppsummeringsurl: '../html/templates/arbeidsforhold/redusertarbeidstid-oppsummering.html' },
            'Arbeidsgiver er konkurs': {oppsummeringsurl: '../html/templates/arbeidsforhold/konkurs-oppsummering.html'},
            'Sagt opp av arbeidsgiver': { oppsummeringsurl: '../html/templates/arbeidsforhold/sagt-opp-av-arbeidsgiver-oppsummering.html' },
            'Sagt opp selv': {oppsummeringsurl: '../html/templates/arbeidsforhold/sagt-opp-selv-oppsummering.html' },
            'Permittert': {oppsummeringsurl: '../html/templates/arbeidsforhold/permittert-oppsummering.html' }
        };

        var arbeidsforhold = data.finnFakta('arbeidsforhold');
        var sluttaarsak = data.finnFakta('sluttaarsak');
        $scope.arbeidsliste = [];

        angular.forEach(arbeidsforhold, function (af) {
            angular.forEach(sluttaarsak, function (s) {
                if (s.parrentFaktum === af.faktumId) {
                    $scope.arbeidsliste.push({'arbeidsforhold': af, 'sluttaarsak': s});
                }
            });

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

        $scope.arbeidsliste.sort(compareArbeidsforholdDate);

        if ($scope.arbeidsliste.length > 0) {
            $scope.harLagretArbeidsforhold = true;
            $scope.harFeil = false;
        } else {
            $scope.harLagretArbeidsforhold = undefined;
        }

        $scope.skalViseFeil = function () {
            return $scope.harFeil === true;
        }

        $scope.harSvart = function () {
            return $scope.hvisHarJobbet() || $scope.hvisHarIkkeJobbet();
        }

        $scope.$watch(function () {
            if (data.finnFaktum('arbeidstilstand')) {
                return data.finnFaktum('arbeidstilstand').value === 'harIkkeJobbet';
            }
        }, function () {
            $scope.harFeil = false;
        })


        $scope.hvisHarJobbet = function () {
            var faktum = data.finnFaktum('arbeidstilstand');

            return faktum && faktum.value && faktum.value !== 'harIkkeJobbet';
        };

        $scope.hvisHarIkkeJobbet = function () {
            var faktum = data.finnFaktum('arbeidstilstand');

            return faktum && faktum.value && faktum.value === 'harIkkeJobbet';
        }
        $scope.hvisHarJobbetVarierende = function () {
            var faktum = data.finnFaktum('arbeidstilstand');

            return faktum && faktum.value && faktum.value === 'varierendeArbeidstid';
        };

        $scope.hvisHarJobbetFast = function () {
            var faktum = data.finnFaktum('arbeidstilstand');
            return faktum && faktum.value && faktum.value === 'fastArbeidstid';
        };

        $scope.validerOgSettModusOppsummering = function (form) {
            $scope.validateForm(form.$invalid);
            $scope.validerArbeidsforhold(true);
            $scope.harFeil = false;

            if (!$scope.hvisHarIkkeJobbet()) {
                $scope.harFeil = true;
            }
        };

        $scope.validerArbeidsforhold = function (skalScrolle) {
            $scope.runValidation(skalScrolle);
        };

        $scope.nyttArbeidsforhold = function ($event) {
            $event.preventDefault();
            settArbeidsforholdCookie();
            $location.path('nyttarbeidsforhold/' + data.soknad.soknadId);
        };

        $scope.endreArbeidsforhold = function (af, $index, $event) {
            $event.preventDefault();
            settArbeidsforholdCookie(af.arbeidsforhold.faktumId);
            $location.path('endrearbeidsforhold/' + data.soknad.soknadId + '/' + af.arbeidsforhold.faktumId);
        };

        $scope.slettArbeidsforhold = function (af, index, $event) {
            $event.preventDefault();

            $scope.arbeidsliste.splice(index, 1);
            data.slettFaktum(af.arbeidsforhold);

            if ($scope.arbeidsliste.length === 0) {
                $scope.harLagretArbeidsforhold = undefined;
            }
            $scope.harFeil = false;
        };


        function settArbeidsforholdCookie(faktumId) {
            var aapneTabIds = [];
            angular.forEach($scope.grupper, function (gruppe) {
                if (gruppe.apen) {
                    aapneTabIds.push(gruppe.id);
                }
            });

            $cookieStore.put('arbeidsforhold', {
                aapneTabs: aapneTabIds,
                gjeldendeTab: '#arbeidsforhold',
                faktumId: faktumId
            })
        }

    });
