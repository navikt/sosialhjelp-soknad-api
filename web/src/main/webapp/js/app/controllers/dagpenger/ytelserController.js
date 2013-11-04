angular.module('nav.ytelser',[])
    .controller('YtelserCtrl', ['$scope', function ($scope) {
        $scope.navigering = {nesteside: 'personalia'};
        $scope.sidedata = {navn: 'ytelser'};

        var nokler = ['ventelonn', 'stonadFisker', 'offentligTjenestepensjon', 'privatTjenestepensjon', 'vartpenger', 'dagpengerEOS', 'annenYtelse', 'ingenYtelse' ];

        $scope.validerYtelser = function(form) {
            var minstEnAvhuket = $scope.erCheckboxerAvhuket(nokler);
            form.$setValidity('ytelser.harValgtYtelse.feilmelding', true); // Fjerne feil som kan være satt dersom man prøver å huke av "nei" mens andre checkboxer er avhuket.
            form.$setValidity("ytelser.minstEnAvhuket.feilmelding", minstEnAvhuket);
            $scope.validateForm(form.$invalid);
            $scope.runValidation();
        };

        $scope.endreYtelse = function(form) {
            var ytelserNokler = nokler.slice(0, nokler.length - 1);
            var harIkkeValgtYtelse = !$scope.erCheckboxerAvhuket(ytelserNokler);

            if (harIkkeValgtYtelse) {
                form.$setValidity('ytelser.harValgtYtelse.feilmelding', true);
            } else {
                form.$setValidity("ytelser.minstEnAvhuket.feilmelding", true);
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

            if (harValgtYtelse) {
                if (Object.keys($scope.soknadData.fakta.ingenYtelse).length == 1) {
                    $scope.$emit("OPPDATER_OG_LAGRE", {key: 'ingenYtelse', value: 'false'});
                }
                $scope.soknadData.fakta.ingenYtelse.value = 'false';

                form.$setValidity('ytelser.harValgtYtelse.feilmelding', false);
                $scope.runValidation();
            } else {
                if (verdi) {
                    form.$setValidity("ytelser.minstEnAvhuket.feilmelding", true);
                }

                $scope.$emit("OPPDATER_OG_LAGRE", {key: 'ingenYtelse', value: verdi});
            }
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