angular.module('nav.ytelser',[])
    .controller('YtelserCtrl', ['$scope', function ($scope) {
        $scope.navigering = {nesteside: 'personalia'};
        $scope.sidedata = {navn: 'ytelser'};

        var nokler = ['ventelonn', 'stonadFisker', 'offentligTjenestepensjon', 'privatTjenestepensjon', 'vartpenger', 'dagpengerEOS', 'annenYtelse', 'ingenYtelse' ];

        $scope.validerYtelser = function(form) {
            var minstEnAvhuket = $scope.erCheckboxerAvhuket(nokler);
            if (form.$error['ytelser'] === undefined) {
                form.$error['ytelser'] = [];
            }

            leggTilFeilmeldingHvisDenIkkeFinnes('ytelser','harValgtYtelse', form ,"ytelser.harValgtYtelse.feilmelding", true, false);
            leggTilFeilmeldingHvisDenIkkeFinnes('ytelser','minstEnAvhuket', form ,"ytelser.minstEnAvhuket.feilmelding", minstEnAvhuket, false);

            console.log("valider ytelser")
            console.log("valgt ytelser " + form.$error['ytelser'][0].$valid);
            console.log("avhuket " + form.$error['ytelser'][1].$valid);

//            form.$setValidity('ytelser.harValgtYtelse.feilmelding', true); // Fjerne feil som kan være satt dersom man prøver å huke av "nei" mens andre checkboxer er avhuket.
//            form.$setValidity("ytelser.minstEnAvhuket.feilmelding", minstEnAvhuket);
            $scope.validateForm(form.$invalid);
            $scope.runValidation();
        };

        $scope.endreYtelse = function(form) {
            var ytelserNokler = nokler.slice(0, nokler.length - 1);
            var harIkkeValgtYtelse = !$scope.erCheckboxerAvhuket(ytelserNokler);

            if (form.$error['ytelser'] === undefined) {
                form.$error['ytelser'] = [];
            }

            leggTilFeilmeldingHvisDenIkkeFinnes('ytelser','harValgtYtelse', form ,"ytelser.harValgtYtelse.feilmelding", harIkkeValgtYtelse, false);
            leggTilFeilmeldingHvisDenIkkeFinnes('ytelser','minstEnAvhuket', form,"ytelser.minstEnAvhuket.feilmelding", !harIkkeValgtYtelse, false);

            if (harIkkeValgtYtelse) {
                settEgendefinertFeilmeldingsverdi(form, 'ytelser','harValgtYtelse', true);
                settEgendefinertFeilmeldingsverdi(form, 'ytelser','minstEnAvhuket', !harIkkeValgtYtelse);
            } else {
                settEgendefinertFeilmeldingsverdi(form, 'ytelser','minstEnAvhuket', true);
            }

            if ($scope.soknadData.fakta.ingenYtelse != undefined && $scope.soknadData.fakta.ingenYtelse.value) {
                $scope.soknadData.fakta.ingenYtelse.value = false;
                $scope.$emit("OPPDATER_OG_LAGRE", {key: 'ingenYtelse', value: false});
            }
            console.log("endre ytelser")
            console.log("valgt ytelser" + form.$error['ytelser'][0].$valid);
            console.log("avhuket" + form.$error['ytelser'][1].$valid);

        }

        $scope.endreIngenYtelse = function(form) {
            var ytelserNokler = nokler.slice(0, nokler.length - 1);
            var harValgtYtelse = $scope.erCheckboxerAvhuket(ytelserNokler);
            var verdi = $scope.soknadData.fakta.ingenYtelse.value;

            //burde kunne trekkes ut?
            if (form.$error['ytelser'] === undefined) {
                form.$error['ytelser'] = [];
            }
            //litt usikker på hva veridene skal være her
            leggTilFeilmeldingHvisDenIkkeFinnes('ytelser','harValgtYtelse', form ,"ytelser.harValgtYtelse.feilmelding", harValgtYtelse, false);
            leggTilFeilmeldingHvisDenIkkeFinnes('ytelser','minstEnAvhuket', form,"ytelser.minstEnAvhuket.feilmelding", harValgtYtelse, false);

            if (harValgtYtelse) {
                if (Object.keys($scope.soknadData.fakta.ingenYtelse).length == 1) {
                    $scope.$emit("OPPDATER_OG_LAGRE", {key: 'ingenYtelse', value: 'false'});
                }
                $scope.soknadData.fakta.ingenYtelse.value = 'false';

                settEgendefinertFeilmeldingsverdi(form, 'ytelser','harValgtYtelse', false);

                $scope.runValidation();
            } else {
                if (verdi) {
                    settEgendefinertFeilmeldingsverdi(form, 'ytelser','minstEnAvhuket', true);
                }

                $scope.$emit("OPPDATER_OG_LAGRE", {key: 'ingenYtelse', value: verdi});
            }
            console.log("endre ingenytelser");
            console.log("valgt ytelser" + form.$error['ytelser'][0].$valid);
            console.log("avhuket" + form.$error['ytelser'][1].$valid);
        }


        $scope.erCheckboxerAvhuket = function(checkboxNokler) {
            var minstEnAvhuket = false;
            for(var i= 0; i < checkboxNokler.length; i++) {
                var nokkel = checkboxNokler[i];
                if ($scope.soknadData.fakta[nokkel] && checkTrue($scope.soknadData.fakta[nokkel].value)) {
                    minstEnAvhuket = true;
                }
            }
            return minstEnAvhuket;
        }

        $scope.ingenYtelserOppsummeringSkalVises = function() {
            if ($scope.soknadData != undefined && $scope.soknadData.fakta != undefined) {
                return $scope.soknadData.fakta.ingenYtelse && checkTrue($scope.soknadData.fakta.ingenYtelse.value) && $scope.hvisIOppsummeringsmodus();
            }
            return false;

        }
    }]);