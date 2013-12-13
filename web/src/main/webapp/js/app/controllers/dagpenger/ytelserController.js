angular.module('nav.ytelser', [])
    .controller('YtelserCtrl', ['$scope', 'lagreSoknadData', function ($scope, lagreSoknadData) {
        var minstEnCheckboksErAvhuketFeilmeldingNavn = 'minstEnCheckboksErAvhuket';
        var minstEnCheckboksErAvhuketFeilmeldingNokkel = 'ytelser.minstEnCheckboksErAvhuket.feilmelding';
        var feilmeldingKategori = 'ytelser';
        var referanseTilFeilmeldingslinken = 'stonadFisker';

        $scope.ytelser = {skalViseFeilmeldingForIngenYtelser: false};

        var nokler = ['ventelonn', 'stonadFisker', 'offentligTjenestepensjon', 'privatTjenestepensjon', 'vartpenger', 'dagpengerEOS', 'annenYtelse', 'ingenYtelse' ];

        $scope.$on('VALIDER_YTELSER', function (scope, form) {
            console.log("validerYtelser");
            $scope.validerYtelser(form, false);
        });

//      sjekker om formen er validert når bruker trykker ferdig med ytelser
        $scope.validerYtelser = function (form, skalScrolle) {
            console.log("validerer ytelser");
            var minstEnCheckboksErAvhuket = erCheckboxerAvhuket(nokler);
            $scope.ytelser.skalViseFeilmeldingForIngenYtelser = false;
            console.log("skalviseFeilmeldingforingen");
            settEgendefinertFeilmeldingsverdi(form, feilmeldingKategori, minstEnCheckboksErAvhuketFeilmeldingNavn, minstEnCheckboksErAvhuketFeilmeldingNokkel, referanseTilFeilmeldingslinken, minstEnCheckboksErAvhuket, true);
            $scope.validateForm(form.$invalid);
            $scope.runValidation(skalScrolle);
        };

//      kjøres hver gang det skjer en endring på checkboksene
        $scope.endreYtelse = function (form) {
            // Sjekker om en ytelse er huket av (inkluderer IKKE siste checkboksen)

            var ytelserNokler = nokler.slice(0, nokler.length - 1);
            var harIkkeValgtYtelse = !erCheckboxerAvhuket(ytelserNokler);

            if (harIkkeValgtYtelse) {
                $scope.ytelser.skalViseFeilmeldingForIngenYtelser = false;
                console.log("har Ikke valgt ytelse " +  $scope.ytelser.skalViseFeilmeldingForIngenYtelser);
                settEgendefinertFeilmeldingsverdi(form, feilmeldingKategori, minstEnCheckboksErAvhuketFeilmeldingNavn, minstEnCheckboksErAvhuketFeilmeldingNokkel, referanseTilFeilmeldingslinken, false, true);

            } else {
                console.log("har valgt ytelse " +  $scope.ytelser.skalViseFeilmeldingForIngenYtelser);
                settEgendefinertFeilmeldingsverdi(form, feilmeldingKategori, minstEnCheckboksErAvhuketFeilmeldingNavn, minstEnCheckboksErAvhuketFeilmeldingNokkel, referanseTilFeilmeldingslinken, true, true);
            }

            if (sjekkOmGittEgenskapTilObjektErTrue($scope.soknadData.fakta.ingenYtelse)) {
                $scope.soknadData.fakta.ingenYtelse.value = false;
                console.log("Skrur av ingen ytelse");
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

                $scope.soknadData.fakta.ingenYtelse.value = 'false';
                $scope.ytelser.skalViseFeilmeldingForIngenYtelser = true;

            } else {
                if (erCheckboksForIngenYtelseHuketAv) {
                    form.$setValidity(minstEnCheckboksErAvhuketFeilmeldingNavn, true);
                     settEgendefinertFeilmeldingsverdi(form, feilmeldingKategori, minstEnCheckboksErAvhuketFeilmeldingNavn, minstEnCheckboksErAvhuketFeilmeldingNokkel, referanseTilFeilmeldingslinken, true, true);
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
