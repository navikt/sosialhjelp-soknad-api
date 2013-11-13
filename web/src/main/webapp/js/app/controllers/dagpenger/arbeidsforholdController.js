angular.module('nav.arbeidsforhold.controller', [])
    .controller('ArbeidsforholdCtrl', function ($scope, soknadService, landService, $routeParams) {

        $scope.showErrors = false;

        $scope.arbeidsforhold = [];
        $scope.endreArbeidsforholdKopi = '';
        $scope.posisjonForArbeidsforholdUnderRedigering = -1;
        $scope.arbeidsforholdaapen = false;

        $scope.navigering = {nesteside: 'egennaering'};
        $scope.sidedata = {navn: 'arbeidsforhold'};

        $scope.validerArbeidsforhold = function (form) {
            if($scope.harIkkeLagretArbeidsforhold() && harIkkeJobbet12SisteMaaneder()) {
                form.$setValidity('arbeidsforhold.feilmelding', false);
            }
            $scope.validateForm(form.$invalid);
            $scope.runValidation();
        }

        $scope.templates = [
            {navn: 'Kontrakt utgått', url: '../html/templates/arbeidsforhold/kontrakt-utgaatt.html', oppsummeringsurl: '../html/templates/arbeidsforhold/kontrakt-utgaatt-oppsummering.html'},
            {navn: 'Avskjediget', url: '../html/templates/arbeidsforhold/avskjediget.html', oppsummeringsurl: '../html/templates/arbeidsforhold/avskjediget-oppsummering.html' },
            {navn: 'Redusert arbeidstid', url: '../html/templates/arbeidsforhold/redusertarbeidstid.html', oppsummeringsurl: '../html/templates/arbeidsforhold/redusertarbeidstid-oppsummering.html' },
            {navn: 'Arbeidsgiver er konkurs', url: '../html/templates/arbeidsforhold/konkurs.html', oppsummeringsurl: '../html/templates/arbeidsforhold/konkurs-oppsummering.html'},
            {navn: 'Sagt opp av arbeidsgiver', url: '../html/templates/arbeidsforhold/sagt-opp-av-arbeidsgiver.html', oppsummeringsurl: '../html/templates/arbeidsforhold/sagt-opp-av-arbeidsgiver-oppsummering.html' },
            {navn: 'Sagt opp selv', url: '../html/templates/arbeidsforhold/sagt-opp-selv.html', oppsummeringsurl: '../html/templates/arbeidsforhold/sagt-opp-selv-oppsummering.html' },
            {navn: 'Permittert', url: '../html/templates/arbeidsforhold/permittert.html', oppsummeringsurl: '../html/templates/arbeidsforhold/permittert-oppsummering.html' }
        ];

        $scope.template = $scope.templates[0];

        $scope.arbeidsforholdetErIkkeIRedigeringsModus = function (index) {
            return $scope.posisjonForArbeidsforholdUnderRedigering != index;
        }


        soknadService.get({param: $routeParams.soknadId}).$promise.then(function (result) {
            $scope.soknadData = result;
            if ($scope.soknadData.fakta.arbeidsforhold) {
                $scope.arbeidsforhold = angular.fromJson($scope.soknadData.fakta.arbeidsforhold.value);
            }


            if ($scope.soknadData.fakta.harIkkeJobbet && $scope.soknadData.fakta.harIkkeJobbet.value == "true") {
                $scope.validateForm();
            }

            $scope.erSluttaarsakValgt = function () {
                if ($scope.sluttaarsak && $scope.sluttaarsak.navn) {
                    return true;
                } else {
                    return false;
                }
            }

            $scope.kanLeggeTilArbeidsforhold = function () {
                return $scope.harIkkeRelevanteArbeidsforhold() && $scope.harIngenSkjemaAapne();
            }

            $scope.harIngenSkjemaAapne = function () {
                return $scope.posisjonForArbeidsforholdUnderRedigering == -1 && $scope.arbeidsforholdaapen == false;
            }

            $scope.harIkkeRelevanteArbeidsforhold = function () {
                if ($scope.soknadData.fakta && $scope.soknadData.fakta.harIkkeJobbet) {
                    return $scope.soknadData.fakta.harIkkeJobbet.value != "true";
                } else {
                    return true;
                }
            }

            $scope.harIkkeLagretArbeidsforhold = function () {
                return $scope.arbeidsforhold.length == 0 && $scope.arbeidsforholdskjemaErIkkeAapent();
            }

            $scope.slettArbeidsforhold = function (af) {
                var i = $scope.arbeidsforhold.indexOf(af);
                $scope.arbeidsforhold.splice(i, 1);

                var arbeidsforholdKopi = $scope.arbeidsforhold.slice(0);
                if ($scope.posisjonForArbeidsforholdUnderRedigering > i) {
                    $scope.posisjonForArbeidsforholdUnderRedigering--;
                    arbeidsforholdKopi.splice($scope.posisjonForArbeidsforholdUnderRedigering, 1);
                }

                var value = angular.toJson(arbeidsforholdKopi);
                $scope.$emit("OPPDATER_OG_LAGRE", {key: 'arbeidsforhold', value: value});
            }


            $scope.nyttArbeidsforhold = function (event, form) {
                if ($scope.ikkeUnderRedigering() && form) {
                    $scope.arbeidsforhold.push({});
                    $scope.endreArbeidsforhold($scope.arbeidsforhold.length - 1);
                    form.$setValidity('arbeidsforhold.leggtilnyttarbeidsforhold.feilmelding', true);
                }
                else {
                    form.$setValidity('arbeidsforhold.leggtilnyttarbeidsforhold.feilmelding', false);
                    $scope.runValidation();
                }
            }

            $scope.endreArbeidsforhold = function (index, form) {
                if ($scope.ikkeUnderRedigering()) {
                    $scope.endreArbeidsforholdKopi = $.extend(true, [], $scope.arbeidsforhold)[index];
                    $scope.posisjonForArbeidsforholdUnderRedigering = index;
                    $scope.arbeidsforholdaapen = false;

                    if (form) {
                        form.$setValidity('arbeidsforhold.endrearbeidsforhold.feilmelding', true);
                    }
                } else {
                    form.$setValidity('arbeidsforhold.endrearbeidsforhold.feilmelding', false);
                    $scope.runValidation();
                }

            }

            $scope.ikkeUnderRedigering = function () {
                return $scope.posisjonForArbeidsforholdUnderRedigering == -1 && $scope.arbeidsforholdaapen == false;
            }

            $scope.arbeidsforholdskjemaErIkkeAapent = function () {
                return !$scope.arbeidsforholdaapen;
            }

            $scope.toggleRedigeringsmodus = function (form) {
                form.$setValidity('arbeidsforhold.feilmelding', true);
                if (harIkkeJobbet12SisteMaaneder()) {
                    $scope.validateForm(form.$invalid);
                }
            }

            $scope.$on("ENDRET_TIL_REDIGERINGS_MODUS", function () {
                $scope.soknadData.fakta.harIkkeJobbet = false;
                $scope.$emit("OPPDATER_OG_LAGRE", {key: 'harIkkeJobbet', value: false});
            });



            $scope.$watch("arbeidsgiver.varighetFra", function (nyVerdi, gammelVerdi) {
                if ($scope.arbeidsgiver && ($scope.arbeidsgiver.varighetTil <= $scope.arbeidsgiver.varighetFra)) {
                    $scope.arbeidsgiver.varighetTil = '';
                    $scope.datoError = true;
                } else {
                    $scope.datoError = false;
                }
            });
            $scope.resolvUrl = function () {
                return "../html/templates/kontrakt-utgaatt.html"
            }

            $scope.$watch("arbeidsgiver.varighetTil", function (nyVerdi, gammelVerdi) {
                if ($scope.arbeidsgiver && ($scope.arbeidsgiver.varighetTil <= $scope.arbeidsgiver.varighetFra)) {
                    $scope.arbeidsgiver.varighetTil = '';
                    $scope.datoError = true;
                } else {
                    $scope.datoError = false;
                }
            });

            $scope.validateTilFraDato = function (af) {
                if (af && (af.varighetTil <= af.varighetFra)) {
                    af.varighetTil = '';
                    $scope.datoError = true;
                } else {
                    $scope.datoError = false;
                }
            }

            $scope.validateRedusertArbeidstidDato = function (af) {
                if (af && (af.redusertArbeidstid.fra < af.varighetFra) || (af.redusertArbeidstid.fra > af.varighetTil)) {
                    af.redusertArbeidstid = '';
                    $scope.redusertArbeidstidDatoError = true;
                } else {
                    $scope.redusertArbeidstidDatoError = false;
                }
            }

            $scope.validatePermittertDato = function (af) {
                if (af && (af.permittertFraDato < af.varighetFra) || (af.permittertFraDato > af.varighetTil) && (af.permittertFraDato > af.permittertTilDato)) {
                    af.permittertFraDato = '';
                    af.permittertTilDato = '';
                    $scope.permittertDatoError = true;
                } else {
                    $scope.permittertDatoError = false;
                }
            }

            $scope.validateOppsigelsestidTilFraDato = function (af) {
                if (af && (af.sagtOppAvArbeidsgiverVarighetTil <= af.sagtOppAvArbeidsgiverVarighetFra)) {
                    af.sagtOppAvArbeidsgiverVarighetTil = '';
                    $scope.oppsigelsestidDatoError = true;
                } else {
                    $scope.oppsigelsestidDatoError = false;
                }
            }

            $scope.settRedigeringsIndex = function(nyIndex) {
                $scope.posisjonForArbeidsforholdUnderRedigering = nyIndex;
            }

            landService.get().$promise.then(function (result) {
                $scope.landService = result;
            });


        });
        function harIkkeJobbet12SisteMaaneder() {
            if ($scope.soknadData.fakta && $scope.soknadData.fakta.harIkkeJobbet) {
                return $scope.soknadData.fakta.harIkkeJobbet.value == "false";
            }
            return true;
        }

    })

    .controller('LeggTilArbeidsforholdCtrl', function ($scope, soknadService, landService, $routeParams) {
        $scope.lagreArbeidsforhold = function (af, form) {
            form.$setValidity('arbeidsforhold.feilmelding', true);
            form.$setValidity('arbeidsforhold.endrearbeidsforhold.feilmelding', true);
            form.$setValidity('arbeidsforhold.leggtilnyttarbeidsforhold.feilmelding', true);
            $scope.runValidation();
            if (form.$valid) {
                var value = angular.toJson($scope.arbeidsforhold);

                $scope.$emit("OPPDATER_OG_LAGRE", {key: 'arbeidsforhold', value: value});
                $scope.settRedigeringsIndex(-1);
                $scope.endreArbeidsforholdKopi = '';
            }
        }

        $scope.avbrytEndringAvArbeidsforhold = function (af, form) {
            var index = $scope.posisjonForArbeidsforholdUnderRedigering;
            if (Object.keys($scope.endreArbeidsforholdKopi).length == 0) {
                $scope.arbeidsforhold.splice(index, 1);
            } else {
                $scope.arbeidsforhold[index] = $scope.endreArbeidsforholdKopi;
            }
            $scope.endreArbeidsforholdKopi = '';
            $scope.settRedigeringsIndex(-1);
            form.$setValidity('arbeidsforhold.leggtilnyttarbeidsforhold.feilmelding', true);
            form.$setValidity('arbeidsforhold.endrearbeidsforhold.feilmelding', true);

        }
    })
