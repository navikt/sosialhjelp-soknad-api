angular.module('nav.ytelser',[])
    .controller('YtelserCtrl', ['$scope', 'lagreSoknadData', function ($scope, lagreSoknadData) {
        const minstEnCheckboksErAvhuketFeilmeldingNavn = 'minstEnCheckboksErAvhuket';
        const minstEnCheckboksErAvhuketFeilmeldingNokkel = 'ytelser.minstEnCheckboksErAvhuket.feilmelding';
        const feilmeldingKategori = 'ytelser';

        $scope.ytelser = {skalViseFeilmeldingForIngenYtelser: false};

        var nokler = ['ventelonn', 'stonadFisker', 'offentligTjenestepensjon', 'privatTjenestepensjon', 'vartpenger', 'dagpengerEOS', 'annenYtelse', 'ingenYtelse' ];

//      sjekker om formen er validert når bruker trykker ferdig med ytelser
        $scope.validerYtelser = function(form) {
            form.$setValidity('ytelser.harValgtYtelse', true);

            var minstEnCheckboksErAvhuket = erCheckboxerAvhuket(nokler);
            settEgendefinertFeilmeldingsverdi(form, feilmeldingKategori, minstEnCheckboksErAvhuketFeilmeldingNavn, minstEnCheckboksErAvhuketFeilmeldingNokkel, minstEnCheckboksErAvhuket, true);

            $scope.validateForm(form.$invalid);
            $scope.runValidation();
        };

//      kjøres hver gang det skjer en endring på checkboksene
        $scope.endreYtelse = function(form) {
            // Sjekker om en ytelse er huket av (inkluderer IKKE siste checkboksen)
            var ytelserNokler = nokler.slice(0, nokler.length - 1);
            var harIkkeValgtYtelse = !erCheckboxerAvhuket(ytelserNokler);

            if (harIkkeValgtYtelse) {
                $scope.ytelser.skalViseFeilmeldingForIngenYtelser = false;
                settEgendefinertFeilmeldingsverdi(form, feilmeldingKategori, minstEnCheckboksErAvhuketFeilmeldingNavn, minstEnCheckboksErAvhuketFeilmeldingNokkel, false, true);

            } else {
                settEgendefinertFeilmeldingsverdi(form, feilmeldingKategori, minstEnCheckboksErAvhuketFeilmeldingNavn, minstEnCheckboksErAvhuketFeilmeldingNokkel, true, true);
                form.$setValidity('ytelser.harValgtYtelse', true);

            }

            if (checkTrue($scope.soknadData.fakta.ingenYtelse.value)) {
                $scope.soknadData.fakta.ingenYtelse.value = false;
                $scope.$emit(lagreSoknadData, {key: 'ingenYtelse', value: false});
            }
        }

        //      kjøres hver gang det skjer en endring på 'ingenYtelse'-checkboksen
        $scope.endreIngenYtelse = function(form) {
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
                    form.$setValidity('ytelser.harValgtYtelse', false);
                    settEgendefinertFeilmeldingsverdi(form, feilmeldingKategori, minstEnCheckboksErAvhuketFeilmeldingNavn, minstEnCheckboksErAvhuketFeilmeldingNokkel, true, true);
                }
                $scope.$emit(lagreSoknadData, {key: 'ingenYtelse', value: erCheckboksForIngenYtelseHuketAv});
            }
        }

        $scope.ingenYtelserOppsummeringSkalVises = function() {
            if ($scope.soknadData && $scope.soknadData.fakta) {
                return $scope.soknadData.fakta.ingenYtelse && checkTrue($scope.soknadData.fakta.ingenYtelse.value) && $scope.hvisIOppsummeringsmodus();
            }
            return false;
        }

        function erCheckboxerAvhuket(checkboxNokler) {
            var minstEnCheckboksErAvhuket = false;
            for(var i= 0; i < checkboxNokler.length; i++) {
                var nokkel = checkboxNokler[i];
                if ($scope.soknadData.fakta[nokkel] && checkTrue($scope.soknadData.fakta[nokkel].value)) {
                    minstEnCheckboksErAvhuket = true;
                }
            }
            return minstEnCheckboksErAvhuket;
        }
    }]);