angular.module('nav.ytelser',[])
    .controller('YtelserCtrl', ['$scope', function ($scope) {
        $scope.navigering = {nesteside: 'personalia'};
        $scope.sidedata = {navn: 'ytelser'};

        var nokler = ['ventelonn', 'stonadFisker', 'offentligTjenestepensjon', 'privatTjenestepensjon', 'vartpenger', 'dagpengerEOS', 'annenYtelse', 'ingenYtelse' ];

//      sjekker om formen er validert når bruker trykker ferdig med ytelser
        $scope.validerYtelser = function(form) {
            var minstEnAvhuket = $scope.erCheckboxerAvhuket(nokler);
            if (form.$error['ytelser'] === undefined) {
                form.$error['ytelser'] = [];
            }

            $scope.validateForm(form.$invalid);
            $scope.runValidation();
        };

//      kjøres hver gang det skjer en endring på checkboksene
        $scope.endreYtelse = function(form) {
            var ytelserNokler = nokler.slice(0, nokler.length - 1);
            var harIkkeValgtYtelse = !$scope.erCheckboxerAvhuket(ytelserNokler);

            if (form.$error['ytelser'] === undefined) {
                form.$error['ytelser'] = [];
            }

            if (harIkkeValgtYtelse) {
                settEgendefinertFeilmeldingsverdi(form, 'ytelser','harValgtYtelse', "ytelser.harValgtYtelse.feilmelding", true, true );
                settEgendefinertFeilmeldingsverdi(form, 'ytelser','minstEnAvhuket', "ytelser.minstEnAvhuket.feilmelding", false, true);
            } else {
                settEgendefinertFeilmeldingsverdi(form, 'ytelser','minstEnAvhuket', "ytelser.minstEnAvhuket.feilmelding", true, true);
            }

            if ($scope.soknadData.fakta.ingenYtelse != undefined && $scope.soknadData.fakta.ingenYtelse.value) {
                $scope.soknadData.fakta.ingenYtelse.value = false;
                $scope.$emit("OPPDATER_OG_LAGRE", {key: 'ingenYtelse', value: false});
            }
        }

        $scope.endreIngenYtelse = function(form) {
            var ytelserNokler = nokler.slice(0, nokler.length - 1);
            var harValgtYtelse = $scope.erCheckboxerAvhuket(ytelserNokler);
            var verdi = $scope.soknadData.fakta.ingenYtelse.value;

            if (form.$error['ytelser'] === undefined) {
                form.$error['ytelser'] = [];
            }

            if (harValgtYtelse) {
                if (Object.keys($scope.soknadData.fakta.ingenYtelse).length == 1) {
                    $scope.$emit("OPPDATER_OG_LAGRE", {key: 'ingenYtelse', value: 'false'});
                }

                $scope.soknadData.fakta.ingenYtelse.value = 'false';
                settEgendefinertFeilmeldingsverdi(form, 'ytelser','harValgtYtelse',"ytelser.harValgtYtelse.feilmelding", false, true);
                $scope.runValidation();

            } else {
                if (verdi) {
                    settEgendefinertFeilmeldingsverdi(form, 'ytelser','minstEnAvhuket', "ytelser.minstEnAvhuket.feilmelding", true, true);
                }

                $scope.$emit("OPPDATER_OG_LAGRE", {key: 'ingenYtelse', value: verdi});
            }
            console.log(form.$error);
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