angular.module('nav.ytelser', [])
    .controller('YtelserCtrl', ['$scope', 'lagreSoknadData', function ($scope, lagreSoknadData) {
        $scope.ytelser = {skalViseFeilmeldingForIngenYtelser: false};
        $scope.ytelserNAV = {skalViseFeilmeldingForIngenNavYtelser: false};

    var nokler = ['stonadFisker', 'offentligTjenestepensjon', 'privatTjenestepensjon', 'vartpenger', 'etterlonn', 'garantilott', 'dagpengerEOS', 'annenYtelse', 'ingenYtelse' ];
    var undernokler = ['sykepenger', 'aap', 'uforetrygd', 'svangerskapspenger', 'foreldrepenger', 'ventelonn',  'ingennavytelser' ];

    $scope.harHuketAvCheckboksYtelse = {value : ''};
    $scope.harHuketAvCheckboksNavYtelse = {value : ''};

        if (erCheckboxerAvhuket(nokler)){
            $scope.harHuketAvCheckboksYtelse.value = true;
            $scope.harHuketAvCheckboksNavYtelse.value = true;
        }

        $scope.$on('VALIDER_YTELSER', function () {
            $scope.validerYtelser(false);
        });

        $scope.validerOgSettModusOppsummering = function(form) {
            $scope.validateForm(form.$invalid);
            $scope.validerYtelser(true);
        }

       $scope.hvisAvtaleInngaatt = function () {
        if ($scope.soknadData.fakta != undefined && $scope.soknadData.fakta.avtale != undefined ) {
            return $scope.soknadData.fakta.avtale.value == 'avtale';
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

            if (sjekkOmGittEgenskapTilObjektErTrue($scope.soknadData.fakta.ingenYtelse)) {
               $scope.soknadData.fakta.ingenYtelse.value = false;
               $scope.$emit(lagreSoknadData, {key: 'ingenYtelse', value: false});
            }
        }

        //      kjøres hver gang det skjer en endring på 'ingenYtelse'-checkboksen
        $scope.endreIngenYtelse = function (form) {
            // Sjekker om en ytelse er huket av (inkluderer IKKE siste checkboksen)
            var ytelserNokler = nokler.slice(0, nokler.length - 1);
            var harValgtYtelse = erCheckboxerAvhuket(ytelserNokler);

            var erCheckboksForIngenYtelseHuketAv = $scope.soknadData.fakta.ingenYtelse.value;

            if (harValgtYtelse) {

                if (Object.keys($scope.soknadData.fakta.ingenYtelse).length == 1) {
                    $scope.$emit(lagreSoknadData, {key: 'ingenYtelse', value: 'false'});
                }

              //Fjerner krysset for andre ytelser
              $scope.soknadData.fakta.ingenYtelse.value = 'false';
              //Viser feilmelding
              $scope.ytelser.skalViseFeilmeldingForIngenYtelser = true;

            } else {
                if (erCheckboksForIngenYtelseHuketAv) {
                    $scope.harHuketAvCheckboksYtelse.value = 'true';
                }
                $scope.$emit(lagreSoknadData, {key: 'ingenYtelse', value: erCheckboksForIngenYtelseHuketAv});
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
        var ytelserNokler = undernokler.slice(0, nokler.length - 1);
        var harValgtNavYtelse = erCheckboxerAvhuket(ytelserNokler);
        var erCheckboksForIngenNavYtelseHuketAv = $scope.soknadData.fakta.ingenNavYtelse.value;
        console.log("Test");
        if (harValgtNavYtelse) {
            console.log("Ytelsersjekket av");
            if (Object.keys($scope.soknadData.fakta.ingenNavYtelse).length == 1) {
                $scope.$emit(lagreSoknadData, {key: 'ingenNavYtelse', value: 'false'});
            }

            //Fjerner krysset for andre ytelser
            $scope.soknadData.fakta.ingenYtelse.value = 'false';
            //Viser feilmelding
            $scope.ytelser.skalViseFeilmeldingForIngenYtelser = true;
            console.log("Fjernet");

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
            var minstEnCheckboksErAvhuket = false;
            for (var i = 0; i < checkboxNokler.length; i++) {
                var nokkel = checkboxNokler[i];
                if ($scope.soknadData.fakta[nokkel] && checkTrue($scope.soknadData.fakta[nokkel].value)) {
                    minstEnCheckboksErAvhuket = true;
                }
            }
            return minstEnCheckboksErAvhuket;
        }

}]);
