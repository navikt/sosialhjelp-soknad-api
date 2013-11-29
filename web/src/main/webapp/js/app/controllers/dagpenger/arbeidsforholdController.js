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
                $scope.settBreddeSlikAtDetFungererIIE();
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
                $scope.settBreddeSlikAtDetFungererIIE();
            }

            $scope.settBreddeSlikAtDetFungererIIE = function() {
                setTimeout(function() {
                    $("#sluttaarsak_id").width($("#sluttaarsak_id").width());
                }, 50);
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

               $scope.resolvUrl = function () {
                return "../html/templates/kontrakt-utgaatt.html"
            }

            $scope.validateTilFraDato = function (af, form) {
                if (af && (af.varighetTil <= af.varighetFra)) {
                    af.varighetTil = '';
                    form.$setValidity('arbeidsforhold.arbeidsgiver.varighet.feilmelding', false);
                } else {
                    form.$setValidity('arbeidsforhold.arbeidsgiver.varighet.feilmelding', true);
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

            $scope.validateOppsigelsestidTilFraDato = function (af) {
                if (af && (af.sagtOppAvArbeidsgiverVarighetTil <= af.sagtOppAvArbeidsgiverVarighetFra)) {
                    af.sagtOppAvArbeidsgiverVarighetTil = '';

                    form.$setValidity('arbeidsforhold.sluttaarsak.sagtoppavarbeidsgiver.varighet.feilmelding', false);
                } else {
                    form.$setValidity('arbeidsforhold.sluttaarsak.sagtoppavarbeidsgiver.varighet.feilmelding', true);
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
            form.$setValidity('arbeidsforhold.arbeidsgiver.varighet.feilmelding', true);
            form.$setValidity('arbeidsforhold.sluttaarsak.sagtoppavarbeidsgiver.varighet.feilmelding', true);


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

        $scope.permitteringsgrad = [{
            id: '1',
            name: '1%'},
            {
                id: '2',
                name: '2%'},
            {
                id: '3',
                name: '3%'},
            {
                id: '4',
                name: '4%'},
            {
                id: '5',
                name: '5%'},
            {
                id: '6',
                name: '6%'},
            {
                id: '7',
                name: '7%'},
            {
                id: '8',
                name: '8%'},
            {
                id: '9',
                name: '9%'},
            {
                id: '10',
                name: '10%'},
            { id: '11',
            name: '11%'},
    {
        id: '12',
        name: '12%'},
    {
        id: '13',
        name: '13%'},
    {
        id: '14',
        name: '14%'},
    {
        id: '15',
        name: '15%'},
    {
        id: '16',
        name: '16%'},
    {
        id: '17',
        name: '17%'},
    {
        id: '18',
        name: '18%'},
    {
        id: '19',
        name: '19%'},
    {
        id: '20',
        name: '20%'},
            { id: '21',
                name: '21%'},
            {
                id: '22',
                name: '22%'},
            {
                id: '23',
                name: '23%'},
            {
                id: '24',
                name: '24%'},
            {
                id: '25',
                name: '25%'},
            {
                id: '26',
                name: '26%'},
            {
                id: '27',
                name: '27%'},
            {
                id: '28',
                name: '28%'},
            {
                id: '29',
                name: '29%'},
            {
                id: '30',
                name: '30%'},
            { id: '31',
                name: '31%'},
            {
                id: '32',
                name: '32%'},
            {
                id: '33',
                name: '33%'},
            {
                id: '34',
                name: '34%'},
            {
                id: '35',
                name: '35%'},
            {
                id: '36',
                name: '36%'},
            {
                id: '37',
                name: '37%'},
            {
                id: '38',
                name: '38%'},
            {
                id: '39',
                name: '39%'},
            {
                id: '40',
                name: '40%'},
            { id: '41',
                name: '41%'},
            {
                id: '42',
                name: '42%'},
            {
                id: '43',
                name: '43%'},
            {
                id: '44',
                name: '44%'},
            {
                id: '45',
                name: '45%'},
            {
                id: '46',
                name: '46%'},
            {
                id: '47',
                name: '47%'},
            {
                id: '48',
                name: '48%'},
            {
                id: '49',
                name: '49%'},
            {
                id: '50',
                name: '50%'},
            { id: '51',
                name: '51%'},
            {
                id: '52',
                name: '52%'},
            {
                id: '53',
                name: '53%'},
            {
                id: '54',
                name: '54%'},
            {
                id: '55',
                name: '55%'},
            {
                id: '56',
                name: '56%'},
            {
                id: '57',
                name: '57%'},
            {
                id: '58',
                name: '58%'},
            {
                id: '59',
                name: '59%'},
            {
                id: '60',
                name: '60%'},
            { id: '61',
                name: '61%'},
            {
                id: '62',
                name: '62%'},
            {
                id: '63',
                name: '63%'},
            {
                id: '64',
                name: '64%'},
            {
                id: '65',
                name: '65%'},
            {
                id: '66',
                name: '66%'},
            {
                id: '67',
                name: '67%'},
            {
                id: '68',
                name: '68%'},
            {
                id: '69',
                name: '69%'},
            {
                id: '70',
                name: '70%'},
            { id: '71',
                name: '71%'},
            {
                id: '72',
                name: '72%'},
            {
                id: '73',
                name: '73%'},
            {
                id: '74',
                name: '74%'},
            {
                id: '75',
                name: '75%'},
            {
                id: '76',
                name: '76%'},
            {
                id: '77',
                name: '77%'},
            {
                id: '78',
                name: '78%'},
            {
                id: '79',
                name: '79%'},
            {
                id: '80',
                name: '80%'},
            { id: '81',
                name: '81%'},
            {
                id: '82',
                name: '82%'},
            {
                id: '83',
                name: '83%'},
            {
                id: '84',
                name: '84%'},
            {
                id: '85',
                name: '85%'},
            {
                id: '86',
                name: '86%'},
            {
                id: '87',
                name: '87%'},
            {
                id: '88',
                name: '88%'},
            {
                id: '89',
                name: '89%'},
            {
                id: '90',
                name: '90%'},
            { id: '91',
                name: '91%'},
            {
                id: '92',
                name: '92%'},
            {
                id: '93',
                name: '93%'},
            {
                id: '94',
                name: '94%'},
            {
                id: '95',
                name: '95%'},
            {
                id: '96',
                name: '96%'},
            {
                id: '97',
                name: '97%'},
            {
                id: '98',
                name: '98%'},
            {
                id: '99',
                name: '99%'},
            {
                id: '100',
                name: '100%'}];
    })
