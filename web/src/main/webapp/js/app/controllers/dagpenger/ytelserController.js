angular.module('nav.ytelser', [])
    .controller('YtelserCtrl', ['$scope', 'lagreSoknadData', 'data', function ($scope, lagreSoknadData, data) {
        $scope.ytelser = {skalViseFeilmeldingForIngenYtelser: false};
        $scope.ytelserNAV = {skalViseFeilmeldingForIngenNavYtelser: false};

        var nokler = ['stonadFisker', 'offentligTjenestepensjon', 'privatTjenestepensjon', 'vartpenger', 'etterlonn', 'garantilott', 'dagpengerEOS', 'annenYtelse', 'ingenYtelse' ];
        var undernokler = ['sykepenger', 'aap', 'uforetrygd', 'svangerskapspenger', 'foreldrepenger', 'ventelonn',  'ingennavytelser' ];

        $scope.harHuketAvCheckboksYtelse = {value : ''};
        $scope.harHuketAvCheckboksNavYtelse = {value : ''};

        if (erCheckboxerAvhuket(nokler)) {
            $scope.harHuketAvCheckboksYtelse.value = true;
            $scope.harHuketAvCheckboksNavYtelse.value = true;
        }

        $scope.$on('VALIDER_YTELSER', function () {
            $scope.validerYtelser(false);
        });

        $scope.validerOgSettModusOppsummering = function (form) {
            $scope.validateForm(form.$invalid);
            $scope.validerYtelser(true);
        }

       $scope.hvisAvtaleInngaatt = function () {
            var faktum = data.finnFaktum('avtale');
            if (faktum != undefined && faktum.value != undefined ) {
                return faktum.value == 'avtale';
            }
            return false;
        }

//      sjekker om formen er validert når bruker trykker ferdig med ytelser
        $scope.validerYtelser = function (skalScrolle) {
            $scope.ytelser.skalViseFeilmeldingForIngenYtelser = false;
            $scope.ytelser.skalViseFeilmeldingForIngenNavYtelser = false;
            $scope.ytelser.skalViseFeilmeldingForAvtale = false;
            $scope.runValidation(skalScrolle);
        };

//      kjøres hver gang det skjer en endring på checkboksene
        $scope.endreYtelse = function (form) {
            // Sjekker om en ytelse er huket av (inkluderer IKKE siste checkboksen)
            var ytelserNokler = nokler.slice(0, nokler.length - 1);
            var harIkkeValgtYtelse = !erCheckboxerAvhuket(ytelserNokler);

            if (harIkkeValgtYtelse) {
                $scope.ytelser.skalViseFeilmeldingForIngenYtelser = false;
                $scope.harHuketAvCheckboksYtelse.value = '';

            } else {

                $scope.harHuketAvCheckboksYtelse.value = true;

            }

            var ingenYtelse = data.finnFaktum("ingenYtelse");
            if (sjekkOmGittEgenskapTilObjektErTrue(ingenYtelse)) {
                ingenYtelse.value = 'false';
                ingenYtelse.$save();
            }
        }

        //      kjøres hver gang det skjer en endring på 'ingenYtelse'-checkboksen
        $scope.endreIngenYtelse = function () {
            var faktum = data.finnFaktum("ingenYtelse");
            // Sjekker om en ytelse er huket av (inkluderer IKKE siste checkboksen)
            var ytelserNokler = nokler.slice(0, nokler.length - 1);
            var harValgtYtelse = erCheckboxerAvhuket(ytelserNokler);

            var erCheckboksForIngenYtelseHuketAv = faktum.value == 'true';

            if (harValgtYtelse) {
                faktum.value = 'false';
                $scope.ytelser.skalViseFeilmeldingForIngenYtelser = true;

            } else {
                if (erCheckboksForIngenYtelseHuketAv) {
                    $scope.harHuketAvCheckboksYtelse.value = 'true';
                }
                faktum.$save()
            }
        }

    //      kjøres hver gang det skjer en endring på checkboksene for Nav Ytelser
    $scope.endreNavYtelse = function (form) {
        // Sjekker om en navytelse er huket av (inkluderer IKKE siste checkboksen)
        var ytelserNokler = undernokler.slice(0, nokler.length - 1);
        var harIkkeValgtYtelse = !erCheckboxerAvhuket(ytelserNokler);
        if (harIkkeValgtYtelse) {
            $scope.ytelser.skalViseFeilmeldingForIngenNavYtelser = false;
            $scope.harHuketAvCheckboksNavYtelse.value = '';
        } else {
            $scope.harHuketAvCheckboksNavYtelse.value = true;
        }
        if (sjekkOmGittEgenskapTilObjektErTrue($scope.soknadData.fakta.ingenYtelse)) {
            $scope.soknadData.fakta.ingenYtelse.value = false;
            $scope.$emit(lagreSoknadData, {key: 'ingenNavYtelse', value: false});
        }
    }

    //      kjøres hver gang det skjer en endring på 'ingenNAVYtelse'-checkboksen
    $scope.endreIngenNavYtelse = function (form) {
        // Sjekker om en ytelse er huket av (inkluderer IKKE siste checkboksen)
        var ytelserNokler = undernokler.slice(0, undernokler.length - 1);
        var harValgtNavYtelse = erCheckboxerAvhuket(ytelserNokler);
        var erCheckboksForIngenNavYtelseHuketAv = $scope.soknadData.fakta.ingennavytelser.value;
        if (harValgtNavYtelse) {
            if (Object.keys($scope.soknadData.fakta.ingennavytelser).length == 1) {
                $scope.$emit(lagreSoknadData, {key: 'ingennavytelser', value: 'false'});
            }
            //Fjerner krysset for andre ytelser
            $scope.soknadData.fakta.ingennavytelser.value = 'false';
            //Viser feilmelding
            $scope.ytelser.skalViseFeilmeldingForIngenNavYtelser = true;

        } else {
            if (erCheckboksForIngenNavYtelseHuketAv) {
                $scope.harHuketAvCheckboksNavYtelse.value = 'true';
            }
            $scope.$emit(lagreSoknadData, {key: 'ingennavytelser', value: erCheckboksForIngenNavYtelseHuketAv});
        }
    }



    $scope.ingenYtelserOppsummeringSkalVises = function () {
            if ($scope.soknadData && $scope.soknadData.fakta) {
                return $scope.soknadData.fakta.ingenYtelse && checkTrue($scope.soknadData.fakta.ingenYtelse.value) && $scope.hvisIOppsummeringsmodus();
            }
            return false;
        }

        function erCheckboxerAvhuket(checkboxNokler) {
            var minstEnAvhuket = false;
            var fakta = {};
            data.fakta.forEach(function (faktum) {
                if (checkboxNokler.indexOf(faktum.key >= 0)) {
                    fakta[faktum.key] = faktum;
                }
            });

            for (var i = 0; i < checkboxNokler.length; i++) {
                var nokkel = checkboxNokler[i];
                if (fakta[nokkel] && checkTrue(fakta[nokkel].value)) {
                    minstEnAvhuket = true;
                }
            }
            console.log("minst en: " + minstEnAvhuket)
            return minstEnAvhuket;
        }

    }]);
