angular.module('nav.ytelser', [])
    .controller('YtelserCtrl', ['$scope', 'lagreSoknadData', function ($scope, lagreSoknadData) {
        $scope.ytelser = {skalViseFeilmeldingForIngenYtelser: false};

        var nokler = ['ventelonn', 'stonadFisker', 'offentligTjenestepensjon', 'privatTjenestepensjon', 'vartpenger', 'dagpengerEOS', 'annenYtelse', 'ingenYtelse' ];

        $scope.harHuketAvCheckboks = {value : ''};

        if (erCheckboxerAvhuket(nokler)){
            $scope.harHuketAvCheckboks.value = true;
        }

        $scope.$on('VALIDER_YTELSER', function () {
            $scope.validerYtelser(false);
        });

        $scope.validerOgSettModusOppsummering = function(form) {
            $scope.validateForm(form.$invalid);
            $scope.validerYtelser(true);
        }

//      sjekker om formen er validert når bruker trykker ferdig med ytelser
        $scope.validerYtelser = function (skalScrolle) {
            $scope.ytelser.skalViseFeilmeldingForIngenYtelser = false;
            $scope.runValidation(skalScrolle);
        };

//      kjøres hver gang det skjer en endring på checkboksene
        $scope.endreYtelse = function (form) {
            // Sjekker om en ytelse er huket av (inkluderer IKKE siste checkboksen)
            var ytelserNokler = nokler.slice(0, nokler.length - 1);
            var harIkkeValgtYtelse = !erCheckboxerAvhuket(ytelserNokler);

            if (harIkkeValgtYtelse) {
                $scope.ytelser.skalViseFeilmeldingForIngenYtelser = false;
                $scope.harHuketAvCheckboks.value = '';

            } else {

                $scope.harHuketAvCheckboks.value = true;

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
                    $scope.harHuketAvCheckboks.value = 'true';
                }
                $scope.$emit(lagreSoknadData, {key: 'ingenYtelse', value: erCheckboksForIngenYtelseHuketAv});
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
